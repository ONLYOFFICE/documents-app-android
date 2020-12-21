package app.editors.manager.mvp.presenters.login;

import android.accounts.Account;

import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import moxy.InjectViewState;

@InjectViewState
public class PersonalLoginPresenter extends EnterpriseLoginPresenter {

    public static final String TAG = PersonalLoginPresenter.class.getSimpleName();

    public PersonalLoginPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    private boolean initPersonal()  {
        try {
            mPreferenceTool.setDefault();
            initRetrofitPref(Api.PERSONAL_HOST);
        } catch (UrlSyntaxMistake e) {
            return false;
        }

        return true;
    }

    public void signInPersonal(final String login, final String password) {
        if (initPersonal()) {
            signInPortal(login.trim(), password, Api.PERSONAL_HOST);
        }
    }

    public void signInPersonalWithTwitter(final String token) {
        if (initPersonal()) {
            signInWithTwitter(token, Api.PERSONAL_HOST);
        }
    }

    public void signInPersonalWithGoogle(final Account token) {
        if (initPersonal()) {
            signInWithGoogle(token, Api.PERSONAL_HOST);
        }
    }

    public void signInPersonalWithFacebook(final String token) {
        if (initPersonal()) {
            signInWithFacebook(token, Api.PERSONAL_HOST);
        }
    }
}
