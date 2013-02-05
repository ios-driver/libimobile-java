#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "org_uiautomation_iosdriver_DeviceManager.h"
#include <libimobiledevice/libimobiledevice.h>


static idevice_t device = NULL;

void throwException(JNIEnv *env,char * msg){
    (*env)->ThrowNew(env, (*env)->FindClass(env, "org/uiautomation/iosdriver/Exception"), msg);
}
JNIEXPORT jobject JNICALL Java_org_uiautomation_iosdriver_DeviceManager_getDeviceList  (JNIEnv *env, jobject thiz) {
    int i;
    char **dev_list = NULL;

    if (idevice_get_device_list(&dev_list, &i) < 0) {
        throwException(env,"ERROR: Unable to retrieve device list!");
    }

    jobjectArray ret;
    ret= (jobjectArray) (*env)->NewObjectArray(env, i, (*env)->FindClass(env,"java/lang/String"),(*env)->NewStringUTF(env,""));



    for (i = 0; dev_list[i] != NULL; i++) {
        (*env)->SetObjectArrayElement(env,ret,i,(*env)->NewStringUTF(env,dev_list[i]));
    }

    idevice_device_list_free(dev_list);





   // char *data[5]= {"A", "B", "C", "D", "E"};

    //ret= (jobjectArray) (*env)->NewObjectArray(env,5, (*env)->FindClass(env,"java/lang/String"),(*env)->NewStringUTF(env,""));

    //for(i=0;i<5;i++)  (*env)->SetObjectArrayElement(env,ret,i,(*env)->NewStringUTF(env,data[i]));
    return(ret);
}
