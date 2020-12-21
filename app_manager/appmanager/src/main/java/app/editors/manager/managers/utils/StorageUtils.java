package app.editors.manager.managers.utils;


import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import app.editors.manager.app.Api;
import lib.toolkit.base.managers.utils.StringUtils;

public class StorageUtils {

    public static final String ARG_AUTH_URL = "auth_url";
    public static final String ARG_CLIENT_ID = "client_id";
    public static final String ARG_REDIRECT_URI = "redirect_uri";
    public static final String ARG_RESPONSE_TYPE = "response_type";
    public static final String ARG_ACCESS_TYPE = "access_type";
    public static final String ARG_APPROVAL_PROMPT = "approval_prompt";
    public static final String ARG_SCOPE = "scope";
    public static final String ARG_CODE = "code";


    @Nullable
    public static String getStorageUrl(final String providerKey, final boolean isInfo) {
        switch (providerKey) {
            case Api.Storage.BOXNET:
                return box(providerKey, isInfo).getUrl();

            case Api.Storage.DROPBOX:
                return dropBox(providerKey, isInfo).getUrl();

            case Api.Storage.GOOGLEDRIVE:
                return google(providerKey, isInfo).getUrl();

            case Api.Storage.ONEDRIVE:
                return oneDrive(providerKey, isInfo).getUrl();
        }

        return null;
    }

    @Nullable
    public static Storage getStorageInstance(final String providerKey, final boolean isInfo) {
        switch (providerKey) {
            case Api.Storage.BOXNET:
                return box(providerKey, isInfo);

            case Api.Storage.DROPBOX:
                return dropBox(providerKey, isInfo);

            case Api.Storage.GOOGLEDRIVE:
                return google(providerKey, isInfo);

            case Api.Storage.ONEDRIVE:
                return oneDrive(providerKey, isInfo);
        }

        return null;
    }

    @Nullable
    public static Storage getNewStorageInstance(final String providerKey, app.editors.manager.mvp.models.account.Storage storage) {
        switch (providerKey) {
            case Api.Storage.BOXNET:
                return newBox(providerKey, storage);

            case Api.Storage.DROPBOX:
                return newDropBox(providerKey, storage);

            case Api.Storage.GOOGLEDRIVE:
                return newGoogle(providerKey, storage);

            case Api.Storage.ONEDRIVE:
                return newOneDrive(providerKey, storage);
        }

        return null;
    }

    /*
     * Get Box instance for request token
     * */
    private static Storage box(final String providerKey, final boolean isInfo) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Box.VALUE_RESPONSE_TYPE);

        if (isInfo) {
            uriMap.put(ARG_CLIENT_ID, Constants.Box.INFO_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Box.INFO_REDIRECT_URL);
        } else {
            uriMap.put(ARG_CLIENT_ID, Constants.Box.COM_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Box.COM_REDIRECT_URL);
        }

        return new Storage(providerKey, Constants.Box.AUTH_URL, uriMap);
    }

    private static Storage newBox(final String providerKey, app.editors.manager.mvp.models.account.Storage storage) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Box.VALUE_RESPONSE_TYPE);

        uriMap.put(ARG_CLIENT_ID, storage.getClientId());
        uriMap.put(ARG_REDIRECT_URI, storage.getRedirectUrl());

        return new Storage(providerKey, Constants.Box.AUTH_URL, uriMap);
    }

    /*
     * Get DropBox instance for request token
     * */
    private static Storage dropBox(final String providerKey, final boolean isInfo) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.DropBox.VALUE_RESPONSE_TYPE);

        if (isInfo) {
            uriMap.put(ARG_CLIENT_ID, Constants.DropBox.INFO_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.DropBox.INFO_REDIRECT_URL);
        } else {
            uriMap.put(ARG_CLIENT_ID, Constants.DropBox.COM_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.DropBox.COM_REDIRECT_URL);
        }

        return new Storage(providerKey, Constants.DropBox.AUTH_URL, uriMap);
    }

    private static Storage newDropBox(final String providerKey, final app.editors.manager.mvp.models.account.Storage storage) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.DropBox.VALUE_RESPONSE_TYPE);

        uriMap.put(ARG_CLIENT_ID, storage.getClientId());
        uriMap.put(ARG_REDIRECT_URI, storage.getRedirectUrl());

        return new Storage(providerKey, Constants.DropBox.AUTH_URL, uriMap);
    }

    /*
     * Get Google instance for request token
     * */
    private static Storage google(final String providerKey, final boolean isInfo) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_APPROVAL_PROMPT, Constants.Google.VALUE_APPROVAL_PROMPT);
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_ACCESS_TYPE, Constants.Google.VALUE_ACCESS_TYPE);
        uriMap.put(ARG_SCOPE, Constants.Google.VALUE_SCOPE);

        if (isInfo) {
            uriMap.put(ARG_CLIENT_ID, Constants.Google.INFO_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Google.INFO_REDIRECT_URL);
        } else {
            uriMap.put(ARG_CLIENT_ID, Constants.Google.COM_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Google.COM_REDIRECT_URL);
        }

        return new Storage(providerKey, Constants.Google.AUTH_URL, uriMap);
    }


    private static Storage newGoogle(final String providerKey, final app.editors.manager.mvp.models.account.Storage storage) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_APPROVAL_PROMPT, Constants.Google.VALUE_APPROVAL_PROMPT);
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_ACCESS_TYPE, Constants.Google.VALUE_ACCESS_TYPE);
        uriMap.put(ARG_SCOPE, Constants.Google.VALUE_SCOPE);

        uriMap.put(ARG_CLIENT_ID, storage.getClientId());
        uriMap.put(ARG_REDIRECT_URI, storage.getRedirectUrl());

        return new Storage(providerKey, Constants.Google.AUTH_URL, uriMap);
    }

    /*
     * Get OneDrive instance for request token
     * */
    private static Storage oneDrive(final String providerKey, final boolean isInfo) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_SCOPE, Constants.Google.VALUE_SCOPE);

        if (isInfo) {
            uriMap.put(ARG_CLIENT_ID, Constants.Google.INFO_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Google.INFO_REDIRECT_URL);
        } else {
            uriMap.put(ARG_CLIENT_ID, Constants.Google.COM_CLIENT_ID);
            uriMap.put(ARG_REDIRECT_URI, Constants.Google.COM_REDIRECT_URL);
        }

        return new Storage(providerKey, Constants.Google.AUTH_URL, uriMap);
    }

    private static Storage newOneDrive(final String providerKey, final app.editors.manager.mvp.models.account.Storage storage) {
        final TreeMap<String, String> uriMap = new TreeMap<>();
        uriMap.put(ARG_RESPONSE_TYPE, Constants.Google.VALUE_RESPONSE_TYPE);
        uriMap.put(ARG_SCOPE, Constants.Google.VALUE_SCOPE);

        uriMap.put(ARG_CLIENT_ID, storage.getClientId());
        uriMap.put(ARG_REDIRECT_URI, storage.getRedirectUrl());

        return new Storage(providerKey, Constants.Google.AUTH_URL, uriMap);
    }


    /*
     * Storage info
     * */
    public static final class Storage implements Serializable {

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
