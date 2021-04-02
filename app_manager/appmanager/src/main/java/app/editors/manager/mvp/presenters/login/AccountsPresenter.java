package app.editors.manager.mvp.presenters.login;

import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.response.ResponseSettings;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.AccountsView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import okhttp3.Credentials;
import retrofit2.HttpException;
import retrofit2.Response;

@InjectViewState
public class AccountsPresenter extends BaseLoginPresenter<AccountsView, ResponseUser> {

    public static final String TAG = AccountsPresenter.class.getSimpleName();

    @Inject
    protected AccountSqlTool mAccountSqlTool;

    private AccountsSqlData mAccountClickedItem;
    private int mAccountClickedPosition;

    private Disposable mDisposable;

    public AccountsPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public void getAccounts() {
        final List<AccountsSqlData> accounts = mAccountSqlTool.getAccounts();
        getViewState().onUsersAccounts(accounts);
    }

    public void setAccountClicked(final AccountsSqlData account, final int position) {
        mAccountClickedItem = account;
        mAccountClickedPosition = position;
    }

    public void deleteAccount() {
        if (mAccountSqlTool.delete(mAccountClickedItem)) {
//            if (mAccountSqlTool.getAccounts().size() > 0){
//                mAccountClickedItem = mAccountSqlTool.getAccounts().get(0);
//                mPreferenceTool.setNoPortal(true);
//            } else {
//                mPreferenceTool.setDefault();
//            }
//            mPreferenceTool.setNoPortal(true);
            if (mAccountClickedItem.getLogin().equals(mPreferenceTool.getLogin()) &&
                    mAccountClickedItem.getPortal().equals(mPreferenceTool.getPortal())) {
                mPreferenceTool.setDefault();
            }
            getViewState().onAccountDelete(mAccountClickedPosition);
        }
    }

    public void loginAccount() {
        if (mAccountClickedItem.isOnline()) {
            getViewState().onError(mContext.getString(R.string.errors_sign_in_account_already_use));
            return;
        }

        if (mAccountClickedItem.isWebDav()) {
            loginWebDav();
            return;
        }

        final String token = mAccountClickedItem.getToken();
        final String scheme = mAccountClickedItem.getScheme();
        final String portal = mAccountClickedItem.getPortal();

        RetrofitTool retrofitTool = new RetrofitTool(mContext);
        retrofitTool.setSslOn(mAccountClickedItem.isSslState());
        retrofitTool.setCiphers(mAccountClickedItem.isSslCiphers());

        mApi = retrofitTool.getApi(scheme + portal);
        getViewState().showWaitingDialog();
        mDisposable = mApi.getSettings()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(ResponseSettings::getResponse)
                .subscribe(settings -> {
                            mPreferenceTool.setServerVersion(settings.getCommunityServer());
                            login(token, portal, scheme, mApi);
                        }
                        , throwable -> {
                            mPreferenceTool.setServerVersion("");
                            login(token, portal, scheme, mApi);
                        });

    }

    private void loginWebDav() {
        if (mAccountClickedItem.getPassword().equals("")) {
            getViewState().onWebDavLogin(mAccountClickedItem);
            return;
        }
        final String credential = Credentials.basic(mAccountClickedItem.getLogin(), mAccountClickedItem.getPassword());
        final WebDavApi.Providers provider = WebDavApi.Providers.valueOf(mAccountClickedItem.getWebDavProvider());
        String path;
        if (provider == WebDavApi.Providers.OwnCloud || provider == WebDavApi.Providers.NextCloud ||
                provider == WebDavApi.Providers.WebDav) {
            path = mAccountClickedItem.getWebDavPath();
        } else {
            path = provider.getPath();
        }

        mDisposable = WebDavApi.getApi(mAccountClickedItem.getScheme() + mAccountClickedItem.getPortal())
                .capabilities(credential, path)
                .doOnSubscribe(disposable -> getViewState().showWaitingDialog())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful() && responseBody.code() == 207) {
                        return responseBody.body();
                    } else {
                        throw new HttpException(responseBody);
                    }
                })
                .subscribe(body -> saveWebDav(), this::fetchError);
    }

    private void saveWebDav() {
        setAccount(false);
        mPreferenceTool.setDefault();
        mPreferenceTool.setPortal(mAccountClickedItem.getPortal());
        mPreferenceTool.setLogin(mAccountClickedItem.getLogin());
        mPreferenceTool.setPassword(mAccountClickedItem.getPassword());
        mPreferenceTool.setVisitor(false);
        mPreferenceTool.setNoPortal(false);
        setAccount(true);
        getViewState().onAccountLogin();
    }

    private void login(String token, String portal, String scheme, Api api) {
        // Check/validate account data
        if (token != null && !token.isEmpty() && scheme != null && !scheme.isEmpty() && portal != null && !portal.isEmpty()) {
            mRequestCall = api.getUserInfo(token);
            mRequestCall.enqueue(new BaseCallback() {

                @Override
                public void onSuccessResponse(retrofit2.Response<ResponseUser> response) {
                    final User user = response.body().getResponse();
                    setAccount(false);
                    mPreferenceTool.setToken(mAccountClickedItem.getToken());
                    mPreferenceTool.setPortal(mAccountClickedItem.getPortal());
                    mPreferenceTool.setScheme(mAccountClickedItem.getScheme());
                    mPreferenceTool.setLogin(mAccountClickedItem.getLogin());
                    mPreferenceTool.setSocialProvider(mAccountClickedItem.getProvider());
                    mPreferenceTool.setSslState(mAccountClickedItem.isSslState());
                    mPreferenceTool.setSslCiphers(mAccountClickedItem.isSslCiphers());
                    mPreferenceTool.setAdmin(user.getIsAdmin());
                    mPreferenceTool.setVisitor(user.getIsVisitor());
                    mPreferenceTool.setOwner(user.getIsOwner());
                    mPreferenceTool.setUserDisplayName(user.getDisplayNameHtml());
                    mPreferenceTool.setUserAvatarUrl(user.getAvatar());
                    mPreferenceTool.setSelfId(user.getId());
                    mPreferenceTool.setNoPortal(false);
                    mAccountSqlTool.setAccountName(mAccountClickedItem.getPortal(), mAccountClickedItem.getLogin(),
                            mAccountClickedItem.getProvider(), user.getDisplayNameHtml());
                    setAccount(true);
                    try {
                        initRetrofitPref(mPreferenceTool.getPortal());
                        getViewState().onAccountLogin();
                    } catch (UrlSyntaxMistake urlSyntaxMistake) {
                        urlSyntaxMistake.printStackTrace();
                    }
                }

                @Override
                public void onErrorResponse(Response<ResponseUser> response) {
                    super.onErrorResponse(response);
                    if (response.code() == Api.HttpCodes.CLIENT_UNAUTHORIZED) {
                        mAccountClickedItem.setToken("");
                        getViewState().onSignIn(portal, mAccountClickedItem.getLogin());
                    }
                }
            });
        } else if (token != null && token.isEmpty()) {
            getViewState().onSignIn(portal, mAccountClickedItem.getLogin());
        } else {
            getViewState().onError(mContext.getString(R.string.errors_sign_in_account_error));
        }
    }

    private void setAccount(boolean isOnline) {
        AccountsSqlData account = mAccountSqlTool.getAccount(mPreferenceTool.getPortal(), mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider());
        if (account != null) {
            account.setOnline(isOnline);
            mAccountSqlTool.setAccount(account);
        }
    }

}
