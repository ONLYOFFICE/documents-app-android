#!/bin/bash

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd $BASE_DIR

SECRETS=(
    "../../fastlane/.env.secret"
    "../../app_manager/google_api.json"
    "../../app_manager/Onlyoffice-keystore.properties"
    "../../app_manager/Onlyoffice.jks"
    "../../app_manager/appmanager/google-services.json"
    "../../app_manager/auth_strings.xml"
    "../../app_manager/Onlyoffice-725a90b8-184d-46a8-b196-6e40590f2d78.lic"
)

if [ -z "$GIT_ANDROID_DOCUMENTS_PASSPHRASE" ]
then
    echo "ERROR: Decrypt passphrase is not set"
    exit 1
fi

decrypt() {
    local file=$1
    echo "Create decrypt ${BASE_DIR}/$file"
    gpg --quiet --batch --yes --decrypt --passphrase="$GIT_ANDROID_DOCUMENTS_PASSPHRASE" --output "${BASE_DIR}/$file" "${BASE_DIR}/$file".gpg
}

for secret in ${SECRETS[@]}; do
    decrypt $secret
done

mv ../../app_manager/auth_strings.xml ../../toolkit/libtoolkit/src/main/res/values/auth_strings.xml
mv ../../app_manager/Onlyoffice-725a90b8-184d-46a8-b196-6e40590f2d78.lic ../../app_manager/appmanager/src/main/assets/Onlyoffice-725a90b8-184d-46a8-b196-6e40590f2d78.lic
