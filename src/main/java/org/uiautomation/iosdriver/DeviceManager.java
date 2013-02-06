package org.uiautomation.iosdriver;

import java.util.List;

public class DeviceManager {

  static {
    System.loadLibrary("devicemanager");
  }

  public native String[] getDeviceList();
  public native String getDeviceInfo(String uuid);


  public static void main(String[] args) {
    DeviceManager manager = new DeviceManager();
    for (String s : manager.getDeviceList()){
      System.out.println("found device : "+s);
    }
  }

}
