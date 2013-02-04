package org.uiautomation.iosdriver;

import java.io.UnsupportedEncodingException;

/**
 * ➜  java  gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -c
 * org_uiautomation_iosdriver_WebInspector.c ➜  java  gcc -dynamiclib -o libwebinspector.jnilib
 * org_uiautomation_iosdriver_WebInspector.o ➜  java  sudo mv libwebinspector.jnilib /usr/lib/java
 */
public class WebInspector {

  static {
    System.loadLibrary("webinspector");
  }

  public native void start();

  public native void stop();

  public String receiveMessage() throws UnsupportedEncodingException {
    return new String(receiveMessageImpl(), "UTF-8");
  }

  public void sendMessage(String xml){
    sendMessageImpl(xml.getBytes());
  }

  public native byte[] receiveMessageImpl();

  public native void sendMessageImpl(byte[] xml);


  public static void main(String[] args) {
    /*WebInspector inspector = new WebInspector();
    inspector.start();
    inspector.sendMessage("my xml");
    System.out.println(inspector.receiveMessage());
    inspector.stop();*/
  }
}
