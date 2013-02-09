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



char *uuid = "d1ce6333af579e27d166349dc8a1989503ba5b4f";
char *options = NULL;
char *appid = NULL;
int notified = 0;
int notification_expected = 0;
char *last_status = NULL;

static void notifier(const char *notification, void *unused)
{
	/* printf("notification received: %s\n", notification);*/
	notified = 1;
}



JNIEXPORT jstring JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_listApps
  (JNIEnv * env, jobject thiz, jstring todouuid,jint type){

    idevice_t phone = NULL;
    lockdownd_client_t client = NULL;
    instproxy_client_t ipc = NULL;
    np_client_t np = NULL;
    afc_client_t afc = NULL;
    uint16_t port = 0;
    int res = 0;


    if (IDEVICE_E_SUCCESS != idevice_new(&phone, uuid)) {
    		fprintf(stderr, "No iPhone found, is it plugged in?\n");
    		return -1;
    }
    if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(phone, &client, "ideviceinstaller")) {
    		fprintf(stderr, "Could not connect to lockdownd. Exiting.\n");
    		goto leave_cleanup;
    }

    if ((lockdownd_start_service
    		 (client, "com.apple.mobile.notification_proxy",
    		  &port) != LOCKDOWN_E_SUCCESS) || !port) {
    		fprintf(stderr,
    				"Could not start com.apple.mobile.notification_proxy!\n");
    		goto leave_cleanup;
    }

    if (np_client_new(phone, port, &np) != NP_E_SUCCESS) {
        fprintf(stderr, "Could not connect to notification_proxy!\n");
        goto leave_cleanup;
    }


    np_set_notify_callback(np, notifier, NULL);

    const char *noties[3] = { NP_APP_INSTALLED, NP_APP_UNINSTALLED, NULL };
	np_observe_notifications(np, noties);

    port = 0;
	if ((lockdownd_start_service
		 (client, "com.apple.mobile.installation_proxy",
		  &port) != LOCKDOWN_E_SUCCESS) || !port) {
		fprintf(stderr,
				"Could not start com.apple.mobile.installation_proxy!\n");
		goto leave_cleanup;
	}

	if (instproxy_client_new(phone, port, &ipc) != INSTPROXY_E_SUCCESS) {
		fprintf(stderr, "Could not connect to installation_proxy!\n");
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
        fprintf(stderr, "ERROR: instproxy_browse returned %d\n", err);
        goto leave_cleanup;
    }
    if (!apps || (plist_get_node_type(apps) != PLIST_ARRAY)) {
        fprintf(stderr,
                "ERROR: instproxy_browse returnd an invalid plist!\n");
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
    		free(uuid);
    	}
    	if (appid) {
    		free(appid);
    	}
    	if (options) {
    		free(options);
    	}
}




JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_uninstall
  (JNIEnv * env, jobject thiz, jstring uuid, jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_install
  (JNIEnv * env, jobject thiz, jstring uuid, jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_upgrade
  (JNIEnv * env, jobject thiz, jstring uuid, jstring appId){

}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_archive
  (JNIEnv * env, jobject thiz, jstring uuid, jstring appId, jboolean uninstall, jboolean appOnly, jstring destinationFolder){

}
