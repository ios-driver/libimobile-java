package org.uiautomation.iosdriver;

import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeviceInstallerService {

  private static final Logger log = Logger.getLogger(DeviceInstallerService.class.getName());
  private static final int LIST_ALL_APPS = 0;
  private static final int LIST_SYSTEM_APPS = 1;
  private static final int LIST_USER_APPS = 2;
  private final String uuid;


  static {
    System.loadLibrary("deviceinstallerservice");
  }

  public DeviceInstallerService(String uuid) {
    this.uuid = uuid;
  }

  private native String listApps(String uuid, int type);

  private native void uninstall(String uuid, String appId);

  private native void install(String uuid, String archive);

  private native void upgrade(String uuid, String archive);

  private native void archive(String uuid,
                              String appId,
                              boolean uninstall,
                              boolean appOnly,
                              String destinationFolder);

  public List<ApplicationInfo> listUserApps() {
    String rawXML = listApps(uuid, LIST_USER_APPS);
    return extractApplications(rawXML);
  }

  public List<ApplicationInfo> listSystemApps() {
    String rawXML = listApps(uuid, LIST_SYSTEM_APPS);
    return extractApplications(rawXML);
  }

  public List<ApplicationInfo> listAllApps() {
    String rawXML = listApps(uuid, LIST_ALL_APPS);
    return extractApplications(rawXML);
  }


  public static void main(String[] args) throws java.lang.Exception {
    List<ApplicationInfo> apps = new DeviceInstallerService("d1ce6333af579e27d166349dc8a1989503ba5b4f").listUserApps();
    System.out.println("found "+apps.size());
    for (ApplicationInfo info : apps){
      System.out.println(info);
    }

  }

  private List<ApplicationInfo> extractApplications(String rawXML) {
    List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
    NSArray apps = null;
    try {
      apps = (NSArray) XMLPropertyListParser.parse(rawXML.getBytes("UTF-8"));
    } catch (Exception e) {
      log.warning("Error parsing the xml returned : " + e.getMessage() + " , xml=\n" + rawXML);
      return infos;
    }
    for (int i = 0; i < apps.count(); i++) {
      NSObject app = apps.objectAtIndex(i);
      ApplicationInfo info = new ApplicationInfo(app);
      infos.add(info);
    }
    return infos;
  }

}
