package app.editors.manager.managers.utils;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;

import app.documents.core.account.CloudAccount;
import app.documents.core.network.ApiContract;
import app.documents.core.webdav.WebDavApi;
import app.editors.manager.R;
import okhttp3.Credentials;

public class GlideUtils {

    private static Object getWebDavLoad(String url, CloudAccount account, String password) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader(WebDavApi.HEADER_AUTHORIZATION, Credentials.basic(account.getLogin(), password))
                .build());
    }

    public static Object getCorrectLoad(@NonNull final String url, @NonNull final String token) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader(ApiContract.HEADER_AUTHORIZATION, token)
                .build());
    }

    public static Object getWebDavUrl(String webUrl, CloudAccount account, String password) {
        return getWebDavLoad(account.getScheme() + account.getPortal() + webUrl, account, password);
    }

    public static RequestOptions getAvatarOptions() {
        return new RequestOptions()
                .timeout(30 * 1000)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_account_placeholder)
                .placeholder(R.drawable.ic_account_placeholder)
                .circleCrop();
    }
}
