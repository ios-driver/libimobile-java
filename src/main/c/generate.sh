#!/bin/bash

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c org_uiautomation_iosdriver_WebInspector.c
#gcc  -L/opt/local/lib -lplist -lzip -L/opt/local/lib -L/usr/local/bin -limobiledevice -dynamiclib -o libwebinspector.jnilib org_uiautomation_iosdriver_WebInspector.o
#sudo mv libwebinspector.jnilib /usr/lib/java


gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include  -c org_uiautomation_iosdriver_DeviceManager.c
#gcc  -L/opt/local/lib -lplist -limobiledevice  -dynamiclib -o libdevicemanager.jnilib org_uiautomation_iosdriver_DeviceManager.o
#sudo mv libdevicemanager.jnilib /usr/lib/java

gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include -I/opt/local/lib/libzip/include  -c org_uiautomation_iosdriver_DeviceInstallerServicer.c
gcc  -L/opt/local/lib -lplist -lzip -limobiledevice  -dynamiclib -o libdeviceinstallerservice.jnilib org_uiautomation_iosdriver_DeviceInstallerServicer.o org_uiautomation_iosdriver_WebInspector.o org_uiautomation_iosdriver_DeviceManager.o
sudo mv libdeviceinstallerservice.jnilib /usr/lib/java/libiosdriver.jnilib

mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=/Users/freynaud/Documents/workspace/libimobile-java/target/libimobile-java-1.0-SNAPSHOT.jar -DgroupId=org.uiautomation -DartifactId=ios-driver-jni -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=/Users/freynaud/Documents/workspace/ios-driver/server/local-repo
