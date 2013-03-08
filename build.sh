#!/bin/bash

VERSION="0.6-SNAPSHOT"
IOS_DRIVER="/Users/freynaud/Documents/workspace/ios-driver/"
JAVA_H="/System/Library/Frameworks/JavaVM.framework/Headers"
LIB_IMOBILE="/Users/freynaud/Documents/workspace/libimobiledevice/include"
MAC_PORT="/opt/local/include/"
LIB_ZIP="/opt/local/lib/libzip/include"
LIB_BPLIST="/Users/freynaud/Documents/workspace/libplist/build/"
LIB_USBMUXD="/Users/freynaud/imobile/usbmuxd-1.0.8/build/"


#mvn clean
rm -f src/main/resources/generated/libiosdriver.jnilib
#mkdir target

gcc  -I${JAVA_H} -I${MAC_PORT} -I${LIB_IMOBILE} -c src/main/c/org_uiautomation_iosdriver_services_WebInspectorService.c  -o target/a.o
gcc  -I${JAVA_H} -I${MAC_PORT} -I${LIB_IMOBILE}  -c src/main/c/org_uiautomation_iosdriver_services_DeviceManagerService.c  -o target/b.o
#gcc  -I${JAVA_H} -I${MAC_PORT} -I${LIB_IMOBILE} -I${LIB_ZIP}  -c src/main/c/org_uiautomation_iosdriver_services_DeviceInstallerService.c -o target/c.o
gcc  -I${JAVA_H} -I${MAC_PORT} -I${LIB_IMOBILE} -I${LIB_ZIP}  -c src/main/c/org_uiautomation_iosdriver_services_LibImobileDeviceInstallerService.c -o target/e.o

gcc  -I${JAVA_H} -I${MAC_PORT} -I${LIB_IMOBILE} -c src/main/c/org_uiautomation_iosdriver_services_LoggerService.c  -o target/d.o


#gcc   -L/opt/local/lib  -l plist -l zip -l imobiledevice -dynamiclib   -o src/main/resources/generated/libiosdriver.jnilib target/a.o target/b.o target/c.o


gcc -dynamiclib target/a.o target/b.o  target/d.o target/e.o \
  ${LIB_USBMUXD}libusbmuxd/CMakeFiles/libusbmuxd.dir/libusbmuxd.c.o \
  ${LIB_USBMUXD}libusbmuxd/CMakeFiles/libusbmuxd.dir/sock_stuff.c.o  \
  ${LIB_USBMUXD}libusbmuxd/CMakeFiles/libusbmuxd.dir/__/common/utils.c.o \
  ${LIB_BPLIST}libcnary/CMakeFiles/libcnary.dir/iterator.c.o \
  ${LIB_BPLIST}libcnary/CMakeFiles/libcnary.dir/list.c.o \
  ${LIB_BPLIST}libcnary/CMakeFiles/libcnary.dir/node.c.o \
  ${LIB_BPLIST}libcnary/CMakeFiles/libcnary.dir/node_iterator.c.o \
  ${LIB_BPLIST}libcnary/CMakeFiles/libcnary.dir/node_list.c.o \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/plist.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/hashtable.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/bytearray.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/ptrarray.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/bplist.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/base64.c.o  \
  ${LIB_BPLIST}src/CMakeFiles/plist.dir/xplist.c.o  \
  ${LIB_BPLIST}plutil/CMakeFiles/plutil.dir/plutil.c.o  \
  /usr/local/lib/libimobiledevice.a \
  /opt/local/lib/libxml2.a \
  /opt/local/lib/libiconv.a \
  /opt/local/lib/liblzma.a \
  /opt/local/lib/libzip.a \
  /opt/local/lib/libcrypto.a \
  /opt/local/lib/libz.a \
  /opt/local/lib/libssl.a \
  -o src/main/resources/generated/libiosdriver.jnilib


#rm -rf ~/.m2/repository/org/uiautomation/ios-driver-jni
#mvn package
#mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=target/ios-driver-jni-${VERSION}.jar \
#    -DgroupId=org.uiautomation \
#    -DartifactId=ios-driver-jni \
#    -Dversion=${VERSION} \
#    -Dpackaging=jar  \
#    -DlocalRepositoryPath=${IOS_DRIVER}server/libs  \



