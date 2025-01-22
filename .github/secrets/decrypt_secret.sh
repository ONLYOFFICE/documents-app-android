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
    "../../app_manager/Onlyoffice-182ba7a9-5335-449e-9c63-2585101e45bc.lic"
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
mv ../../app_manager/Onlyoffice-182ba7a9-5335-449e-9c63-2585101e45bc.lic ../../app_manager/appmanager/src/main/assets/Onlyoffice-182ba7a9-5335-449e-9c63-2585101e45bc.lic
