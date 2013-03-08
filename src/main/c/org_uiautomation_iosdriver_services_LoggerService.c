#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>


static JavaVM *jvm;

void logJava(const char *format, ...){
  va_list args;
  char *msg = NULL;

  va_start(args, format);
  (void)vasprintf(&msg, format, args);
  va_end(args);

  // should come from the context and be set in the service contructor.
  char * logId="[uniqueLogTODO]";
  char * new_str ;
  if((new_str = malloc(strlen(msg)+strlen(logId)+1)) != NULL){
      new_str[0] = '\0';   // ensures the memory is an empty string
      strcat(new_str,logId);
      strcat(new_str,msg);
  }

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
  jstring s = (*env)->NewStringUTF(env, new_str);
  (*env)->CallStaticVoidMethod(env,clazz, mLog,s);
  free(msg);
}


JNIEXPORT void JNICALL Java_org_uiautomation_iosdriver_services_LoggerService_setLogLevel(JNIEnv * env, jclass clazz, jint level){
 idevice_set_debug_level((int)level);
 int status = (*env)->GetJavaVM(env, &jvm);
     if(status != 0) {
         printf("failed storing the JVM instance.");
     }
}

