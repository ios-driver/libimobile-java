package org.uiautomation.iosdriver;

import java.io.UnsupportedEncodingException;

/**
 * ➜  java git:(master) ✗ gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c org_uiautomation_iosdriver_WebInspector.c
 * ➜  javagit:(master) ✗ gcc  -limobiledevicec -dynamiclib -o libwebinspector.jnilib org_uiautomation_iosdriver_WebInspector.o
 * ➜  java  sudo mv libwebinspector.jnilib /usr/lib/java
 */
public class WebInspector {

  static {
    System.loadLibrary("webinspector");
  }

  public native void start();

  public native void stop();

  public native String receiveMessage();
  public native void sendMessage(String xml);




  public static void main(String[] args) {
    WebInspector inspector = new WebInspector();
    inspector.start();
    inspector.sendMessage("my xml");
    System.out.println(inspector.receiveMessage());
    inspector.stop();
  }
}
