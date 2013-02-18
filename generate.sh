#!/bin/bash

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c src/main/c/org_uiautomation_iosdriver_WebInspector.c  -o target/a.o
gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include  -c src/main/c/org_uiautomation_iosdriver_DeviceManagerService.c  -o target/b.o
gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include -I/opt/local/lib/libzip/include  -c src/main/c/org_uiautomation_iosdriver_DeviceInstallerServicer.c -o target/c.o
gcc  -L/opt/local/lib -lplist -lzip -limobiledevice  -dynamiclib -o libiosdriver.jnilib target/a.o target/b.o target/c.o
sudo mv libiosdriver.jnilib src/main/resources/generated/libiosdriver.jnilib

