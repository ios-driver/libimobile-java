#include <jni.h>
#include "org_uiautomation_iosdriver_DeviceInstallerService.h"


#ifdef HAVE_CONFIG_H
#include <config.h>
#endif
#include <stdlib.h>
#define _GNU_SOURCE 1
#define __USE_GNU 1
#include <stdio.h>
#include <string.h>
#include <getopt.h>
#include <errno.h>
#include <time.h>
#include <libgen.h>
#include <inttypes.h>
#include <limits.h>

#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/lockdown.h>
#include <libimobiledevice/installation_proxy.h>
#include <libimobiledevice/notification_proxy.h>
#include <libimobiledevice/afc.h>

#include <plist/plist.h>

#include <zip.h>


void throwException(JNIEnv *env,char * msg){
    (*env)->ThrowNew(env, (*env)->FindClass(env, "org/uiautomation/iosdriver/services/DeviceInstallerException"), msg);
}


const char PKG_PATH[] = "PublicStaging";
const char APPARCH_PATH[] = "ApplicationArchives";
int archive_mode = 0;
int remove_archive_mode = 0;

/**
* get the uuid of the device from the java service.
*/
char* getServiceUuid(JNIEnv * env, jobject thiz){
    jclass peerCls = (*env)->GetObjectClass(env,thiz);
    jmethodID mGetValue = (*env)->GetMethodID(env,peerCls, "getUuid","()Ljava/lang/String;");
    jstring value = (*env)->CallObjectMethod(env,thiz, mGetValue);
    return (char*)(*env)->GetStringUTFChars(env,value,0);
}

char *options = NULL;
char *appid = NULL;
int notified = 0;
int notification_expected = 0;
char *last_status = NULL;
int wait_for_op_complete = 0;
int op_completed = 0;
int err_occured = 0;
char err_details[256];

static void notifier(const char *notification, void *unused){
	notified = 1;
}
static void status_cb(const char *operation, plist_t status, void *unused){
	if (status && operation) {
		plist_t npercent = plist_dict_get_item(status, "PercentComplete");
		plist_t nstatus = plist_dict_get_item(status, "Status");
		plist_t nerror = plist_dict_get_item(status, "Error");
		int percent = 0;
		char *status_msg = NULL;
		if (npercent) {
			uint64_t val = 0;
			plist_get_uint_val(npercent, &val);
			percent = val;
		}
		if (nstatus) {
			plist_get_string_val(nstatus, &status_msg);
			if (!strcmp(status_msg, "Complete")) {
				op_completed = 1;
			}
		}
		if (!nerror) {
			if (last_status && (strcmp(last_status, status_msg))) {
				printf("\n");
			}

			if (!npercent) {
				printf("%s - %s\n", operation, status_msg);
			} else {
				printf("%s - %s (%d%%)\r", operation, status_msg, percent);
			}
		} else {
			char *err_msg = NULL;
			plist_get_string_val(nerror, &err_msg);
			printf("%s - Error occured: %s\n", operation, err_msg);
			err_occured = 1;
			char msg[256];
            strcpy(msg,operation);
            strcat(msg," failed : ");
            strcat(msg,err_msg);
            strcpy(err_details,msg);
            free(err_msg);

		}
		if (last_status) {
			free(last_status);
			last_status = NULL;
		}
		if (status_msg) {
			last_status = strdup(status_msg);
			free(status_msg);
		}
	} else {
		printf("%s: called with invalid data!\n", __func__);
	}
}


static void do_wait_when_needed(){
	int i = 0;
	struct timespec ts;
	ts.tv_sec = 0;
	ts.tv_nsec = 500000000;

	/* wait for operation to complete */
	while (wait_for_op_complete && !op_completed && !err_occured
		   && !notified && (i < 60)) {
		nanosleep(&ts, NULL);
		i++;
	}

	/* wait some time if a notification is expected */
	while (notification_expected && !notified && !err_occured && (i < 10)) {
		nanosleep(&ts, NULL);
		i++;
	}
}

