#!/bin/bash

#mvn clean
#mkdir target
gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include/ -I/Users/freynaud/Documents/workspace/libimobiledevice/include -c src/main/c/org_uiautomation_iosdriver_services_WebInspectorService.c  -o target/a.o
gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include  -c src/main/c/org_uiautomation_iosdriver_services_DeviceManagerService.c  -o target/b.o
gcc   -I/System/Library/Frameworks/JavaVM.framework/Headers -I/opt/local/include -I/Users/freynaud/Documents/workspace/libimobiledevice/include -I/opt/local/lib/libzip/include  -c src/main/c/org_uiautomation_iosdriver_services_DeviceInstallerService.c -o target/c.o
#gcc   -L/opt/local/lib  -l plist -l zip -l imobiledevice -dynamiclib   -o libiosdriver.jnilib target/a.o target/b.o target/c.o


gcc   -dynamiclib target/a.o target/b.o target/c.o  \
  /Users/freynaud/imobile/usbmuxd-1.0.8/build/libusbmuxd/CMakeFiles/libusbmuxd.dir/libusbmuxd.c.o \
  /Users/freynaud/imobile/usbmuxd-1.0.8/build/libusbmuxd/CMakeFiles/libusbmuxd.dir/sock_stuff.c.o  \
  /Users/freynaud/imobile/usbmuxd-1.0.8/build/libusbmuxd/CMakeFiles/libusbmuxd.dir/__/common/utils.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/libcnary/CMakeFiles/libcnary.dir/iterator.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/libcnary/CMakeFiles/libcnary.dir/list.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/libcnary/CMakeFiles/libcnary.dir/node.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/libcnary/CMakeFiles/libcnary.dir/node_iterator.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/libcnary/CMakeFiles/libcnary.dir/node_list.c.o \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/plist.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/hashtable.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/bytearray.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/ptrarray.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/bplist.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/base64.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/src/CMakeFiles/plist.dir/xplist.c.o  \
  /Users/freynaud/Documents/workspace/libplist/build/plutil/CMakeFiles/plutil.dir/plutil.c.o  \
  /Users/freynaud/Documents/workspace/libimobile-java/libimobiledevice.a \
  /opt/local/lib/libxml2.a \
  /opt/local/lib/libiconv.a \
  /opt/local/lib/liblzma.a \
  /opt/local/lib/libzip.a \
  /opt/local/lib/libcrypto.a \
  /opt/local/lib/libz.a \
  /opt/local/lib/libssl.a \
  -o libiosdriver_static.jnilib


mv libiosdriver_static.jnilib src/main/resources/generated/libiosdriver.jnilib

mvn package
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=target/ios-driver-jni-1.0-SNAPSHOT.jar -DgroupId=org.uiautomation -DartifactId=ios-driver-jni -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=/Users/freynaud/Documents/workspace/ios-driver/server/libs



