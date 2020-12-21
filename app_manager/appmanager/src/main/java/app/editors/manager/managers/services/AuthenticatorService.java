package app.editors.manager.managers.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import app.editors.manager.managers.accounts.AuthenticatorAccounts;

public class AuthenticatorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        AuthenticatorAccounts authenticator = new AuthenticatorAccounts(this);
        return authenticator.getIBinder();
    }

}