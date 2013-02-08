package org.uiautomation.iosdriver;

public class DeviceInstallerService {

  private final String uuid;
  static {
    System.loadLibrary("deviceinstallerservice");
  }


  public DeviceInstallerService(String uuid) {
    this.uuid = uuid;
  }

  private native String[] listAllApps(String uuid);

  private native String[] listUserApps(String uuid);

  private native String[] listSystemApps(String uuid);

  private native void uninstall(String uuid, String appId);

  private native void install(String uuid, String archive);

  private native void upgrade(String uuid, String archive);

  private native void archive(String uuid,
                              String appId,
                              boolean uninstall,
                              boolean appOnly,
                              String destinationFolder);


  public static void main(String[] args){
    String s = "Application installed : "+new DeviceInstallerService("TODO").listAllApps("TODO");
    System.out.println(s);

  }

}
