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
    inspector.sendMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +"\n"+
                                  "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +"\n"+
                                  "<plist version=\"1.0\">" +"\n"+
                                  " <dict>" +"\n"+
                                  " <key>__argument</key>" +"\n"+
                                  " <dict>" +"\n"+
                                  " <key>WIRConnectionIdentifierKey</key>" +"\n"+
                                  " <string>9128c1d9-069d-4751-b45b-bbcb6e7e8591</string>" +"\n"+
                                  " </dict>" +"\n"+
                                  " <key>__selector</key>" +"\n"+
                                  " <string>_rpc_reportIdentifier:</string>" +"\n"+
                                  " </dict>" +"\n"+
                                  "</plist>" +"\n");
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());
    System.out.println(inspector.receiveMessage());


    inspector.stop();
  }
}
