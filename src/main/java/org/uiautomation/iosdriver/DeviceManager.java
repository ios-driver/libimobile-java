package org.uiautomation.iosdriver;

import com.google.common.collect.Lists;

import java.lang.Exception;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManager {

  private final DeviceDetector detector;
  private Thread listeningThread;
  private final Map<String, DeviceInfo> deviceByUuid = new HashMap<String, DeviceInfo>();
  private volatile boolean run = true;

  static {
    System.loadLibrary("devicemanager");
  }


  public DeviceManager(DeviceDetector detector) {
    this.detector = detector;
  }

  public native String[] getDeviceListNative();

  public native String getDeviceInfoNative(String uuid);

  private List<String> getDeviceList() {
    return Arrays.asList(getDeviceListNative());
  }

  private DeviceInfo getDeviceInfo(String uuid) {
    return new DeviceInfo(getDeviceInfoNative(uuid));
  }


  public synchronized void startDetection() {

    listeningThread = new Thread(new Runnable() {
      @Override
      public void run() {
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
      }
    });
    listeningThread.start();
  }

  public void stopDetection() {
    run = false;
  }


  public static void main(String[] args) throws InterruptedException {
    DeviceManager manager = new DeviceManager(new DeviceDetector() {
      @Override
      public void onDeviceAdded(DeviceInfo deviceInfo) {
        System.out.println(
            "added " + deviceInfo.getDeviceName() + " running " + deviceInfo.getProductVersion());
      }

      @Override
      public void onDeviceRemoved(DeviceInfo deviceInfo) {
        System.out.println("device unplugged :" + deviceInfo.getDeviceName());
      }
    });

    manager.startDetection();

  }
}

