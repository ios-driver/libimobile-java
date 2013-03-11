package org.uiautomation.iosdriver.services.jnitools;

import org.uiautomation.iosdriver.services.LoggerService;

public class JNIService {

  static {
    System.load(JniLoader.getLibraryPath());
    LoggerService.disableDebug();
  }
}
