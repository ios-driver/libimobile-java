package org.uiautomation.iosdriver;

public interface DeviceDetector {

  public void onDeviceAdded(DeviceInfo deviceInfo);

  public void onDeviceRemoved(DeviceInfo deviceInfo);
}
