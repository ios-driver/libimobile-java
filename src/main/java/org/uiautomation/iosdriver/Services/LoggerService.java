package org.uiautomation.iosdriver.services;


import org.uiautomation.iosdriver.services.jnitools.*;

public class LoggerService extends JNIService {

  private static native void setLogLevel(int level);

  public static void enableDebug() {
    setLogLevel(1);
  }

  public static void disableDebug() {
    setLogLevel(0);
  }
}
