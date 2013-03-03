#include <jni.h>


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_LoggerService_setLogLevel(JNIEnv * env, jclass clazz, jint level){
 idevice_set_debug_level((int)level);
}