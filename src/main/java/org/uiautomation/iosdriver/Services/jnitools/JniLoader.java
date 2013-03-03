package org.uiautomation.iosdriver.services.jnitools;

import org.apache.commons.io.IOUtils;
import org.uiautomation.iosdriver.IOSDriverServiceException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class JniLoader {

  private static final JniLoader INSTANCE = new JniLoader();
  private final File path;
  private static final Logger log = Logger.getLogger(JniLoader.class.getName());
  private final String platform;


  private JniLoader() {
    path = extractFromJar();
    platform = System.getProperty("os.name");
  }

  private String getResource(String platform) {
    String lib = "/generated/libiosdriver.jnilib";
    System.out.println("loading lib " + lib);
    return lib;
  }

  private File extractFromJar() {
    InputStream is = JniLoader.class.getResourceAsStream(getResource(platform));

    if (is == null) {
      String msg = "Cannot find " + getResource(platform) + " as a pre-generated artefact.";
      System.err.println(msg);
      throw new IOSDriverServiceException(msg);
    }
    File f = null;
    try {
      f = File.createTempFile("libiosdriver", ".jnilib");
      FileOutputStream os = new FileOutputStream(f);
      IOUtils.copy(is, os);
      IOUtils.closeQuietly(is);
      IOUtils.closeQuietly(os);
      f.deleteOnExit();

    } catch (IOException e) {
      throw new RuntimeException("Cannot load the jni native lib " + e.getMessage(), e);
    }
    return f;
  }

  public  static String getLibraryPath() {
    return INSTANCE.path.getAbsolutePath();
  }

  public static void load(){
  }




}
