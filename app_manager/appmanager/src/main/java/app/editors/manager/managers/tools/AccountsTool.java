package app.editors.manager.managers.tools;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;

public class AccountsTool {

    private static final String KEY_PORTAL = "KEY_1";
    private static final String KEY_LOGIN = "KEY_2";
    private static final String KEY_TOKEN = "KEY_3";
    private static final String KEY_AVATAR = "KEY_4";

    private Context mContext;
    private PreferenceTool mPreferenceTool;
    private AccountManager mAccountManager;

    public AccountsTool(Context context, PreferenceTool preferenceTool) {
        mContext = context;
        mPreferenceTool = preferenceTool;
        mAccountManager = AccountManager.get(mContext);
    }

    public boolean create(final String portal, final String login, final String password, final String authToken, final String avatarUrl) {
        final Account account = new Account(portal + "/" + login, mContext.getString(R.string.app_package));
        final boolean isCreate = mAccountManager.addAccountExplicitly(account, password, null);
        mAccountManager.setUserData(account, KEY_PORTAL, portal);
        mAccountManager.setUserData(account, KEY_LOGIN, login);
        mAccountManager.setUserData(account, KEY_TOKEN, authToken);
        mAccountManager.setUserData(account, KEY_AVATAR, avatarUrl);
        return isCreate;
    }

    public void remove(final Account account) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mAccountManager.removeAccountExplicitly(account);
        } else {
            mAccountManager.removeAccount(account, null, null);
        }
    }

    public List<UserData> getAccounts() {
        final List<UserData> usersList = new ArrayList<>();
        final Account[] accounts = mAccountManager.getAccountsByType(mContext.getString(R.string.app_package));
        for (Account account : accounts) {
            final String password = mAccountManager.getPassword(account);
            final String portal = mAccountManager.getUserData(account, KEY_PORTAL);
            final String login = mAccountManager.getUserData(account, KEY_LOGIN);
            final String token = mAccountManager.getUserData(account, KEY_TOKEN);
            final String avatarUrl = mAccountManager.getUserData(account, KEY_AVATAR);
            usersList.add(new UserData(account, portal, login, token, avatarUrl, password));
        }

        return usersList;
    }

    public static class UserData {

        public final Account mAccount;
        public final String mPortal;
        public final String mLogin;
        public final String mToken;
        public final String mAvatarUrl;
        public final String mPassword;

        public UserData(Account account, String portal, String login, String token, String avatarUrl, String password) {
            mAccount = account;
            mPortal = portal;
            mLogin = login;
            mToken = token;
            mAvatarUrl = avatarUrl;
            mPassword = password;
        }
    }

}
