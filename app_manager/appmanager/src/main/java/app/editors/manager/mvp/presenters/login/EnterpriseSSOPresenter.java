package app.editors.manager.mvp.presenters.login;

import app.editors.manager.app.App;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.EnterpriseSSOView;

public class EnterpriseSSOPresenter extends BaseLoginPresenter<EnterpriseSSOView, ResponseSignIn>{

    public static final String TAG = EnterpriseSSOPresenter.class.getSimpleName();

    public EnterpriseSSOPresenter() { App.getApp().getAppComponent().inject(this);}

    public void signInWithSSO(String token, String portal) {
        mPreferenceTool.setSocialToken(token);
        mPreferenceTool.setToken(token);
        mPreferenceTool.setPortal(portal);
        getUser(token, null);
    }

    @Override
    protected void onGetUser(User user) {
        super.onGetUser(user);
        getViewState().onSuccessLogin();
    }

}
