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



static idevice_t device = NULL;


JNIEXPORT jobjectArray JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_listAllApps
  (JNIEnv * env, jobject thiz, jstring uuid){

 }


JNIEXPORT jobjectArray JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_listUserApps
  (JNIEnv * env, jobject thiz, jstring uuid){

}


JNIEXPORT jobjectArray JNICALL Java_org_uiautomation_iosdriver_DeviceInstallerService_listSystemApps
  (JNIEnv * env, jobject thiz, jstring uuid){

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
