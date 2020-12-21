package app.editors.manager.mvp.models.account;


import android.accounts.Account;

public class AccountsManagerData extends Accounts {

    public final Account mAccount;

    public AccountsManagerData(Account account, String portal, String login, String scheme,
                               String name, String token, String provider, String avatarUrl) {
        super(portal, login, scheme, name, token, provider, avatarUrl);
        mAccount = account;
    }

}
