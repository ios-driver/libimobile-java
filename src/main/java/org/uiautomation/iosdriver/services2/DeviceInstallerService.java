package org.uiautomation.iosdriver.services;

import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;

import org.uiautomation.iosdriver.ApplicationInfo;
import org.uiautomation.iosdriver.services.jnitools.JNILoggerListener;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeviceInstallerService extends JNIService {

  private static final Logger log = Logger.getLogger(DeviceInstallerService.class.getName());

  private final String uuid;

  private native String installNative(String[] args);

  public DeviceInstallerService(String uuid) {
    this.uuid = uuid;
  }

  public List<ApplicationInfo> getUserApplications() {
    return getApplicationList("list_user");
  }

  public List<ApplicationInfo> getAllApplications() {
    return getApplicationList("list_all");
  }

  public List<ApplicationInfo> getSystemApplications() {
    return getApplicationList("list_system");
  }


  private List<ApplicationInfo> getApplicationList(String type) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-l"
        , "-o", type
        , "-o", "xml"
    };
    String raw = installNative(args);
    return extractApplications(raw);
  }

  public void install(File ipa) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-i", ipa.getAbsolutePath()
    };
    installNative(args);
  }

  public void uninstall(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-u", bundleIdentifier
    };
    installNative(args);
  }

  public void restore(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-r", bundleIdentifier
    };
    installNative(args);
  }

  public void removeArchive(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-R", bundleIdentifier
    };
    installNative(args);
  }

  public void upgrade(String bundleIdentifier) {
    throw new RuntimeException("NI");
  }

  /**
   *
   * @param bundleIdentifier
   * @param andUninstall
   * @param archiveDataOnly
   * @param archiveBackup
   * @param andRemoveAfterBackup
   */
  public void archive(String bundleIdentifier, boolean andUninstall, boolean archiveDataOnly,
                      File archiveBackup, boolean andRemoveAfterBackup) {
    List<String> cmd = new ArrayList<String>();
    cmd.add("firstArgInC");
    cmd.add("-U");
    cmd.add(uuid);
    cmd.add("-a");
    cmd.add(bundleIdentifier);
    if (andUninstall) {
      cmd.add("-o");
      cmd.add("uninstall");
    }
    if (archiveDataOnly) {
      cmd.add("-o");
      cmd.add("app_only");
    }
    if (archiveBackup != null) {
      cmd.add("-o");
      cmd.add("copy=" + archiveBackup.getAbsolutePath());
      /*if (andRemoveAfterBackup) {
        cmd.add("-o");
        cmd.add("remove");
      }*/
    }

    installNative(cmd.toArray(new String[0]));
    if (andRemoveAfterBackup) {
      removeArchive(bundleIdentifier);
    }
  }

  public static void main(String[] args) {
    String app = "com.yourcompany.UICatalog";
    LoggerService.registerListener(new JNILoggerListener() {
      @Override
      public void onLog(int level, String message) {
        System.out.println("(" + level + ")" + " - " + message);
      }
    });
    args = new String[]{"bla", "-a", "com.yourcompany.UICatalog"};
    DeviceInstallerService
        service =
        new DeviceInstallerService("d1ce6333af579e27d166349dc8a1989503ba5b4f");
    //service.installNative(args);
    //

    //service.install(new File("/Users/freynaud/build/work/uicatalog.ipa"));
    //service.removeArchive(app);
    //service.archive(app,true,false,new File("/Users/freynaud/build/tmp"),true);
    //service.uninstall("com.yourcompany.UICatalog");
    //System.out.println(service.getSystemApplications());
    //System.out.println(service.getAllApplications());
    //service.removeArchive(app);
    //service.archive("com.yourcompany.UICatalog", true, false, new File("/Users/freynaud/build/tmp"),true);

    //System.out.println("INSTALL\n");
    //service.install(new File("/Users/freynaud/build/work/uicatalog.ipa"));
    System.out.println("LIST\n");
    System.out.println(service.getUserApplications());
    //System.out.println("UNINSTALL\n");
    //
    // service.uninstall(app);
    //service.install(new File("/Users/freynaud/build/work/uicatalog.ipa"));

    service.removeArchive(app);
    service
        .archive("com.yourcompany.UICatalog", false, false, new File("/Users/freynaud/build/tmp"),
                 false);
    service.removeArchive(app);
    service
        .archive("com.yourcompany.UICatalog", false, false, new File("/Users/freynaud/build/tmp2"),
                 false);

    //service.removeArchive(app);
    //service.archive("com.yourcompany.UICatalog", false, false, new File("/Users/freynaud/build/tmp"),true);

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
