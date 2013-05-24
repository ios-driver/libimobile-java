package org.uiautomation.iosdriver;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


public class ApplicationInfo {

  private static final Logger log = Logger.getLogger(ApplicationInfo.class.getName());

  private final Map<String, Object> properties;


  public ApplicationInfo(NSObject app) {
    properties = cast(app);
  }

  public Object getProperty(String key) {
    return properties.get(key);
  }

  public Set<String> keySet() {
    return properties.keySet();

  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  private <T> T cast(NSObject value) {
    if(value == null){
      return null;
    } else if (value instanceof NSString) {
      return (T) value.toString();
    } else if (value instanceof NSNumber) {
      NSNumber number = (NSNumber) value;
      if (number.isInteger()) {
        return (T) new Integer(number.intValue());
      } else if (number.isBoolean()) {
        return (T) new Boolean(number.boolValue());
      } else {
        // TODO can be long, float or double
        return (T) new Float(number.floatValue());
      }
    } else if (value instanceof NSArray) {
      List<T> res = new ArrayList<T>();
      NSArray array = (NSArray) value;
      for (int i = 0; i < array.count(); i++) {
        res.add((T) cast(array.objectAtIndex(i)));
      }
      return (T) res;
    } else if (value instanceof NSDictionary) {
      Map<String, Object> res = new HashMap<String, Object>();
      for (String key : ((NSDictionary) value).allKeys()) {
        NSObject o = ((NSDictionary) value).objectForKey(key);
        res.put(key, cast(o));
      }
      return (T) res;
    } else {
      log.warning("Can't cast from " + value.getClass());
    }
    return null;
  }

  public String getApplicationId() {
    return (String) properties.get("CFBundleIdentifier");
  }

  @Override
  public String toString() {
    return getApplicationId() + ":\n" + properties.toString();
  }
}
