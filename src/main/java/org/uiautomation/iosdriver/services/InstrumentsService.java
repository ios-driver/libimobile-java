/*
 * Copyright (c) 2013 Martin Szulecki. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 */

package org.uiautomation.iosdriver.services;

import org.uiautomation.iosdriver.services.jnitools.*;

public class InstrumentsService extends JNIService {
  private final String uuid;

  public native void executeScriptNative(String uuid, String bundleIdentifier, String scriptBody, String[] environment, String[] arguments);

  public InstrumentsService(String uuid) {
    this.uuid = uuid;
  }

  public void executeScript(String bundleIdentifier, String scriptBody, String[] environment, String[] arguments) {
    executeScriptNative(uuid, bundleIdentifier, scriptBody, environment, arguments);
  }

  public static void main(String[] args) {
    InstrumentsService service = new InstrumentsService(null);
    String[] environment = new String[]{"UIAScript", "dummy.js"};
    String[] arguments = new String[]{"-D", "trace"};
    service.executeScript("com.ebay.iphone", "UIALogger.logMessage(\"Hello World!\");", environment, arguments);
  }
}
