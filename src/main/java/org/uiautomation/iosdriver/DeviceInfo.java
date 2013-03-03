package org.uiautomation.iosdriver;

import com.dd.plist.NSArray;
import com.dd.plist.NSData;
import com.dd.plist.NSDate;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DeviceInfo {

  private final String raw;
  private String buildVersion;
  private String bluetoothAddress;
  private String boardId;
  private String cpuArchitecture;
  private String chipID;
  private String deviceClass;
  private String deviceColor;
  private String productType;
  private String productVersion;
  private String uniqueDeviceID;
  private String wifiAddress;
  private String deviceName;
  private String firmwareVersion;
  private String hardwareModel;
  private String modelNumber;
  // array of int
  private String SupportedDeviceFamilies;

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


  public DeviceInfo(String xml) {
    this.raw = xml;
    try {
      parse();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Cannot parse the device info xml " + e.getMessage(), e);
    }

  }

  private void parse() throws java.lang.Exception {
    byte[] xml = raw.getBytes("UTF-8");
    NSDictionary rootDict = (NSDictionary) MyParser.parse(xml);
    buildVersion = rootDict.objectForKey("BuildVersion").toString();

    bluetoothAddress = get(rootDict,"BluetoothAddress");
    boardId = get(rootDict,"BoardId");
    cpuArchitecture = get(rootDict,"CPUArchitecture");
    chipID = get(rootDict,"ChipID");
    deviceClass = get(rootDict,"DeviceClass");
    deviceColor = get(rootDict,"DeviceColor");
    deviceName = get(rootDict,"DeviceName");
    firmwareVersion = get(rootDict,"FirmwareVersion");
    hardwareModel = get(rootDict,"HardwareModel");
    modelNumber = get(rootDict,"ModelNumber");
    productType = get(rootDict,"ProductType");
    productVersion = get(rootDict,"ProductVersion");
    uniqueDeviceID = get(rootDict,"UniqueDeviceID");
    wifiAddress = get(rootDict,"WiFiAddress");

  }

  private String get(NSDictionary rootDict, String key) {
    if (rootDict.objectForKey(key) != null) {
      return rootDict.objectForKey(key).toString();
    } else {
      System.out.println("key "+key+" is null.");
      return null;
    }
  }


  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("DeviceInfo");
    sb.append("{buildVersion='").append(buildVersion).append('\'');
    sb.append(", cpuArchitecture='").append(cpuArchitecture).append('\'');
    sb.append(", deviceClass='").append(deviceClass).append('\'');
    sb.append(", deviceName='").append(deviceName).append('\'');
    sb.append(", productType='").append(productType).append('\'');
    sb.append(", productVersion='").append(productVersion).append('\'');
    sb.append(", uniqueDeviceID='").append(uniqueDeviceID).append('\'');
    sb.append('}');
    return sb.toString();
  }


  public String getBuildVersion() {
    return buildVersion;
  }


}


class MyParser {

  /**
   * Objects are unneccesary.
   */
  private MyParser() {
    /** empty **/
  }

  private static DocumentBuilderFactory docBuilderFactory = null;

  /**
   * Initialize the document builder factory so that it can be reuused and does not need to be
   * reinitialized for each new parsing.
   *
   * @throws javax.xml.parsers.ParserConfigurationException
   *          If the parser configuration is not supported on your system.
   */
  private static synchronized void initDocBuilderFactory() throws ParserConfigurationException {
    docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setIgnoringComments(true);
    docBuilderFactory.setCoalescing(true);
  }

