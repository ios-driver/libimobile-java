package org.uiautomation.iosdriver.services.jnitools;

public class JNIService {

  static {
    System.load(JniLoader.getLibraryPath());
  }
}
