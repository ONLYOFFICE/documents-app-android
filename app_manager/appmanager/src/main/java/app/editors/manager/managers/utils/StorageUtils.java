package app.editors.manager.managers.utils;

import static app.documents.core.network.common.contracts.StorageContract.*;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import app.documents.core.network.common.contracts.ApiContract;
import app.editors.manager.R;
import lib.toolkit.base.managers.utils.StringUtils;

public class StorageUtils {

    @Nullable
    public static String getStorageUrl(final String providerKey, final String clientId, final String redirectUrl) {
        switch (providerKey) {
            case ApiContract.Storage.BOXNET:
                return box(providerKey, clientId, redirectUrl).getUrl();

            case ApiContract.Storage.DROPBOX:
                return dropBox(providerKey, clientId, redirectUrl).getUrl();

            case ApiContract.Storage.GOOGLEDRIVE:
                return google(providerKey, clientId, redirectUrl).getUrl();

            case ApiContract.Storage.ONEDRIVE:
                return oneDrive(providerKey, clientId, redirectUrl).getUrl();
        }

        return null;
    }

    public static @Nullable Integer getStorageTitle(@Nullable final String providerKey) {
        if (providerKey == null) {
            return null;
        }

        return switch (providerKey) {
            case ApiContract.Storage.BOXNET -> R.string.storage_select_box;
            case ApiContract.Storage.DROPBOX -> R.string.storage_select_drop_box;
            case ApiContract.Storage.SHAREPOINT -> R.string.storage_select_share_point;
            case ApiContract.Storage.GOOGLEDRIVE -> R.string.storage_select_google_drive;
            case ApiContract.Storage.ONEDRIVE -> R.string.storage_select_one_drive;
            case ApiContract.Storage.YANDEX -> R.string.storage_select_yandex;
            case ApiContract.Storage.WEBDAV -> R.string.storage_select_web_dav;
            default -> null;
        };
    }

    public static Integer getStorageIcon(String providerKey) {
        switch (providerKey) {
            case ApiContract.Storage.BOXNET:
                return R.drawable.ic_storage_box;
            case ApiContract.Storage.DROPBOX:
                return R.drawable.ic_storage_dropbox;
            case ApiContract.Storage.SHAREPOINT:
                return R.drawable.ic_storage_sharepoint;
            case ApiContract.Storage.GOOGLEDRIVE:
                return R.drawable.ic_storage_google;
            case ApiContract.Storage.ONEDRIVE:
            case ApiContract.Storage.SKYDRIVE:
                return R.drawable.ic_storage_onedrive;
            case ApiContract.Storage.YANDEX:
                return R.drawable.ic_storage_yandex;
            case ApiContract.Storage.KDRIVE:
                return R.drawable.ic_storage_kdrive;
            case ApiContract.Storage.NEXTCLOUD:
                return R.drawable.ic_storage_nextcloud;
            case ApiContract.Storage.OWNCLOUD:
                return R.drawable.ic_storage_owncloud;
            case ApiContract.Storage.WEBDAV:
                return R.drawable.ic_storage_webdav;
            default:
                return R.drawable.ic_type_folder;
        }
    }

    /*
     * Get Box instance for request token
     * */
    private static Storage box(final String providerKey, final String clientId, final String redirectUrl) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Box.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_CLIENT_ID, clientId);
        uriMap.put(ARG_REDIRECT_URI, redirectUrl);
        return new Storage(providerKey, Box.AUTH_URL, uriMap);
    }


    /*
     * Get DropBox instance for request token
     * */
    private static Storage dropBox(final String providerKey, final String clientId, final String redirectUrl) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, DropBox.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_CLIENT_ID, clientId);
        uriMap.put(ARG_REDIRECT_URI, redirectUrl);
        uriMap.put(ARG_TOKEN_ACCESS, DropBox.VALUE_ACCESS_TYPE);
        return new Storage(providerKey, DropBox.AUTH_URL, uriMap);
    }


    /*
     * Get Google instance for request token
     * */
    private static Storage google(final String providerKey, final String clientId, final String redirectUrl) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_APPROVAL_PROMPT, Google.VALUE_APPROVAL_PROMPT);
        uriMap.put(ARG_RESPONSE_TYPE, Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_ACCESS_TYPE, Google.VALUE_ACCESS_TYPE);
        uriMap.put(ARG_SCOPE, Google.VALUE_SCOPE);
        uriMap.put(ARG_CLIENT_ID, clientId);
        uriMap.put(ARG_REDIRECT_URI, redirectUrl);
        return new Storage(providerKey, Google.AUTH_URL, uriMap);
    }


    /*
     * Get OneDrive instance for request token
     * */
    private static Storage oneDrive(final String providerKey, final String clientId, final String redirectUrl) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_CLIENT_ID, clientId);
        uriMap.put(ARG_REDIRECT_URI, redirectUrl);
        uriMap.put(ARG_RESPONSE_TYPE, OneDrive.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_SCOPE, OneDrive.VALUE_SCOPE);
        return new Storage(providerKey, OneDrive.AUTH_URL, uriMap);
    }


    /*
     * Storage info
     * */
    private static final class Storage implements Serializable {

        public final String mProviderKey;
        public final String mRequestUrl;
        public final TreeMap<String, String> mRequestArgs;

        public Storage(final String providerKey, final String url, final TreeMap<String, String> requestArgs) {
            mProviderKey = providerKey;
            mRequestUrl = url;
            mRequestArgs = requestArgs;
        }

        public String getUrl() {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(mRequestUrl);
            for (Map.Entry<String, String> item : mRequestArgs.entrySet()) {
                stringBuilder.append(item.getKey()).append("=").append(item.getValue()).append("&");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return StringUtils.getEncodedString(stringBuilder.toString());
        }
    }

}
