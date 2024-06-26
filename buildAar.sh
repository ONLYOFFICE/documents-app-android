#!/bin/bash

build() {
  ./gradlew buildAar
  cd ../
}

echo Build aar

chmod +x app_manager/gradlew
cd app_manager || exit 1
build

tar -cvf libs.tar libs

exit 0