  /**
   * Gets a DocumentBuilder to parse a XML property list. As DocumentBuilders are not thread-safe a
   * new DocBuilder is generated for each request.
   *
   * @return A new DocBuilder that can parse property lists w/o an internet connection.
   */
  private static synchronized DocumentBuilder getDocBuilder() throws ParserConfigurationException {
    if (docBuilderFactory == null) {
      initDocBuilderFactory();
    }
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    docBuilder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(String publicId, String systemId) {
        if ("-//Apple Computer//DTD PLIST 1.0//EN".equals(publicId) || // older publicId
            "-//Apple//DTD PLIST 1.0//EN".equals(publicId)) { // newer publicId
          // return a dummy, zero length DTD so we don't have to fetch
          // it from the network.
          return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
        return null;
      }
    });
    return docBuilder;
  }

  /**
   * Parses a XML property list file.
   *
   * @param f The XML property list file.
   * @return The root object of the property list. This is usally a NSDictionary but can also be a
   *         NSArray.
   * @throws Exception When an error occurs during parsing.
   * @see javax.xml.parsers.DocumentBuilder#parse(java.io.File)
   */
  public static NSObject parse(File f) throws Exception {
    DocumentBuilder docBuilder = getDocBuilder();

    Document doc = docBuilder.parse(f);

    return parseDocument(doc);
  }

  /**
   * Parses a XML property list from a byte array.
   *
   * @param bytes The byte array containing the property list's data.
   * @return The root object of the property list. This is usally a NSDictionary but can also be a
   *         NSArray.
   * @throws Exception When an error occurs during parsing.
   */
  public static NSObject parse(final byte[] bytes) throws Exception {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    return parse(bis);
  }

  /**
   * Parses a XML property list from an input stream.
   *
   * @param is The input stream pointing to the property list's data.
   * @return The root object of the property list. This is usally a NSDictionary but can also be a
   *         NSArray.
   * @throws Exception When an error occurs during parsing.
   * @see javax.xml.parsers.DocumentBuilder#parse(java.io.InputStream)
   */
  public static NSObject parse(InputStream is) throws Exception {
    DocumentBuilder docBuilder = getDocBuilder();

    Document doc = docBuilder.parse(is);

    return parseDocument(doc);
  }

  /**
   * Parses the XML document by generating the appropriate NSObjects for each XML node.
   *
   * @param doc The XML document.
   * @return The root NSObject of the property list contained in the XML document.
   * @throws Exception If an error occured during parsing.
   */
  private static NSObject parseDocument(Document doc) throws Exception {
    DocumentType docType = doc.getDoctype();
    if (docType == null) {
      if (!doc.getDocumentElement().getNodeName().equals("plist")) {
        throw new UnsupportedOperationException("The given XML document is not a property list.");
      }
    } else if (!docType.getName().equals("plist")) {
      throw new UnsupportedOperationException("The given XML document is not a property list.");
    }

    //Skip all #TEXT nodes and take the first element node we find as root
    List<Node> rootNodes = filterElementNodes(doc.getDocumentElement().getChildNodes());
    if (rootNodes.size() > 0) {
      return parseObject(rootNodes.get(0));
    } else {
      throw new Exception("No root node found!");
    }
  }

  /**
   * Parses a node in the XML structure and returns the corresponding NSObject
   *
   * @param n The XML node.
   * @return The corresponding NSObject.
   * @throws Exception If an error occured during parsing the node.
   */
  private static NSObject parseObject(Node n) throws Exception {
    String type = n.getNodeName();
    if (type.equals("dict")) {
      NSDictionary dict = new NSDictionary();
      List<Node> children = filterElementNodes(n.getChildNodes());
      for (int i = 0; i < children.size(); i += 2) {
        Node key = children.get(i);
        Node val = children.get(i + 1);

        String keyString = key.getChildNodes().item(0).getNodeValue();

        //Workaround for buggy behavior of the Android XML parser, which
        //separates certain Strings into several nodes
        for (int j = 1; j < key.getChildNodes().getLength(); j++) {
          keyString += key.getChildNodes().item(j).getNodeValue();
        }
        dict.put(keyString, parseObject(val));
      }
      return dict;
    } else if (type.equals("array")) {
      List<Node> children = filterElementNodes(n.getChildNodes());
      NSArray array = new NSArray(children.size());
      for (int i = 0; i < children.size(); i++) {
        array.setValue(i, parseObject(children.get(i)));
      }
      return array;
    } else if (type.equals("true")) {
      return new NSNumber(true);
    } else if (type.equals("false")) {
      return new NSNumber(false);
    } else if (type.equals("integer")) {
      return new NSNumber(n.getChildNodes().item(0).getNodeValue());
    } else if (type.equals("real")) {
      return new NSNumber(n.getChildNodes().item(0).getNodeValue());
    } else if (type.equals("string")) {
      NodeList children = n.getChildNodes();
      if (children.getLength() == 0) {
        return new NSString(""); //Empty string
      } else {
        String string = children.item(0).getNodeValue();
        //Workaround for buggy behavior of the Android XML parser, which
        //separates certain Strings into several nodes
        for (int i = 1; i < children.getLength(); i++) {
          string += children.item(i).getNodeValue();
        }
        return new NSString(string);
      }
    } else if (type.equals("data")) {
      if (n.getChildNodes().getLength() > 0) {
        return new NSData(n.getChildNodes().item(0).getNodeValue());
      } else {
        return new NSData((byte[]) null);
      }

    } else if (type.equals("date")) {
      return new NSDate(n.getChildNodes().item(0).getNodeValue());
    }
    return null;
  }

  /**
   * Returns all element nodes that are contained in a list of nodes.
   *
   * @param list The list of nodes to search.
   * @return The sublist containing only nodes representing actual elements.
   */
  private static List<Node> filterElementNodes(NodeList list) {
    List<Node> result = new ArrayList<Node>(list.getLength());
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
        result.add(list.item(i));
      }
    }
    return result;
  }
}

