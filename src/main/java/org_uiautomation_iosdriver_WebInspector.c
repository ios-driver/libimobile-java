#include <jni.h>
#include <stdio.h>
#include "org_uiautomation_iosdriver_WebInspector.h"
#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/webinspector.h>

static webinspector_client_t client = 0;


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_start(JNIEnv * env, jobject thiz){
	printf("Start!\n");
    return;	
  }


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_stop(JNIEnv * env, jobject thiz){
  	printf("Stop!\n");
  	webinspector_client_free(client);
    return;	
  }

JNIEXPORT jstring JNICALL Java_org_uiautomation_iosdriver_WebInspector_receiveMessage(JNIEnv* env,   jobject thiz){
	jstring retval = (*env)->NewStringUTF(env, "test res");


  	return retval;
  }

JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_WebInspector_sendMessage(JNIEnv * env, jobject thiz, jstring command){
	char buf[50];
	const char *str = (*env)->GetStringUTFChars(env,command,0);

  	printf("got message : %s\n",str);
    return;	
  }