JNIEXPORT jstring JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_listApps(JNIEnv * env, jobject thiz, jint type){

    const char *uuid = getServiceUuid(env,thiz);
    idevice_t phone = NULL;
    lockdownd_client_t client = NULL;
    instproxy_client_t ipc = NULL;
    np_client_t np = NULL;
    afc_client_t afc = NULL;
    uint16_t port = 0;
    int res = 0;
    jstring dummy;


    if (IDEVICE_E_SUCCESS != idevice_new(&phone, uuid)) {
            char msg[256];
            strcpy(msg,uuid);
            strcat(msg," not found, is it plugged in?");
    		throwException(env,msg);
    		goto leave_cleanup;
    }
    if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(phone, &client, "ideviceinstaller")) {
    		throwException(env, "Could not connect to lockdownd. Exiting.");
    		goto leave_cleanup;
    }

    if ((lockdownd_start_service
    		 (client, "com.apple.mobile.notification_proxy",
    		  &port) != LOCKDOWN_E_SUCCESS) || !port) {
    		throwException(env, "Could not start com.apple.mobile.notification_proxy!");
    		goto leave_cleanup;
    }

    if (np_client_new(phone, port, &np) != NP_E_SUCCESS) {
        throwException(env, "Could not connect to notification_proxy!");
        goto leave_cleanup;
    }


    np_set_notify_callback(np, notifier, NULL);

    const char *noties[3] = { NP_APP_INSTALLED, NP_APP_UNINSTALLED, NULL };
	np_observe_notifications(np, noties);

    port = 0;
	if ((lockdownd_start_service(client, "com.apple.mobile.installation_proxy",&port) != LOCKDOWN_E_SUCCESS) || !port) {
		throwException(env, "Could not start com.apple.mobile.installation_proxy!");
		goto leave_cleanup;
	}

	if (instproxy_client_new(phone, port, &ipc) != INSTPROXY_E_SUCCESS) {
		throwException(env,"Could not connect to installation_proxy!");
		goto leave_cleanup;
	}

	setbuf(stdout, NULL);

	if (last_status) {
		free(last_status);
		last_status = NULL;
	}
	notification_expected = 0;


    int xml_mode = 0;
    plist_t client_opts = NULL;

    if (((int)type)==0){
        // list all apps
        client_opts = NULL;
    }else if (((int)type) ==1){
        // list system apps
        instproxy_client_options_add(client_opts, "ApplicationType", "System", NULL);
    }else {
        // list user apps
        client_opts = instproxy_client_options_new();
    }

    instproxy_client_options_add(client_opts, "ApplicationType", "User", NULL);
    instproxy_error_t err;
    plist_t apps = NULL;

    err = instproxy_browse(ipc, client_opts, &apps);
    instproxy_client_options_free(client_opts);
    if (err != INSTPROXY_E_SUCCESS) {
        throwException(env, "instproxy_browse error");
        goto leave_cleanup;
    }
    if (!apps || (plist_get_node_type(apps) != PLIST_ARRAY)) {
        throwException(env, "ERROR: instproxy_browse returnd an invalid plist!");
        goto leave_cleanup;
    }

    char *xml = NULL;
    uint32_t len = 0;

    plist_to_xml(apps, &xml, &len);
    if (xml) {
        //puts(xml);
        jstring retval = (*env)->NewStringUTF(env, xml);
        free(xml);
        return retval;
    }
    plist_free(apps);


    leave_cleanup:
    	if (np) {
    		np_client_free(np);
    	}
    	if (ipc) {
    		instproxy_client_free(ipc);
    	}
    	if (afc) {
    		afc_client_free(afc);
    	}
    	if (client) {
    		lockdownd_client_free(client);
    	}
    	idevice_free(phone);

    	if (uuid) {
    		free((char*)uuid);
    	}
    	if (appid) {
    		free(appid);
    	}
    	if (options) {
    		free(options);
    	}
}




JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_uninstall
  (JNIEnv * env, jobject thiz,  jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_install
  (JNIEnv * env, jobject thiz,  jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_upgrade
  (JNIEnv * env, jobject thiz,  jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_archive(JNIEnv * env, jobject thiz, jstring app, jint uninstall, jint appOnly, jstring destinationFolder){

    char *copy_path = (char*)(*env)->GetStringUTFChars(env,destinationFolder,0);;
    int remove_after_copy = uninstall;
    int skip_uninstall;
    if (uninstall){
       skip_uninstall = 0;
    } else {
       skip_uninstall =1;
    }
    int app_only = appOnly;
    plist_t client_opts = NULL;

    const char *appid = (char*)(*env)->GetStringUTFChars(env,app,0);
    const char *uuid = getServiceUuid(env,thiz);

    idevice_t phone = NULL;
    lockdownd_client_t client = NULL;
    instproxy_client_t ipc = NULL;
    np_client_t np = NULL;
    afc_client_t afc = NULL;
    uint16_t port = 0;
    int res = 0;


    if (IDEVICE_E_SUCCESS != idevice_new(&phone, uuid)) {
      char msg[256];
      strcpy(msg,uuid);
      strcat(msg," not found, is it plugged in?");
      throwException(env,msg);
      goto leave_cleanup;
    }
    if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(phone, &client, "ideviceinstaller")) {
        throwException(env, "Could not connect to lockdownd. Exiting.");
        goto leave_cleanup;
    }

    if ((lockdownd_start_service
         (client, "com.apple.mobile.notification_proxy",
          &port) != LOCKDOWN_E_SUCCESS) || !port) {
        throwException(env, "Could not start com.apple.mobile.notification_proxy!");
        goto leave_cleanup;
    }

    if (np_client_new(phone, port, &np) != NP_E_SUCCESS) {
      throwException(env, "Could not connect to notification_proxy!");
      goto leave_cleanup;
    }


    np_set_notify_callback(np, notifier, NULL);

    const char *noties[3] = { NP_APP_INSTALLED, NP_APP_UNINSTALLED, NULL };
    np_observe_notifications(np, noties);

 run_again:
    port = 0;
    if ((lockdownd_start_service(client, "com.apple.mobile.installation_proxy",&port) != LOCKDOWN_E_SUCCESS) || !port) {
        throwException(env, "Could not start com.apple.mobile.installation_proxy!");
        goto leave_cleanup;
    }

    if (instproxy_client_new(phone, port, &ipc) != INSTPROXY_E_SUCCESS) {
        throwException(env,"Could not connect to installation_proxy!");
        goto leave_cleanup;
    }

    setbuf(stdout, NULL);

    if (last_status) {
        free(last_status);
        last_status = NULL;
    }
    notification_expected = 0;

    if (skip_uninstall || app_only) {
        client_opts = instproxy_client_options_new();
        if (skip_uninstall) {
            instproxy_client_options_add(client_opts, "SkipUninstall", 1, NULL);
        }
        if (app_only) {
            instproxy_client_options_add(client_opts, "ArchiveType", "ApplicationOnly", NULL);
        }
    }
    if (copy_path) {
        struct stat fst;
        if (stat(copy_path, &fst) != 0) {
            fprintf(stderr, "ERROR: stat: %s: %s\n", copy_path, strerror(errno));
            free(copy_path);
            goto leave_cleanup;
    }
    if (!S_ISDIR(fst.st_mode)) {
        fprintf(stderr, "ERROR: '%s' is not a directory as expected.\n", copy_path);
        free(copy_path);
        goto leave_cleanup;
    }

    port = 0;
    if ((lockdownd_start_service(client, "com.apple.afc", &port) != LOCKDOWN_E_SUCCESS) || !port) {
        fprintf(stderr, "Could not start com.apple.afc!\n");
        free(copy_path);
        goto leave_cleanup;
    }
    lockdownd_client_free(client);
    client = NULL;

    if (afc_client_new(phone, port, &afc) != INSTPROXY_E_SUCCESS) {
        fprintf(stderr, "Could not connect to AFC!\n");
        goto leave_cleanup;
        }
    }

    instproxy_archive(ipc, appid, client_opts, status_cb, NULL);

    instproxy_client_options_free(client_opts);
    wait_for_op_complete = 1;
    if (skip_uninstall) {
        notification_expected = 0;
    } else {
        notification_expected = 1;
    }
    do_wait_when_needed();


    if (copy_path) {
        if (err_occured) {
            afc_client_free(afc);
            afc = NULL;
            throwException(env,err_details);
            goto leave_cleanup;
        }
        FILE *f = NULL;
        uint64_t af = 0;
        /* local filename */
        char *localfile = NULL;
        if (asprintf(&localfile, "%s/%s.ipa", copy_path, appid) < 0) {
            fprintf(stderr, "Out of memory!?\n");
            goto leave_cleanup;
        }
        free(copy_path);
        f = fopen(localfile, "w");
        if (!f) {
            fprintf(stderr, "ERROR: fopen: %s: %s\n", localfile, strerror(errno));
            free(localfile);
            goto leave_cleanup;
        }

        /* remote filename */
        char *remotefile = NULL;
        if (asprintf(&remotefile, "%s/%s.zip", APPARCH_PATH, appid) < 0) {
            fprintf(stderr, "Out of memory!?\n");
            goto leave_cleanup;
        }

        uint32_t fsize = 0;
        char **fileinfo = NULL;
        if ((afc_get_file_info(afc, remotefile, &fileinfo) != AFC_E_SUCCESS) || !fileinfo) {
            fprintf(stderr, "ERROR getting AFC file info for '%s' on device!\n", remotefile);
            fclose(f);
            free(remotefile);
            free(localfile);
            goto leave_cleanup;
        }

        int i;
        for (i = 0; fileinfo[i]; i+=2) {
            if (!strcmp(fileinfo[i], "st_size")) {
                fsize = atoi(fileinfo[i+1]);
                break;
            }
        }
        i = 0;
        while (fileinfo[i]) {
            free(fileinfo[i]);
            i++;
        }
        free(fileinfo);

        if (fsize == 0) {
            fprintf(stderr, "Hm... remote file length could not be determined. Cannot copy.\n");
            fclose(f);
            free(remotefile);
            free(localfile);
            goto leave_cleanup;
        }

        if ((afc_file_open(afc, remotefile, AFC_FOPEN_RDONLY, &af) != AFC_E_SUCCESS) || !af) {
            fclose(f);
            fprintf(stderr, "ERROR: could not open '%s' on device for reading!\n", remotefile);
            free(remotefile);
            free(localfile);
            goto leave_cleanup;
        }

        /* copy file over */
        printf("Copying '%s' --> '%s'\n", remotefile, localfile);
        free(remotefile);
        free(localfile);

        uint32_t amount = 0;
        uint32_t total = 0;
        char buf[8192];

        do {
            if (afc_file_read(afc, af, buf, sizeof(buf), &amount) != AFC_E_SUCCESS) {
                fprintf(stderr, "AFC Read error!\n");
                break;
            }

            if (amount > 0) {
                size_t written = fwrite(buf, 1, amount, f);
                if (written != amount) {
                    fprintf(stderr, "Error when writing %d bytes to local file!\n", amount);
                    break;
                }
                total += written;
            }
        } while (amount > 0);

        afc_file_close(afc, af);
        fclose(f);

        printf("done.\n");
        if (total != fsize) {
            fprintf(stderr, "WARNING: remote and local file sizes don't match (%d != %d)\n", fsize, total);
            if (remove_after_copy) {
                fprintf(stderr, "NOTE: archive file will NOT be removed from device\n");
                remove_after_copy = 0;
            }
        }

        if (remove_after_copy) {
            /* remove archive if requested */
            printf("Removing '%s'\n", appid);
            archive_mode = 0;
            remove_archive_mode = 1;
            free(options);
            options = NULL;
            if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(phone, &client, "ideviceinstaller")) {
                fprintf(stderr, "Could not connect to lockdownd. Exiting.\n");
                goto leave_cleanup;
            }
            goto run_again;
        }
    }


     leave_cleanup:
        	if (np) {
        		np_client_free(np);
        	}
        	if (ipc) {
        		instproxy_client_free(ipc);
        	}
        	if (afc) {
        		afc_client_free(afc);
        	}
        	if (client) {
        		lockdownd_client_free(client);
        	}
        	idevice_free(phone);

        	if (uuid) {
        		free((char*)uuid);
        	}
        	if (appid) {
        		free((char*)appid);
        	}
        	if (options) {
        		free(options);
        	}


}
