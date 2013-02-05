#!/bin/bash

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c org_uiautomation_iosdriver_WebInspector.c
gcc  -limobiledevice -dynamiclib -o libwebinspector.jnilib org_uiautomation_iosdriver_WebInspector.o
sudo mv libwebinspector.jnilib /usr/lib/java


gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c org_uiautomation_iosdriver_DeviceManager.c
gcc  -limobiledevice -dynamiclib -o libdevicemanager.jnilib org_uiautomation_iosdriver_DeviceManager.o
sudo mv libdevicemanager.jnilib /usr/lib/java