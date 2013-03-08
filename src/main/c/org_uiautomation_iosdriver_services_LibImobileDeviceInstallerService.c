#include <jni.h>
#include "org_uiautomation_iosdriver_services_LibImobileDeviceInstallerService.h"


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


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_LibImobileDeviceInstallerService_installNative(JNIEnv * env, jobject instance, jobjectArray stringArray){
   printf("A\n");

   int stringCount = (*env)->GetArrayLength(env, stringArray);
   printf("SIZE : %i\n",stringCount);
   int i;
   char **args = (char **)malloc(stringCount*sizeof(char *));
   for (i=0; i<stringCount; i++) {
           jstring string = (jstring) (*env)->GetObjectArrayElement(env, stringArray, i);
           const char *rawString = (*env)->GetStringUTFChars(env, string, 0);
           args[i] = (char*)rawString;
           printf("got %s",args[i]);
    }



}