#!/bin/bash

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c org_uiautomation_iosdriver_WebInspector.c
gcc  -limobiledevice -dynamiclib -o libwebinspector.jnilib org_uiautomation_iosdriver_WebInspector.o
sudo mv libwebinspector.jnilib /usr/lib/java


gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include  -c org_uiautomation_iosdriver_DeviceManager.c
gcc  -L/opt/local/lib -lplist -limobiledevice  -dynamiclib -o libdevicemanager.jnilib org_uiautomation_iosdriver_DeviceManager.o
sudo mv libdevicemanager.jnilib /usr/lib/java

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include -I/opt/local/lib/libzip/include  -c org_uiautomation_iosdriver_DeviceInstallerServicer.c
gcc  -L/opt/local/lib -lplist -lzip -limobiledevice  -dynamiclib -o libdeviceinstallerservice.jnilib org_uiautomation_iosdriver_DeviceInstallerServicer.o
sudo mv libdeviceinstallerservice.jnilib /usr/lib/java
