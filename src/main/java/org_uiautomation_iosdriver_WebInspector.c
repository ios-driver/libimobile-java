#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "org_uiautomation_iosdriver_WebInspector.h"
#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/webinspector.h>

static webinspector_client_t client = NULL;
static idevice_t device = NULL;

JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_start(JNIEnv * env, jobject thiz, jstring uuid){
	if (IDEVICE_E_SUCCESS != idevice_new(&device, NULL)) {
        printf("No device found, is it plugged in?\n");
        return;
    }

    webinspector_error_t error = webinspector_start_service(device, &client);
    if (error != WEBINSPECTOR_E_SUCCESS) {
        printf("Could not connect to the webinspector! Error: %i\n", error);
        return;
    }

	printf("Started!\n");
    return;	
  }


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_stop(JNIEnv * env, jobject thiz, jstring uuid){
  	printf("Stop!\n");
  	webinspector_client_free(client);
  	idevice_free(device);
    return;	
  }

JNIEXPORT jstring JNICALL Java_org_uiautomation_iosdriver_WebInspector_receiveMessage(JNIEnv* env,   jobject thiz, jstring uuid,jint timeout_ms){

    char * buf = NULL;
    uint32_t length = 0;
    webinspector_error_t error = webinspector_receive_xml_plist(client, &buf, &length,timeout_ms);
    if (error != WEBINSPECTOR_E_SUCCESS || !buf || length == 0) {
        printf("> Error reading: %d\n", error);
    } else {
        printf("> %d bytes\n%s===== end of message =====\n\n", length, buf);
    }
    jstring retval = (*env)->NewStringUTF(env, buf);
    free(buf);

  	return retval;
  }

JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_sendMessage(JNIEnv * env, jobject thiz, jstring uuid,jstring command){
	const char * str = (*env)->GetStringUTFChars(env,command,0);
    webinspector_send_xml_plist(client, str, strlen(str));
    printf("Sent message : %s\n",str);
    (*env)->ReleaseStringUTFChars(env,command,str);
  	return;
  }


