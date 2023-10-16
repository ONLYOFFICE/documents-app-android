#!/bin/bash

build() {
  ./gradlew buildNativeSdkJs
  ./gradlew installManagerDebug
}

echo Build and install

chmod +x app_manager/gradlew
cd app_manager || exit 1
build

exit 0