package org.uiautomation.iosdriver.services;

import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;

import org.uiautomation.iosdriver.ApplicationInfo;
import org.uiautomation.iosdriver.services.jnitools.JNIService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeviceInstallerService extends JNIService {

  private static final Logger log = Logger.getLogger(DeviceInstallerService.class.getName());

  private final String uuid;


  private native String installNative(String[] args);

  private native void emptyApplicationCacheNative(String uuid, String bundleIdentifier);

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
    String raw = installNativeSafe(args);
    return extractApplications(raw);
  }


  public void install(File ipa) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-i", ipa.getAbsolutePath()
    };
    installNativeSafe(args);
  }

  public void uninstall(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-u", bundleIdentifier
    };
    installNativeSafe(args);
  }

  public void restore(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-r", bundleIdentifier
    };
    installNativeSafe(args);
  }

  public void removeArchive(String bundleIdentifier) {
    String[] args = new String[]{"firstArgInC"
        , "-U", uuid
        , "-R", bundleIdentifier
    };
    installNativeSafe(args);
  }

  public void upgrade(String bundleIdentifier) {
    throw new RuntimeException("NI");
  }

  private String installNativeSafe(String[] args) {
    boolean wasRunning = DeviceManagerService.getInstance().stopDetection();
    String res = installNative(args);
    if (wasRunning) {
      DeviceManagerService.getInstance().startDetection();
    }
    return res;
  }

  public void emptyApplicationCache(String bundleIdentifier) {
    boolean wasRunning = DeviceManagerService.getInstance().stopDetection();
    emptyApplicationCacheNative(uuid, bundleIdentifier);
    if (wasRunning) {
      DeviceManagerService.getInstance().startDetection();
    }
  }

  /**
   * @return the info about this bundle id for the device. Null is the app isn't installed.
   */
  public ApplicationInfo getApplication(String bundleId) {
    List<ApplicationInfo> all = getAllApplications();
    for (ApplicationInfo app : all) {
      if (bundleId.equals(app.getProperty("CFBundleIdentifier"))) {
        return app;
      }
    }
    return null;
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

    installNativeSafe(cmd.toArray(new String[0]));
    if (andRemoveAfterBackup) {
      removeArchive(bundleIdentifier);
    }
  }

  public static void main(String[] args) {

    DeviceInstallerService
        service =
        new DeviceInstallerService("d1ce6333af579e27d166349dc8a1989503ba5b4f");

    service.uninstall("com.yourcompany.UICatalog");
    service.install(new File(
        "/Users/freynaud/Documents/workspace/ios-driver/applications/com.yourcompany.UICatalog.ipa"));
    service.uninstall("com.yourcompany.UICatalog");
    service.install(new File(
        "/Users/freynaud/Documents/workspace/ios-driver/applications/com.yourcompany.UICatalog.ipa"));
  }


  private native void setLockDownValue(String uuid, String domain, String key, String value);

  /*
    <key>HostKeyboard</key>
    <string>de</string>
    <key>Keyboard</key>
    <string>de_DE</string>
    <key>Language</key>
    <string>pl</string>
    <key>Locale</key>
    <string>en_US</string>
    */
  public void setLocale(String locale) {
    setLockDownValue(uuid, "com.apple.international", "Locale", locale);
  }

  public void setLanguage(String language) {
    setLockDownValue(uuid, "com.apple.international", "Language", language);
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

  public String getDeviceId() {
    return this.uuid;
  }
}
