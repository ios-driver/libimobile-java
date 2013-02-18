package org.uiautomation.iosdriver.services;

import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import org.uiautomation.iosdriver.*;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeviceInstallerService extends JNIService {

  private static final Logger log = Logger.getLogger(DeviceInstallerService.class.getName());
  private static final int LIST_ALL_APPS = 0;
  private static final int LIST_SYSTEM_APPS = 1;
  private static final int LIST_USER_APPS = 2;

  public String getUuid() {
    return uuid;
  }

  private final String uuid;




  public DeviceInstallerService(String uuid) {
    this.uuid = uuid;
  }

  private native String listApps(int type);

  private native void uninstall(String appId);

  private native void install(String archive);

  private native void upgrade(String archive);

  private native void archive(String appId,
                              int uninstall,
                              int appOnly,
                              String destinationFolder);

  public List<ApplicationInfo> listUserApps() {
    String rawXML = listApps( LIST_USER_APPS);
    return extractApplications(rawXML);
  }

  public List<ApplicationInfo> listSystemApps() {
    String rawXML = listApps( LIST_SYSTEM_APPS);
    return extractApplications(rawXML);
  }

  public List<ApplicationInfo> listAllApps() {
    String rawXML = listApps( LIST_ALL_APPS);
    return extractApplications(rawXML);
  }


  public static void main(String[] args) throws java.lang.Exception {
    DeviceInstallerService service = new DeviceInstallerService("d1ce6333af579e27d166349dc8a1989503ba5b4f");
    List<ApplicationInfo> apps = service.listUserApps();


    for (ApplicationInfo info : apps){
      System.out.println("archive : "+ info);
      service.archive(info.getApplicationId(),1,0,"/Users/freynaud/build/archived");
    }
    service.install("/Users/freynaud/build/archived/com.yourcompany.UICatalog.ipa");
    //service.archive("com.yourcompany.UICatalog",0,0,"/Users/freynaud/build/archived");




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
