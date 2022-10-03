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
    "../../app_manager/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic"
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
mv ../../app_manager/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic ../../app_manager/appmanager/src/main/assets/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic
