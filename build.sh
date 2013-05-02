#!/bin/bash
CC=gcc

JNILIB_NAME=libiosdriver
IOS_DRIVER_PATH=$(pwd)
BUILD_PATH=$IOS_DRIVER_PATH/build

MACHINE=$($CC -dumpmachine)

if [[ "$MACHINE" =~ "darwin" ]]
then
  JAVA_H="-I/System/Library/Frameworks/JavaVM.framework/Headers"
  SHAREDEXT=.dylib
  FLAGS=-dynamiclib
fi

if [[ "$MACHINE" =~ "linux" ]]
then
  JAVA_H="-I$JAVA_HOME/include -I$JAVA_HOME/include/linux"
  SHAREDEXT=.so
  FLAGS="-Wall -fPIC -shared -Wl,-soname,$JNILIB_NAME$SHAREDEXT -lc"
fi

STDHEADERS="-I/opt/local/include/ -I/usr/include"

LIBIMOBILEDEVICE_CFLAGS=$(pkg-config --cflags libimobiledevice-1.0)
LIBZIP_CFLAGS=$(pkg-config --cflags libzip)
LIBPLIST_CFLAGS=$(pkg-config --cflags libplist)
LIBUSBMUXD_CFAGS=$(pkg-config --cflags libusbmuxd)

TARGET_PATH=$IOS_DRIVER_PATH/src/main/resources/generated/
TARGET_NAME=$JNILIB_NAME$SHAREDEXT
TARGET=$TARGET_PATH$TARGET_NAME

rm -f $TARGET
mkdir -p $BUILD_PATH

$CC -fPIC ${JAVA_H} $STDHEADERS $LIBIMOBILEDEVICE_CFLAGS -c src/main/c/org_uiautomation_iosdriver_services_WebInspectorService.c -o $BUILD_PATH/a.o
$CC -fPIC ${JAVA_H} $STDHEADERS $LIBIMOBILEDEVICE_CFLAGS -c src/main/c/org_uiautomation_iosdriver_services_DeviceManagerService.c -o $BUILD_PATH/b.o
$CC -fPIC ${JAVA_H} $STDHEADERS $LIBIMOBILEDEVICE_CFLAGS ${LIBZIP_CFLAGS} -c src/main/c/org_uiautomation_iosdriver_services_DeviceInstallerService.c -o $BUILD_PATH/c.o
$CC -fPIC ${JAVA_H} $STDHEADERS $LIBIMOBILEDEVICE_CFLAGS -c src/main/c/org_uiautomation_iosdriver_services_LoggerService.c -o $BUILD_PATH/d.o
$CC -fPIC ${JAVA_H} $STDHEADERS $LIBIMOBILEDEVICE_CFLAGS -c src/main/c/org_uiautomation_iosdriver_services_InstrumentsService.c -o $BUILD_PATH/e.o

$CC $FLAGS \
  $(pkg-config --libs libusbmuxd) \
  $(pkg-config --libs libplist) \
  $(pkg-config --libs libimobiledevice-1.0) \
  $(pkg-config --libs libzip) \
  $BUILD_PATH/a.o $BUILD_PATH/b.o $BUILD_PATH/c.o $BUILD_PATH/d.o $BUILD_PATH/e.o \
  -o $TARGET
