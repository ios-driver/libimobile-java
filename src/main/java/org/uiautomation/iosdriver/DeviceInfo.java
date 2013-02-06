package org.uiautomation.iosdriver;

import com.dd.plist.NSDictionary;
import com.dd.plist.XMLPropertyListParser;

import java.lang.Exception;

public class DeviceInfo {

  private final String raw;
  private String buildVersion;
  private String bluetoothAddress;
  private String boardId;
  private String cpuArchitecture;
  private String chipID;
  private String deviceClass;
  private String deviceColor;
  private String deviceName;
  private String firmwareVersion;
  private String hardwareModel;
  private String modelNumber;

  public String getBluetoothAddress() {
    return bluetoothAddress;
  }

  public String getBoardId() {
    return boardId;
  }

  public String getCpuArchitecture() {
    return cpuArchitecture;
  }

  public String getChipID() {
    return chipID;
  }

  public String getDeviceClass() {
    return deviceClass;
  }

  public String getDeviceColor() {
    return deviceColor;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public String getFirmwareVersion() {
    return firmwareVersion;
  }

  public String getHardwareModel() {
    return hardwareModel;
  }

  public String getModelNumber() {
    return modelNumber;
  }

  public String getProductType() {
    return productType;
  }

  public String getProductVersion() {
    return productVersion;
  }

  public String getUniqueDeviceID() {
    return uniqueDeviceID;
  }

  public String getWifiAddress() {
    return wifiAddress;
  }

  private String productType;
  private String productVersion;
  private String uniqueDeviceID;
  private String wifiAddress;


  // array of int
  private String SupportedDeviceFamilies;

  public DeviceInfo(String xml) {
    this.raw = xml;
    try {
      parse();
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse the device info xml", e);
    }

  }

  private void parse() throws java.lang.Exception {
    NSDictionary rootDict = (NSDictionary) XMLPropertyListParser.parse(raw.getBytes());
    buildVersion = rootDict.objectForKey("BuildVersion").toString();
    bluetoothAddress= rootDict.objectForKey("BluetoothAddress").toString();
    boardId= rootDict.objectForKey("BoardId").toString();
    cpuArchitecture= rootDict.objectForKey("CPUArchitecture").toString();
    chipID= rootDict.objectForKey("ChipID").toString();
    deviceClass= rootDict.objectForKey("DeviceClass").toString();
    deviceColor= rootDict.objectForKey("DeviceColor").toString();
    deviceName= rootDict.objectForKey("DeviceName").toString();
    firmwareVersion= rootDict.objectForKey("FirmwareVersion").toString();
    hardwareModel= rootDict.objectForKey("HardwareModel").toString();
    modelNumber= rootDict.objectForKey("ModelNumber").toString();
    productType= rootDict.objectForKey("ProductType").toString();
    productVersion= rootDict.objectForKey("ProductVersion").toString();
    uniqueDeviceID= rootDict.objectForKey("UniqueDeviceID").toString();
    wifiAddress= rootDict.objectForKey("WiFiAddress").toString();
  }

  public String getBuildVersion() {
    return buildVersion;
  }
}
