package org.uiautomation.iosdriver.services;

import com.google.common.collect.Lists;

import org.uiautomation.iosdriver.DeviceDetector;
import org.uiautomation.iosdriver.DeviceInfo;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DeviceManagerService extends JNIService {

  private DeviceDetector detector;
  private Thread listeningThread;
  private final Map<String, DeviceInfo> deviceByUuid = new HashMap<String, DeviceInfo>();
  private volatile boolean run = true;
  private volatile boolean running = false;
  private static final Logger log = Logger.getLogger(DeviceManagerService.class.getName());
  private static DeviceManagerService INSTANCE;


  // TODO freynaud change that
  public synchronized static DeviceManagerService create(DeviceDetector detector) {
    if (INSTANCE == null) {
      INSTANCE = new DeviceManagerService(detector);
      return INSTANCE;
    } else if (INSTANCE.running) {
      throw new IllegalStateException("Only one connection to USB is allowed.");
    } else {
      return INSTANCE;
    }
  }

  public static DeviceDetector getDetector() {
    if (INSTANCE == null) {
      return null;
    } else {
      return INSTANCE.detector;
    }
  }


  public synchronized static DeviceManagerService getInstance() {
    if (INSTANCE == null) {
      throw new IllegalStateException("You need to create the instance passing a detector first.");
    }
    return INSTANCE;
  }

  private DeviceManagerService(DeviceDetector detector) {
    this.detector = detector;
  }

  public native String[] getDeviceListNative();

  public native String getDeviceInfoNative(String uuid);

  private List<String> getDeviceList() {
    return Arrays.asList(getDeviceListNative());
  }

  private DeviceInfo getDeviceInfo(String uuid) {
    String xml = getDeviceInfoNative(uuid);
    try {
      // it is there for a reason.
      xml = new String(xml.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return new DeviceInfo(xml);
  }


  public synchronized void startDetection() {
    if (running) {
      log.warning("already running. Only 1 instance allowed.");
    }
    listeningThread = new Thread(new Runnable() {
      @Override
      public void run() {
        running = true;
        try {
          while (run) {
            List<String> connecteds = getDeviceList();
            List<String> previouslyConnecteds = Lists.newArrayList(deviceByUuid.keySet());

            for (String uuid : connecteds) {
              if (!deviceByUuid.containsKey(uuid)) {
                try {
                  DeviceInfo di = getDeviceInfo(uuid);
                  deviceByUuid.put(uuid, di);
                  detector.onDeviceAdded(di);
                } catch (Exception e) {
                  System.err.println("cannot read info," + e.getMessage());
                }
              }
              previouslyConnecteds.remove(uuid);
            }

            for (String uuid : previouslyConnecteds) {
              DeviceInfo di = deviceByUuid.remove(uuid);
              detector.onDeviceRemoved(di);
            }
          }

        } finally {
          running = false;
        }
      }
    });
    listeningThread.start();
  }

  public boolean stopDetection() {
    boolean res = running;
    run = false;
    while (running) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // ignore.
      }
      log.warning("waiting for the listener thread to finish.");
    }
    return res;
  }


  public static void main(String[] args) throws InterruptedException {
    LoggerService.disableDebug();
    DeviceDetector detector = new DeviceDetector() {
      @Override
      public void onDeviceAdded(DeviceInfo deviceInfo) {
        System.out.println(
            "added " + deviceInfo.getDeviceName() + " running " + deviceInfo.getProductVersion());
        System.out.println(deviceInfo.toString());
      }

      @Override
      public void onDeviceRemoved(DeviceInfo deviceInfo) {
        System.out.println("device unplugged :" + deviceInfo.getDeviceName());
      }
    };
    DeviceManagerService manager = DeviceManagerService.create(detector);
    manager.startDetection();
    Thread.sleep(1000);
    manager.stopDetection();

    //DeviceManagerService manager2 = DeviceManagerService.create(detector);
    //manager2.startDetection();

  }
}


