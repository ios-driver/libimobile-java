package org.uiautomation.iosdriver.services;


import org.uiautomation.iosdriver.services.jnitools.JNILoggerListener;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoggerService extends JNIService {

  private static native void setLogLevel(int level);

  private static List<JNILoggerListener> listeners;


  private synchronized static List<JNILoggerListener> getListeners() {
    if (listeners == null) {
      listeners = new CopyOnWriteArrayList<JNILoggerListener>();
    }
    return listeners;
  }

  public static void registerListener(JNILoggerListener listener) {
    getListeners().add(listener);
  }

  public static void enableDebug() {
    setLogLevel(1);
  }

  public static void disableDebug() {
    setLogLevel(0);
  }

  public static void log(int level, String s) {
    for (JNILoggerListener listener : getListeners()) {
      listener.onLog(level, s);
    }
  }

  public static void main(String[] args) {
    JNILoggerListener listener = new JNILoggerListener() {
      public void onLog(int level, String message) {
        System.out.println("[" + level + "]" + message + "\n");
      }
    };
    LoggerService.registerListener(listener);
    setLogLevel(1);
  }

}
