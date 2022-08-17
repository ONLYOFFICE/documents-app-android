#!/bin/bash

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd $BASE_DIR

SECRETS=(
    "../../fastlane/.env.secret"
    "../../app_manager/google_api.json"
    "../../app_manager/Onlyoffice-keystore.properties"
    "../../app_manager/Onlyoffice.jks"
    "../../app_manager/appmanager/google-services.json"
    "../../toolkit/libtoolkit/src/main/res/values/auth_strings.xml"
    "../../app_manager/appmanager/src/main/assets/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic"
)

if [ -z "$GIT_ANDROID_DOCUMENTS_PASSPHRASE" ]
then
    echo "ERROR: Decrypt passphrase is not set"
    exit 1
fi

encrypt() {
    local file=$1
    echo "Create encrypt ${BASE_DIR}/$file.gpg"
    gpg --quiet --batch --yes --symmetric --cipher-algo AES256 --passphrase="$GIT_ANDROID_DOCUMENTS_PASSPHRASE" --output "${BASE_DIR}/$file".gpg "${BASE_DIR}/$file"
}

for secret in ${SECRETS[@]}; do
    encrypt $secret
done

mv ../../toolkit/libtoolkit/src/main/res/values/auth_strings.xml.gpg ../../app_manager/auth_strings.xml.gpg
mv ../../app_manager/appmanager/src/main/assets/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic.gpg ../../app_manager/onlyoffice_82491479-356e-4ee1-928d-f9870b6da6d4.lic.gpg