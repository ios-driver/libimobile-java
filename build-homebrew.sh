#!/bin/bash

VERSION="0.6-SNAPSHOT"
IOS_DRIVER="$HOME/vcs/libs/ios-driver-beta"
JAVA_H="/System/Library/Frameworks/JavaVM.framework/Headers/"
LIB_IMOBILE="/usr/local/Cellar/libimobiledevice/1.1.5/include/libimobiledevice"

#mvn clean
rm -f src/main/resources/generated/libiosdriver.jnilib
#mkdir target

doCompile="gcc  -I${JAVA_H} -I${LIB_IMOBILE} -I/usr/local//Cellar/libzip/0.10.1/lib/libzip/include/"
$doCompile -I/usr/local/include -c src/main/c/org_uiautomation_iosdriver_services_WebInspectorService.c -o target/a.o
$doCompile -c src/main/c/org_uiautomation_iosdriver_services_DeviceManagerService.c -o target/b.o
$doCompile -c src/main/c/org_uiautomation_iosdriver_services_DeviceInstallerService.c -o target/c.o
$doCompile -c src/main/c/org_uiautomation_iosdriver_services_LoggerService.c  -o target/d.o

#gcc   -L/opt/local/lib  -l plist -l zip -l imobiledevice -dynamiclib   -o src/main/resources/generated/libiosdriver.jnilib target/a.o target/b.o target/c.o
gcc -dynamiclib target/a.o target/b.o  target/c.o target/d.o \
  /usr/local/Cellar/openssl/1.0.1e/lib/libssl.dylib \
  /usr/local/lib/libimobiledevice.dylib \
  /usr/local/lib/libplist.dylib \
  /usr/local/lib/libzip.dylib \
  -o src/main/resources/generated/libiosdriver.jnilib

exit

#rm -rf ~/.m2/repository/org/uiautomation/ios-driver-jni
#mvn package
#mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=target/ios-driver-jni-${VERSION}.jar \
#    -DgroupId=org.uiautomation \
#    -DartifactId=ios-driver-jni \
#    -Dversion=${VERSION} \
#    -Dpackaging=jar  \
#    -DlocalRepositoryPath=${IOS_DRIVER}server/libs  \



