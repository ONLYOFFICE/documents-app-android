package app.editors.manager.mvp.presenters.login;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.login.WebDavSignInView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

@InjectViewState
public class WebDavSignInPresenter extends BasePresenter<WebDavSignInView, ResponseUser> {

    private Disposable mLoginDisposable;

    public WebDavSignInPresenter() {
        super();
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoginDisposable != null) {
            mLoginDisposable.dispose();
        }
    }

    public void checkPortal(WebDavApi.Providers provider, String url, String login, String password) {

        final String correctUrl = correctUrl(url, provider);
        final String credentials = Credentials.basic(login, password);

        if (correctUrl.equals("")) {
            return;
        }

        if (mLoginDisposable != null && !mLoginDisposable.isDisposed()) {
            mLoginDisposable.dispose();
        }

        String path;
        if (provider.equals(WebDavApi.Providers.OwnCloud) || provider.equals(WebDavApi.Providers.NextCloud) ||
                provider.equals(WebDavApi.Providers.WebDav)) {
            path = getPortalPath(correctUrl, provider, login);
        } else {
            path = provider.getPath();
        }

        WebDavApi api;
        try {
            api = WebDavApi.getApi(correctUrl);
            mLoginDisposable = api.capabilities(credentials, path)
                    .doOnSubscribe(disposable -> getViewState().onDialogWaiting(mContext.getString(R.string.dialogs_check_portal_header_text)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseBody -> {
                        if (responseBody.isSuccessful() && responseBody.code() == 207) {
                            if (provider == WebDavApi.Providers.WebDav) {
                                createUser(provider, path, correctUrl, login, password, true);
                            } else {
                                createUser(provider, path, url, login, password, true);
                            }
                        } else if (responseBody.code() == 404 && correctUrl.startsWith(Api.SCHEME_HTTPS)) {
                            String httpUrl = correctUrl.replaceAll(Api.SCHEME_HTTPS, Api.SCHEME_HTTP);
                            checkHttp(httpUrl, provider, login, password, path);
                        } else {
                            getViewState().onDialogClose();
                            fetchError(new HttpException(responseBody));
                        }
                    }, throwable -> {
                        if (throwable instanceof ConnectException && correctUrl.startsWith(Api.SCHEME_HTTPS)) {
                            String httpUrl = correctUrl.replaceAll(Api.SCHEME_HTTPS, Api.SCHEME_HTTP);
                            checkHttp(httpUrl, provider, login, password, path);
                        } else {
                            getViewState().onDialogClose();
                            fetchError(throwable);
                        }
                    });
        } catch (IllegalArgumentException e) {
            getViewState().onError(mContext.getString(R.string.errors_path_url));
        }
    }

    public void checkNextCloud(WebDavApi.Providers provider, String url, Boolean isHttp) {
        StringBuilder correctUrl = new StringBuilder();
        if (!isHttp) {
            correctUrl.append(correctUrl(url, provider));
        } else {
            correctUrl.append(url);
        }

        String[] paths = correctUrl.toString().split("/");
        String path = "";
        if (paths.length > 3) {
            int index = correctUrl.indexOf(paths[3]);
            path = correctUrl.substring(index);
        }

        if (!correctUrl.toString().endsWith("/")) {
            correctUrl.append("/");
        }
        getViewState().onDialogWaiting(mContext.getString(R.string.dialogs_check_portal_header_text));

        try {
            WebDavApi.getApi(correctUrl.toString()).capability(path + "/index.php/login/flow").enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.code() == 200) {
                        getViewState().onNextCloudLogin(correctUrl.toString());
                    } else {
                        if (correctUrl.toString().startsWith(Api.SCHEME_HTTPS)) {
                            final String httpUrl = correctUrl.toString().replaceAll(Api.SCHEME_HTTPS, Api.SCHEME_HTTP);
                            checkNextCloud(provider, httpUrl, true);
                        } else {
                            onErrorHandle(response.body(), response.code());
                            getViewState().onUrlError(mContext.getString(R.string.errors_path_url));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    if (correctUrl.toString().startsWith(Api.SCHEME_HTTPS)) {
                        final String httpUrl = correctUrl.toString().replaceAll(Api.SCHEME_HTTPS, Api.SCHEME_HTTP);
                        checkNextCloud(provider, httpUrl, true);
                    } else {
                        onFailureHandle(t);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            final String message = e.getMessage();
            if (message != null && message.contains("Invalid URL")) {
                getViewState().onDialogClose();
                getViewState().onUrlError(mContext.getString(R.string.errors_path_url));
            }
        }

    }

    private void checkHttp(String httpUrl, WebDavApi.Providers provider, String login, String password, String path) {
        final String credentials = Credentials.basic(login, password);
        mLoginDisposable = WebDavApi.getApi(httpUrl).capabilities(credentials, path)
                .doOnSubscribe(disposable -> getViewState().onDialogWaiting(mContext.getString(R.string.dialogs_check_portal_header_text)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBody -> {
                    if (responseBody.isSuccessful() && responseBody.code() == 207) {
                        createUser(provider, path, httpUrl, login, password, false);
                    } else {
                        getViewState().onDialogClose();
                        fetchError(new HttpException(responseBody));
                    }
                }, throwable -> {
                    getViewState().onDialogClose();
                    fetchError(throwable);
                });
    }

    private void createUser(WebDavApi.Providers provider, String path, String url, String login, String password, boolean isHttps) {
        AccountsSqlData account = new AccountsSqlData();
        account.setWebDav(true);

        account.setPortal(correctPortal(url));
        account.setLogin(login);
        account.setPassword(password);
        if (isHttps) {
            account.setScheme(Api.SCHEME_HTTPS);
        } else {
            account.setScheme(Api.SCHEME_HTTP);
        }
        if (provider == WebDavApi.Providers.OwnCloud || provider == WebDavApi.Providers.NextCloud ||
                provider == WebDavApi.Providers.WebDav) {
            account.setWebDavPath(path);
        } else {
            account.setWebDavPath(provider.getPath());
        }
        account.setWebDavProvider(provider.name());
        setPreferences(account, password);
    }

    private void setPreferences(AccountsSqlData account, String password) {
        mPreferenceTool.setDefault();
        mPreferenceTool.setLogin(account.getLogin());
        mPreferenceTool.setPassword(password);
        mPreferenceTool.setPortal(StringUtils.getUrlWithoutScheme(account.getPortal()));
        mPreferenceTool.setScheme(account.getScheme());
        login(account);
    }

    private void login(AccountsSqlData account) {
        AccountsSqlData onlineAccount = mAccountSqlTool.getAccountOnline();
        if (onlineAccount != null) {
            onlineAccount.setOnline(false);
            mAccountSqlTool.setAccount(onlineAccount);
        }
        account.setOnline(true);
        mAccountSqlTool.setAccount(account);
        getViewState().onDialogClose();
        getViewState().onLogin();
    }

    private String getPortalPath(String url, WebDavApi.Providers provider, String login) {
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        String base = uri.getAuthority();
        if (base != null && path.contains(base)) {
            path = path.replaceAll(base, "");
        }
        if (path != null && !path.equals("")) {
            StringBuilder builder = new StringBuilder();
            if (path.charAt(path.length() - 1) == '/') {
                path = path.substring(0, path.lastIndexOf('/'));
            }
            if (!provider.equals(WebDavApi.Providers.WebDav)) {
                return builder.append(path)
                        .append(provider.getPath())
                        .append(login)
                        .toString();
            } else {
                return builder.append(path)
                        .append(provider.getPath())
                        .toString();
            }

        } else {
            if (provider.equals(WebDavApi.Providers.WebDav)) {
                return provider.getPath();
            } else {
                return provider.getPath() + login;
            }

        }
    }

    private String correctUrl(String url, WebDavApi.Providers provider) {
        StringBuilder correctUrl = new StringBuilder();
        url = url.replaceAll("\\s+", "");

        Uri uri = Uri.parse(url);
        final String path = uri.getPath();

        switch (provider) {
            case NextCloud:
            case OwnCloud:
//                if (path != null && !path.equals("")) {
//                    url = url.replaceAll(path, "");
//                }
                break;
            case Yandex:
            case WebDav:
                break;
        }

        if (url.startsWith(Api.SCHEME_HTTP)) {
            correctUrl.append(Api.SCHEME_HTTPS).append(url.replaceAll(Api.SCHEME_HTTP, ""));
        } else if (url.startsWith(Api.SCHEME_HTTPS)) {
            correctUrl.append(url);
        } else {
            correctUrl.append(Api.SCHEME_HTTPS).append(url);
        }

        if (correctUrl.charAt(correctUrl.length() - 1) != '/') {
            correctUrl.append("/");
        }

        if (provider != WebDavApi.Providers.Yandex) {
            try {
                URL checkUrl = new URL(correctUrl.toString());
            } catch (MalformedURLException e) {
                getViewState().onUrlError(mContext.getString(R.string.errors_path_url));
                return "";
            }
        }

        return correctUrl.toString();
    }

    private String correctPortal(String portal) {
        if (portal.contains(Api.SCHEME_HTTP)) {
            portal = portal.replaceAll(Api.SCHEME_HTTP, "");
        } else if (portal.contains(Api.SCHEME_HTTPS)) {
            portal = portal.replaceAll(Api.SCHEME_HTTPS, "");
        }

        if (portal.contains("/")) {
            return correctPortal(portal.substring(0, portal.indexOf("/")));
        } else {
            return portal;
        }
    }

}
