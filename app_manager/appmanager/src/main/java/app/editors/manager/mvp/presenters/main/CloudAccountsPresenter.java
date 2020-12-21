package app.editors.manager.mvp.presenters.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.app.App;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.presenters.login.BaseLoginPresenter;
import app.editors.manager.mvp.views.main.CloudAccountsView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

@InjectViewState
public class CloudAccountsPresenter extends BaseLoginPresenter<CloudAccountsView, ResponseUser> {

    public static final String TAG = CloudAccountsPresenter.class.getSimpleName();

    @Inject
    protected Context mContext;
    @Inject
    protected AccountSqlTool mAccountSqlTool;
    @Inject
    protected PreferenceTool mPreferenceTool;

    @Nullable
    private AccountsSqlData mContextAccount;
    @Nullable
    private AccountsSqlData mClickedAccount;

    private int mContextPosition;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    private List<AccountsSqlData> mSelectedItems;
    private boolean mIsSelectionMode;

    public CloudAccountsPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void getAccounts() {
        List<AccountsSqlData> accounts = mAccountSqlTool.getAccounts();
        if (mIsSelectionMode) {
            checkSelected(accounts);
            getViewState().onSelectionMode();
        }
        if (!accounts.isEmpty()) {
            getViewState().onSetAccounts(accounts);
        }
        checkUpdate(accounts);
    }

    private void checkSelected(List<AccountsSqlData> accounts) {
        if (mSelectedItems != null) {
            for (AccountsSqlData selectedAccount : mSelectedItems) {
                for (AccountsSqlData account : accounts) {
                    if (selectedAccount.getId().equals(account.getId())) {
                        account.setSelection(true);
                    }
                }
            }
            mSelectedItems.clear();
            for (AccountsSqlData account : accounts) {
                if (account.isSelection()) {
                    mSelectedItems.add(account);
                }
            }
        }
    }

    public void checkLogin(AccountsSqlData account, int position) {
        mClickedAccount = account;
        if (mIsSelectionMode) {
            addSelectionItem(account, position);
        } else {
            if (account.isOnline()) {
                getViewState().onShowClouds();
            } else if (account.isWebDav()) {
                loginWebDav(account);
            } else {
                login(account);
            }
        }
    }

    private void addSelectionItem(AccountsSqlData account, int position) {
        if (mSelectedItems.contains(account)) {
            account.setSelection(false);
            mSelectedItems.remove(account);
        } else {
            account.setSelection(true);
            mSelectedItems.add(account);
        }
        getViewState().onActionBarTitle(String.valueOf(mSelectedItems.size()));
        getViewState().onSelectedItem(position);
    }

    public void contextClick(AccountsSqlData account, int position) {
        mContextAccount = account;
        mContextPosition = position;
        getViewState().onShowBottomDialog(account);
    }

