package app.editors.manager.managers.utils;

import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;


public class UiUtils {

    public static void setWebDavImage(String providerName, ImageView image) {
        WebDavApi.Providers provider = WebDavApi.Providers.valueOf(providerName);
        switch (provider) {
            case NextCloud:
                image.setImageDrawable(ContextCompat.getDrawable(image.getContext(), R.drawable.ic_storage_nextcloud));
                break;
            case OwnCloud:
                image.setImageDrawable(ContextCompat.getDrawable(image.getContext(), R.drawable.ic_storage_owncloud));
                break;
            case Yandex:
                image.setImageDrawable(ContextCompat.getDrawable(image.getContext(), R.drawable.ic_storage_yandex));
                break;
            case WebDav:
                image.setImageDrawable(ContextCompat.getDrawable(image.getContext(), R.drawable.ic_storage_webdav));
                break;
        }
    }

}
