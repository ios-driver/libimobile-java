#include <jni.h>

static JavaVM *jvm;

void logJava(const char *msg){
  printf("logJava : %s",msg);
  JNIEnv *env;
  if (jvm ==NULL){
     printf("Initialize by calling LogService.disabledebug() first..\n");
  }
  (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);

  if (env==NULL){
    printf("Need to specific the JNI env for logging.\n");
  }
  char * n = "org/uiautomation/iosdriver/services/LoggerService";
  jclass clazz = (*env)->FindClass(env, n);
  if (clazz==NULL){
          printf("%s can't be loaded in C. Class got renamed ?",n);
          return;
  }
  jmethodID mLog = (*env)->GetStaticMethodID(env,clazz, "log","(Ljava/lang/String;)V");
  if (mLog==NULL){
        printf("mLog NULL");
        return;
  }
  jstring s = (*env)->NewStringUTF(env, msg);
  (*env)->CallStaticVoidMethod(env,clazz, mLog,s);
}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_LoggerService_setLogLevel(JNIEnv * env, jclass clazz, jint level){
 idevice_set_debug_level((int)level);
 int status = (*env)->GetJavaVM(env, &jvm);
     if(status != 0) {
         printf("failed storing the JVM instance.");
     }
}

