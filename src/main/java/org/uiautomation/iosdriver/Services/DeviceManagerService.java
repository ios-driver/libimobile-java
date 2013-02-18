package org.uiautomation.iosdriver.services;

import com.google.common.collect.Lists;

import org.uiautomation.iosdriver.DeviceDetector;
import org.uiautomation.iosdriver.DeviceInfo;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.io.UnsupportedEncodingException;
import java.lang.Exception;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManagerService extends JNIService {

  private final DeviceDetector detector;
  private Thread listeningThread;
  private final Map<String, DeviceInfo> deviceByUuid = new HashMap<String, DeviceInfo>();
  private volatile boolean run = true;




  public DeviceManagerService(DeviceDetector detector) {
    this.detector = detector;
  }

  public native String[] getDeviceListNative();

  public native String getDeviceInfoNative(String uuid);

  private List<String> getDeviceList() {
    return Arrays.asList(getDeviceListNative());
  }

  private DeviceInfo getDeviceInfo(String uuid) {
    String xml =  getDeviceInfoNative(uuid);
    try {
      // it is there for a reason.
      xml = new String(xml.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return new DeviceInfo(xml);
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
    DeviceManagerService manager = new DeviceManagerService(new DeviceDetector() {
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
    });

    manager.startDetection();

  }
}

