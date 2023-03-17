#!/bin/bash

build() {
  ./gradlew publishToGithub
}

echo Publish editors

chmod +x app_manager/gradlew
cd app_manager || exit 1
build

exit 0


