#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "org_uiautomation_iosdriver_services_WebInspectorService.h"
#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/webinspector.h>

static webinspector_client_t client = NULL;
static idevice_t device = NULL;

JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_WebInspectorService_start(JNIEnv * env, jobject thiz, jstring j_uuid){
    char* uuid = (char*)(*env)->GetStringUTFChars(env,j_uuid,NULL);
	if (IDEVICE_E_SUCCESS != idevice_new(&device, uuid)) {
        printf("No device found, is it plugged in?\n");
        return;
    }

    webinspector_error_t error = webinspector_client_start_service(device, &client, "libimobile-java");
    if (error != WEBINSPECTOR_E_SUCCESS) {
        printf("Could not connect to the webinspector! Error: %i\n", error);
        return;
    }

	printf("WebInspector started!\n");
    return;	
  }


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_WebInspectorService_stop(JNIEnv * env, jobject thiz, jstring uuid){
  	printf("WebInspector stopped!\n");
  	webinspector_client_free(client);
  	idevice_free(device);
    return;	
  }

JNIEXPORT jstring JNICALL Java_org_uiautomation_iosdriver_services_WebInspectorService_receiveMessage(JNIEnv * env, jobject thiz, jstring uuid, jint timeout_ms){
	plist_t plist = NULL;

	webinspector_error_t res = webinspector_receive_with_timeout(client, &plist, timeout_ms);
	if (res != WEBINSPECTOR_E_SUCCESS || plist == NULL) {
		printf("Error receiving.\n");
		plist_free(plist);
		return NULL;
	}

	char * xml = NULL;
	int xmllength = 0;
	plist_to_xml(plist, &xml, &xmllength);
	plist_free(plist);
	
	if (xml == NULL) {
		printf("Error converting plist to xml.\n");
		return NULL;
	}

	//printf("> %d bytes\n%s===== end of message =====\n\n", xmllength, xml);
    jstring retval = (*env)->NewStringUTF(env, xml);
    free(xml);

  	return retval;
}

JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_WebInspectorService_sendMessage(JNIEnv * env, jobject thiz, jstring uuid, jstring command){
	char const * const xml = (*env)->GetStringUTFChars(env, command, 0);
	int const xmllength = strlen(xml);

	plist_t plist = NULL;
	plist_from_xml(xml, xmllength, &plist);
	if (!plist) {
		printf("Failed to create plist from xml.\n");
		return;
	}

	webinspector_send(client, plist);
	plist_free(plist);

    //printf("Sent message : %s\n", xml);
    (*env)->ReleaseStringUTFChars(env, command, xml);
  	return;
}