    private void loginWebDav(AccountsSqlData account) {
        if (account.getPassword() == null || account.getPassword().equals("")) {
            getViewState().onWebDavLogin(account);
            return;
        }
        final String credential = Credentials.basic(account.getLogin(), account.getPassword());
        final WebDavApi.Providers provider = WebDavApi.Providers.valueOf(account.getWebDavProvider());
        String path;
        if (provider == WebDavApi.Providers.OwnCloud || provider == WebDavApi.Providers.NextCloud ||
                provider == WebDavApi.Providers.WebDav) {
            path = account.getWebDavPath();
        } else {
            path = provider.getPath();
        }

        mDisposable.add(WebDavApi.getApi(account.getScheme() + account.getPortal())
                .capabilities(credential, path)
                .doOnSubscribe(disposable -> getViewState().onShowWaitingDialog())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseBody -> {
                    if (responseBody.isSuccessful() && responseBody.code() == 207) {
                        return responseBody.body();
                    } else {
                        throw new HttpException(responseBody);
                    }
                })
                .subscribe(body -> saveWebDav(account), this::fetchError));
    }

    private void saveWebDav(AccountsSqlData account) {
        setAccount(false);
        mPreferenceTool.setDefault();
        mPreferenceTool.setPortal(account.getPortal());
        mPreferenceTool.setLogin(account.getLogin());
        mPreferenceTool.setPassword(account.getPassword());
        mPreferenceTool.setVisitor(false);
        mPreferenceTool.setNoPortal(false);
        setAccount(true);
        getViewState().onAccountLogin();
    }

    private void login(AccountsSqlData account) {
        if (account.getToken() != null && !account.getToken().isEmpty()) {
            getViewState().onShowWaitingDialog();
            onGetToken(account.getToken(), account);
        } else {
            if (account.isWebDav()) {
                getViewState().onWebDavLogin(account);
            } else {
                mPreferenceTool.setScheme(account.getScheme());
                mPreferenceTool.setSslState(account.isSslState());
                mPreferenceTool.setSslCiphers(account.isSslCiphers());
                getViewState().onSignIn(account.getPortal(), account.getLogin());
            }
        }
    }

    @Override
    protected void onErrorUser(Response<ResponseUser> response) {
        if (mClickedAccount != null) {
            getViewState().onSignIn(mClickedAccount.getPortal(), mClickedAccount .getLogin());
        }
    }

    @Override
    protected void onGetUser(User user) {
        super.onGetUser(user);
        getViewState().onAccountLogin();
    }

    private void setAccount(boolean isOnline) {
        AccountsSqlData account = mAccountSqlTool.getAccount(mPreferenceTool.getPortal(), mPreferenceTool.getLogin(), mPreferenceTool.getSocialProvider());
        if (account != null) {
            account.setOnline(isOnline);
            mAccountSqlTool.setAccount(account);
        }
    }

    public void logout() {
        if (mContextAccount != null) {
            if (mContextAccount.isOnline()) {
                mPreferenceTool.setDefault();
            }
            mContextAccount.setOnline(false);
            mContextAccount.setPassword("");
            mContextAccount.setToken("");
            mAccountSqlTool.setAccount(mContextAccount);
            getViewState().onUpdateItem(mContextAccount);
        }
    }

    public void removeAccount() {
        if (mContextAccount != null) {
            if (mContextAccount.isOnline()) {
                mPreferenceTool.setDefault();
            }
            mAccountSqlTool.delete(mContextAccount);
            if (mAccountSqlTool.getAccounts().isEmpty()) {
                getViewState().onEmptyList();
            } else {
                getViewState().removeItem(mContextPosition);
            }
        }
    }

    public void signIn() {
        if (mContextAccount != null) {
            login(mContextAccount);
        }
    }

    public AccountsSqlData getContextAccount() {
        return mContextAccount;
    }

    public void longClick(AccountsSqlData account, int position) {
        mSelectedItems = new ArrayList<>();
        mIsSelectionMode = true;
        account.setSelection(true);
        mSelectedItems.add(account);
        getViewState().onSelectionMode();
        getViewState().onActionBarTitle(String.valueOf(mSelectedItems.size()));
    }

    public boolean onBackPressed() {
        if (mIsSelectionMode) {
            mIsSelectionMode = false;
            clearSelected();
            getViewState().onDefaultState();
            return true;
        }

        return false;
    }

    private void clearSelected() {
        for (AccountsSqlData account : mSelectedItems) {
            account.setSelection(false);
        }
        mSelectedItems.clear();
        mSelectedItems = null;
    }

    public void selectAll(List<AccountsSqlData> itemList) {
        for (AccountsSqlData account : itemList) {
            if (!account.isSelection() && !account.isIdNull()) {
                account.setSelection(true);
                mSelectedItems.add(account);
            }
        }
        getViewState().onActionBarTitle(String.valueOf(mSelectedItems.size()));
        getViewState().onNotifyItems();
    }

    public void deleteAll() {
        if (mSelectedItems != null) {
            for (AccountsSqlData account : mSelectedItems) {
                if (account.isOnline()) {
                    mPreferenceTool.setDefault();
                }
                mAccountSqlTool.delete(account);
            }
            onBackPressed();
            if (mAccountSqlTool.getAccounts().isEmpty()) {
                getViewState().onEmptyList();
            } else {
                getViewState().onSetAccounts(mAccountSqlTool.getAccounts());
            }
        }
    }

    public void deselectAll() {
        if (mSelectedItems != null) {
            for (AccountsSqlData account : mSelectedItems) {
                account.setSelection(false);
            }
            mSelectedItems.clear();
            getViewState().onActionBarTitle(String.valueOf(mSelectedItems.size()));
            getViewState().onNotifyItems();
        }
    }

    private void checkUpdate(List<AccountsSqlData> accounts) {
        for (AccountsSqlData account : accounts) {
            if (!account.isWebDav() && account.getToken() != null && !account.getToken().isEmpty()) {
                mDisposable.add(getUser(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(responseUser -> {
                            if (responseUser.getResponse() == null) {
                                return new User();
                            } else {
                                return responseUser.getResponse();
                            }
                        })
                        .subscribe(user -> updateAccount(user, account),
                                throwable -> Log.d(TAG, "checkUpdate: " + throwable.getMessage())));
            }
        }
    }

    private void updateAccount(User user, AccountsSqlData account) {
        if (!user.getDisplayNameHtml().isEmpty() && !account.getName().equals(user.getDisplayNameHtml())) {
            account.setName(user.getDisplayNameHtml());
        }
        if (!user.getAvatar().isEmpty() && !account.getAvatarUrl().equals(user.getAvatar())) {
            account.setAvatarUrl(user.getAvatar());
        }
        mAccountSqlTool.setAccount(account);
        getViewState().onNotifyItems();
    }

    private Observable<ResponseUser> getUser(AccountsSqlData account) {
        String token = account.getToken();
        RetrofitTool retrofitTool = new RetrofitTool(mContext);
        retrofitTool.setCiphers(account.isSslCiphers());
        retrofitTool.setSslOn(account.isSslState());
        return Observable.fromCallable(() -> {
            Response<ResponseUser> response = retrofitTool.getApi(account.getScheme() + account.getPortal())
                    .getUserInfo(token).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                return new ResponseUser();
            }
        });

    }

    @Override
    protected void onErrorHandle(ResponseBody responseBody, int responseCode) {
       // stub
    }

    public void restoreAccount() {
        final AccountsSqlData accountsSqlData = mAccountSqlTool.getAccountOnline();
        if (accountsSqlData != null) {
            mPreferenceTool.setScheme(accountsSqlData.getScheme());
            mPreferenceTool.setPortal(accountsSqlData.getPortal());
            mPreferenceTool.setToken(accountsSqlData.getToken());
            mPreferenceTool.setSslState(accountsSqlData.isSslState());
            mPreferenceTool.setSslCiphers(accountsSqlData.isSslCiphers());
        }
    }
}
