package app.editors.manager.managers.tools;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.AccountsManagerData;

public class AccountManagerTool {

    private static final String KEY_PORTAL = "KEY_1";
    private static final String KEY_LOGIN = "KEY_2";
    private static final String KEY_SCHEME = "KEY_3";
    private static final String KEY_NAME = "KEY_4";
    private static final String KEY_TOKEN = "KEY_5";
    private static final String KEY_PROVIDER = "KEY_6";
    private static final String KEY_AVATAR = "KEY_7";

    private Context mContext;
    private AccountManager mAccountManager;

    public AccountManagerTool(Context context) {
        mContext = context;
        mAccountManager = AccountManager.get(mContext);
    }

    public boolean setAccount(final String portal, final String login, final String scheme,
                              final String name, final String provider, final String token, final String avatarUrl) {
        final android.accounts.Account account = new android.accounts.Account(portal + "/" + login, mContext.getString(R.string.app_package));
        final boolean isCreate = mAccountManager.addAccountExplicitly(account, null, null);
        mAccountManager.setUserData(account, KEY_PORTAL, portal);
        mAccountManager.setUserData(account, KEY_LOGIN, login);
        mAccountManager.setUserData(account, KEY_SCHEME, scheme);
        mAccountManager.setUserData(account, KEY_NAME, name);
        mAccountManager.setUserData(account, KEY_TOKEN, token);
        mAccountManager.setUserData(account, KEY_PROVIDER, provider);
        mAccountManager.setUserData(account, KEY_AVATAR, avatarUrl);
        return isCreate;
    }

    public List<AccountsManagerData> getAccounts() {
        final List<AccountsManagerData> usersList = new ArrayList<>();
        final android.accounts.Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string.app_package));
        for (android.accounts.Account account : accounts) {
            final String provider = mAccountManager.getUserData(account, KEY_PROVIDER);
            final String portal = mAccountManager.getUserData(account, KEY_PORTAL);
            final String login = mAccountManager.getUserData(account, KEY_LOGIN);
            final String scheme = mAccountManager.getUserData(account, KEY_SCHEME);
            final String name = mAccountManager.getUserData(account, KEY_NAME);
            final String token = mAccountManager.getUserData(account, KEY_TOKEN);
            final String avatarUrl = mAccountManager.getUserData(account, KEY_AVATAR);
            usersList.add(new AccountsManagerData(account, portal, login, scheme, name, token, provider, avatarUrl));
        }

        return usersList;
    }

    public void delete(final android.accounts.Account account) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccountExplicitly(account);
        } else {
            mAccountManager.removeAccount(account, null, null);
        }
    }

}
