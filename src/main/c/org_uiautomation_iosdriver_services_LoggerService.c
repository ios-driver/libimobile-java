#include <jni.h>

JNIEnv * jniEnv = NULL;
jclass loggerClass = NULL;

void logJava(const char *msg){
     jmethodID mLog = (*jniEnv)->GetStaticMethodID(jniEnv,loggerClass, "log","(Ljava/lang/String;)V");
     jstring s = (*jniEnv)->NewStringUTF(jniEnv, msg);
     (*jniEnv)->CallStaticVoidMethod(jniEnv,loggerClass, mLog,s);
}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_LoggerService_setLogLevel(JNIEnv * env, jclass clazz, jint level){
 idevice_set_debug_level((int)level);
  jniEnv = env;
  loggerClass = clazz;
  logJava("Hello");
}

