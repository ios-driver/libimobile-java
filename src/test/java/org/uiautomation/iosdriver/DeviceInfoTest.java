package org.uiautomation.iosdriver;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

public class DeviceInfoTest {

  @Test
  public void test() throws IOException {
    InputStream in = DeviceInfo.class.getClassLoader().getResourceAsStream("deviceinfo.xml");
    String xml = IOUtils.toString(in);
    DeviceInfo deviceInfo = new DeviceInfo(xml);
    Assert.assertEquals(deviceInfo.getBuildVersion(), "10B144");
    Assert.assertEquals(deviceInfo.getBluetoothAddress(), "58:55:ca:78:76:a5");
    Assert.assertEquals(deviceInfo.getBoardId(), "8");
    Assert.assertEquals(deviceInfo.getCpuArchitecture(), "armv7");
    Assert.assertEquals(deviceInfo.getChipID(), "35120");
    Assert.assertEquals(deviceInfo.getDeviceClass(), "iPod");
    Assert.assertEquals(deviceInfo.getDeviceColor(), "white");
    Assert.assertEquals(deviceInfo.getDeviceName(), "iPod touch");
    Assert.assertEquals(deviceInfo.getFirmwareVersion(), "iBoot-1537.9.55");
    Assert.assertEquals(deviceInfo.getHardwareModel(), "N81AP");
    Assert.assertEquals(deviceInfo.getModelNumber(), "MC540");
    Assert.assertEquals(deviceInfo.getProductType(), "iPod4,1");
    Assert.assertEquals(deviceInfo.getProductVersion(), "6.1");
    Assert.assertEquals(deviceInfo.getWifiAddress(), "58:55:ca:71:5e:2e");

  }


}
