package app.editors.manager.managers.utils;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import lib.toolkit.base.managers.utils.StringUtils;
import okhttp3.Credentials;

public class GlideUtils {

    private static Object getAuthLoad(@NonNull String url, PreferenceTool preference) {
        if (isRequireAuth(url, preference)) {
            return new GlideUrl(url, new LazyHeaders.Builder()
                    .addHeader(Api.HEADER_AUTHORIZATION, preference.getToken())
                    .build());
        }

        return url;
    }

    private static Object getWebDavLoad(String url, AccountsSqlData account) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader(WebDavApi.HEADER_AUTHORIZATION, Credentials.basic(account.getLogin(), account.getPassword()))
                .build());
    }

    private static boolean isRequireAuth(@NonNull final String url, PreferenceTool preference) {
        return StringUtils.isRequireAuth(preference.getPortal(), url);
    }


    public static Object getCorrectLoad(@NonNull final String url, PreferenceTool preference) {
        return getAuthLoad(correctUrl(url, preference), preference);
    }

    public static Object getCorrectLoad(@NonNull final String url, @NonNull final String token) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader(Api.HEADER_AUTHORIZATION, token)
                .build());
    }

    private static String correctUrl(final String url, PreferenceTool preference) {
        return StringUtils.correctUrl(preference.getScheme() + preference.getPortal(), url);
    }

    public static Object getWebDavUrl(String webUrl, AccountsSqlData account) {
        return getWebDavLoad(account.getScheme() + account.getPortal() + webUrl, account);
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
