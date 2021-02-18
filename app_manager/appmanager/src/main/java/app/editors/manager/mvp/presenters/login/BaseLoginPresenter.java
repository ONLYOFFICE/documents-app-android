package app.editors.manager.mvp.presenters.login;

import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.managers.exceptions.NoConnectivityException;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.managers.utils.FirebaseUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.response.ResponseSettings;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.user.Token;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.presenters.base.BasePresenter;
import app.editors.manager.mvp.views.base.BaseView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.StringUtils;
import retrofit2.Call;

public abstract class BaseLoginPresenter<View extends BaseView, Response> extends BasePresenter<View, Response> {

    protected static final String GOOGLE_PERMISSION = "GOOGLE_PERMISSION";
    protected static final String KEY_NULL_VALUE = "null";

    protected Call<ResponseSignIn> mRequestSignInCall;
    protected Call<ResponseUser> mRequestUserCall;

    protected Account mAccount;
    protected AsyncTask<Void, Void, String> mGetGoogleToken;

    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mRequestSignInCall != null) {
            mRequestSignInCall.cancel();
        }

        if (mRequestUserCall != null) {
            mRequestUserCall.cancel();
        }
    }

    private int mTryCounter = 0;
    protected Api mApi;
    private Disposable mDisposable;

    /*
     * Common sign in
     * */
    protected void signIn(final RequestSignIn requestSignIn, @NonNull String portal) {
        try {
            mPreferenceTool.setSecretKey("");
            AccountsSqlData sqlData = mAccountSqlTool.getAccount(portal, requestSignIn.getUserName(), requestSignIn.getProvider());

            RetrofitTool retrofitTool = new RetrofitTool(mContext);
            if (sqlData != null) {
                retrofitTool.setSslOn(sqlData.isSslState());
                retrofitTool.setCiphers(sqlData.isSslCiphers());
                mApi = retrofitTool.getApi(sqlData.getScheme() + StringUtils.getEncodedString(portal));
                mRequestSignInCall = mApi.signIn(requestSignIn);
            } else {
                final String url = mPreferenceTool.getScheme() + portal;

                retrofitTool.setSslOn(mPreferenceTool.getSslState());
                retrofitTool.setCiphers(mPreferenceTool.getSslCiphers());
                retrofitTool.init(url);

                mRequestSignInCall = retrofitTool.getApi(url).signIn(requestSignIn);
            }

            mRequestSignInCall.enqueue(new CommonCallback<ResponseSignIn>() {

                @Override
                public void onSuccessResponse(retrofit2.Response<ResponseSignIn> response) {
                    if (response.body() != null) {
                        final Token token = response.body().getResponse();
                        if (requestSignIn.getProvider() == null) {
                            mPreferenceTool.setLogin(requestSignIn.getUserName());
                            mPreferenceTool.setPassword(requestSignIn.getPassword());
                        }
                        mPreferenceTool.setSocialToken(requestSignIn.getAccessToken());
                        mPreferenceTool.setSocialProvider(requestSignIn.getProvider());
                        if (token.getSms()) {
                            smsAuth(token.getPhoneNoise(), sqlData);
                        } else if (token.getTfa()) {
                            appAuth(token.getTfaKey(), sqlData);
                        } else {
                            if (sqlData != null) {
                                mPreferenceTool.setScheme(sqlData.getScheme());
                            }
                            mPreferenceTool.setPortal(portal);
                            mPreferenceTool.setSecretKey("");
                            onGetToken(token.getToken(), sqlData);
                        }
                    }
                }

                @Override
                public void onErrorResponse(retrofit2.Response<ResponseSignIn> response) {
                    super.onErrorResponse(response);
                    onSignInError(response);
                }

                @Override
                public void onFailResponse(Throwable t) {
                    if (t instanceof IOException && mTryCounter++ < 3) {
                        signIn(requestSignIn, portal);
                    } else {
                        mTryCounter = 0;
                        super.onFailResponse(t);
                        onSignInFail(t);
                    }
                }
            });
        } catch (UrlSyntaxMistake urlSyntaxMistake) {
            urlSyntaxMistake.printStackTrace();
        }
    }

    private void appAuth(String secretKey, AccountsSqlData sqlData) {
        if (secretKey != null && !secretKey.isEmpty()) {
            mPreferenceTool.setSecretKey(secretKey);
            onTwoFactorAuthApp(true, sqlData);
        } else {
            onTwoFactorAuthApp(false, sqlData);
        }
    }

    private void smsAuth(String phoneNoise, AccountsSqlData sqlData) {
        if (phoneNoise == null || phoneNoise.isEmpty() || phoneNoise.equalsIgnoreCase(KEY_NULL_VALUE)) {
            onTwoFactorAuth(true, sqlData);
        } else {
            mPreferenceTool.setPhoneNoise(phoneNoise);
            onTwoFactorAuth(false, sqlData);
        }
    }

    protected void onSignInError(retrofit2.Response<ResponseSignIn> response) {
    }

    protected void onSignInFail(Throwable t) {
    }

    protected void onTwoFactorAuth(boolean isPhone, AccountsSqlData sqlData) {
    }

    protected void onTwoFactorAuthApp(boolean isSecret, AccountsSqlData sqlData) {
    }

    protected void onGetToken(String token, AccountsSqlData sqlData) {
        mPreferenceTool.setToken(token);
        getUser(token, sqlData);
    }

    /*
     * Get user config
     * */
    protected void getUser(final String token, AccountsSqlData sqlData) {
        if (sqlData != null) {
            try {
                mPreferenceTool.setPortal(sqlData.getPortal());
                mPreferenceTool.setSslState(sqlData.isSslState());
                mPreferenceTool.setSslCiphers(sqlData.isSslCiphers());
                mRetrofitApi.setCiphers(sqlData.isSslCiphers());
                mRetrofitApi.setSslOn(sqlData.isSslState());
                mApi = mRetrofitApi.init(sqlData.getScheme() + StringUtils.getEncodedString(sqlData.getPortal()))
                        .getApi(sqlData.getScheme() + StringUtils.getEncodedString(sqlData.getPortal()));
            } catch (UrlSyntaxMistake urlSyntaxMistake) {
                urlSyntaxMistake.printStackTrace();
            }
        } else {
            mApi = mRetrofitTool.getApiWithPreferences();
        }
        mDisposable = mApi.getSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(responseSettings -> {
                    mPreferenceTool.setServerVersion(responseSettings.getResponse().getCommunityServer());
                    return Observable.fromCallable(() -> mApi.getUserInfo(token).execute())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .subscribe(responseUser -> {
                    if (responseUser.body() != null && responseUser.isSuccessful()) {
                        onGetUser(responseUser.body().getResponse());
                    } else {
                        onErrorUser(responseUser);
                    }
                }, throwable -> {
                    if (!(throwable instanceof NoConnectivityException)) {
                        onFailUser(throwable);
                    }
                });
    }

    protected void onGetUser(User user) {
        try {
            FirebaseUtils.addAnalyticsLogin(mPreferenceTool.getPortal(), mPreferenceTool.getSocialProvider());
            setUserInfo(user);
            createAccount();
            initRetrofitPref(mPreferenceTool.getPortal());
        } catch (UrlSyntaxMistake urlSyntaxMistake) {
            urlSyntaxMistake.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null){
            mDisposable.dispose();
        }
    }

    protected void onErrorUser(retrofit2.Response<ResponseUser> response) {
    }

    protected void onFailUser(Throwable t) {
    }

    /*
     * Save user info
     * */
    protected void setUserInfo(User user) {
        mPreferenceTool.setLogin(user.getEmail());
        mPreferenceTool.setAdmin(user.getIsAdmin());
        mPreferenceTool.setVisitor(user.getIsVisitor());
        mPreferenceTool.setOwner(user.getIsOwner());
        mPreferenceTool.setUserDisplayName(user.getDisplayNameHtml());
        mPreferenceTool.setUserFirstName(user.getFirstName());
        mPreferenceTool.setUserLastName(user.getLastName());
        mPreferenceTool.setUserAvatarUrl(user.getAvatar());
        mPreferenceTool.setSelfId(user.getId());
        mPreferenceTool.setScheme(URLUtil.isHttpsUrl(user.getProfileUrl()) ? Api.SCHEME_HTTPS : Api.SCHEME_HTTP);
    }

    protected void createAccount() {
        mAccountSqlTool.setAccount(mPreferenceTool.getPortal(), mPreferenceTool.getLogin(), mPreferenceTool.getScheme(),
                mPreferenceTool.getUserDisplayName(), mPreferenceTool.getToken(), mPreferenceTool.getSocialProvider(),
                mPreferenceTool.getUserAvatarUrl(), null, mPreferenceTool.getSslCiphers(), mPreferenceTool.getSslState());
        AccountsSqlData account = mAccountSqlTool.getAccount(mPreferenceTool.getPortal(), mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider());
        AccountsSqlData onlineAccount = mAccountSqlTool.getAccountOnline();
        if (onlineAccount != null) {
            onlineAccount.setOnline(false);
            mAccountSqlTool.setAccount(onlineAccount);
        }
        if (account != null) {
            account.setOnline(true);
            mAccountSqlTool.setAccount(account);
        }
    }

    /*
     * Socials
     * */
    public void signInWithTwitter(final String token, String portal) {
        final RequestSignIn requestSignIn = new RequestSignIn();
        requestSignIn.setProvider(Api.Social.TWITTER);
        requestSignIn.setAccessToken(token);
        signIn(requestSignIn, portal);
    }

    public void signInWithFacebook(final String token, String portal) {
        final RequestSignIn requestSignIn = new RequestSignIn();
        requestSignIn.setProvider(Api.Social.FACEBOOK);
        requestSignIn.setAccessToken(token);
        mPreferenceTool.setSocialProvider(Api.Social.FACEBOOK);
        mPreferenceTool.setSocialToken(token);
        signIn(requestSignIn, portal);
    }

    public void retrySignInWithGoogle(String portal) {
        signInWithGoogle(mAccount, portal);
    }

    public void signInWithGoogle(final Account account, String portal) {
        if (mGetGoogleToken != null && !AsyncTask.Status.FINISHED.equals(mGetGoogleToken.getStatus())) {
            return;
        }

        mAccount = account;
        mGetGoogleToken = new GoogleToken(account, portal);
        mGetGoogleToken.execute();
    }

    /*
     * Google get token
     * */
    private class GoogleToken extends AsyncTask<Void, Void, String> {

        private Intent mIntent;
        private Account mAccount;
        private RequestSignIn mRequestSignIn;
        private String mPortal;

        public GoogleToken(final Account account, String portal) {
            mAccount = account;
            mRequestSignIn = new RequestSignIn();
            mRequestSignIn.setProvider(Api.Social.GOOGLE);
            mRequestSignIn.setUserName(account.name);
            mPreferenceTool.setSocialProvider(Api.Social.GOOGLE);
            mPortal = portal;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String scope = mContext.getString(R.string.google_scope);
                final String accessToken = GoogleAuthUtil.getToken(mContext, mAccount, scope);
                mRequestSignIn.setAccessToken(accessToken);
                mPreferenceTool.setSocialToken(accessToken);
                mPreferenceTool.setLogin(mAccount.name);
            } catch (UserRecoverableAuthException e) {
                mIntent = e.getIntent();
                return GOOGLE_PERMISSION;
            } catch (Exception e) {
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                signIn(mRequestSignIn, mPortal);
            } else {
                switch (result) {
                    case GOOGLE_PERMISSION:
                        onGooglePermission(mIntent);
                        break;
                    default:
                        getViewState().onError(result);
                }
            }
        }
    }

    /*
     * If we need user confirmation
     * */
    protected void onGooglePermission(Intent intent) {
    }

    @Nullable
    protected String getPortal(final String url) {
        if (StringUtils.isValidUrl(url)) {
            mPreferenceTool.setScheme(URLUtil.isHttpsUrl(url) ? Api.SCHEME_HTTPS : Api.SCHEME_HTTP);
            return lib.toolkit.base.managers.utils.StringUtils.getUrlWithoutScheme(url);
        } else {
            final String concatUrl = mPreferenceTool.getScheme() + url;
            if (StringUtils.isValidUrl(concatUrl)) {
                return url;
            }
        }

        return null;
    }

}
