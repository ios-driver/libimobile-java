package org.uiautomation.iosdriver;

/**
 * ➜  java git:(master) ✗ gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers
 * -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c
 * org_uiautomation_iosdriver_WebInspector.c ➜  javagit:(master) ✗ gcc  -limobiledevicec -dynamiclib
 * -o libwebinspector.jnilib org_uiautomation_iosdriver_WebInspector.o ➜  java  sudo mv
 * libwebinspector.jnilib /usr/lib/java
 */
public class WebInspector {

  static {
    System.loadLibrary("webinspector");
  }

  private final String uuid;

  private native void start(String uuid);

  private native void stop(String uuid);

  private native String receiveMessage(String uuid,Integer timeout_ms);

  private native void sendMessage(String uuid, String xml);


  public WebInspector(String uuid) {
    this.uuid = uuid;
  }


  public void start() {
    System.out.println("start");
    start(uuid);
  }

  public void stop() {
    System.out.println("stop");
    stop(uuid);
  }

  public String receiveMessage() {
    return receiveMessage(uuid,1000);
  }

  public void sendMessage(String message) {
    sendMessage(uuid, message);
  }


  public static void main2(String[] args) {
    final WebInspector inspector = new WebInspector("d1ce6333af579e27d166349dc8a1989503ba5b4f");
    inspector.start();
    inspector.receiveMessage();

    /*
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          inspector.receiveMessage();
        }
      }
    }).start();

    inspector.stop();
    inspector.start();*/

  }


  public static void main(String[] args) throws InterruptedException {
    final WebInspector inspector = new WebInspector("uuid");
    inspector.start();
    final Monitor m = new Monitor();
    new Thread(new Runnable() {
      @Override
      public void run() {
        m.run = true;
        m.clean = false;
        while (m.run) {
          String msg =inspector.receiveMessage();
          if (msg!=null){
            System.out.println(msg);
          }
          if (m.run == false) {
            m.clean = true;
          }
        }
      }
    }).start();
    //Thread.sleep(2000);

    inspector.sendMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
                          "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"
                          + "\n" +
                          "<plist version=\"1.0\">" + "\n" +
                          " <dict>" + "\n" +
                          " <key>__argument</key>" + "\n" +
                          " <dict>" + "\n" +
                          " <key>WIRConnectionIdentifierKey</key>" + "\n" +
                          " <string>9128c1d9-069d-4751-b45b-bbcb6e7e8591</string>" + "\n" +
                          " </dict>" + "\n" +
                          " <key>__selector</key>" + "\n" +
                          " <string>_rpc_reportIdentifier:</string>" + "\n" +
                          " </dict>" + "\n" +
                          "</plist>" + "\n");

    m.run = false;
    while (m.clean == false) {
      //System.out.println("waiting");
      Thread.sleep(25);
    }
    inspector.stop();
    System.out.println("Starting 2");
    inspector.start();
    System.out.println("Started");
    new Thread(new Runnable() {
      @Override
      public void run() {
        m.run = true;
        m.clean = false;
        while (m.run) {
          String msg =inspector.receiveMessage();
          if (msg!=null){
            System.out.println(msg);
          }
          if (m.run == false) {
            m.clean = true;
          }
        }
      }
    }).start();
    System.out.println("Thread 2 started");
    inspector.sendMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
                          "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"
                          + "\n" +
                          "<plist version=\"1.0\">" + "\n" +
                          " <dict>" + "\n" +
                          " <key>__argument</key>" + "\n" +
                          " <dict>" + "\n" +
                          " <key>WIRConnectionIdentifierKey</key>" + "\n" +
                          " <string>9128c1d9-069d-4751-b45b-bbcb6e7e8592</string>" + "\n" +
                          " </dict>" + "\n" +
                          " <key>__selector</key>" + "\n" +
                          " <string>_rpc_reportIdentifier:</string>" + "\n" +
                          " </dict>" + "\n" +
                          "</plist>" + "\n");
    System.out.println("Message sent 2");

    m.run = false;
    while (m.clean == false) {
      //System.out.println("waiting");
      Thread.sleep(25);
    }
    inspector.stop();

  }
}

class Monitor {

  boolean run = true;
  boolean clean = true;
}