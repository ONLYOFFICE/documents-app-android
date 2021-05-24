package app.editors.manager.mvp.presenters.login;

import app.editors.manager.app.App;
import app.editors.manager.mvp.models.request.RequestPassword;
import app.editors.manager.mvp.models.response.ResponsePassword;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.views.login.EnterpriseSSOView;
import app.editors.manager.mvp.views.login.PasswordRecoveryView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;

import static app.editors.manager.app.Api.PERSONAL_HOST;

@InjectViewState
public class PasswordRecoveryPresenter extends BaseLoginPresenter<PasswordRecoveryView, ResponseSignIn>{
    public static final String TAG = PasswordRecoveryPresenter.class.getSimpleName();

    protected Disposable mDisposable;

    public PasswordRecoveryPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    public void recoverPassword(String email, Boolean isPersonal) {
        if(!StringUtils.isEmailValid(email)) {
            getViewState().onEmailError();
        } else {
            sendEmailNotification(email, isPersonal);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void sendEmailNotification(String email, Boolean isPersonal) {

        RequestPassword requestPassword = new RequestPassword();
        if(!isPersonal) {
            requestPassword.setPortal(mPreferenceTool.getPortal());
        } else {
            requestPassword.setPortal(PERSONAL_HOST);
        }
        requestPassword.setEmail(email);

        mDisposable = mRetrofitTool.getApiWithPreferences().forgotPassword(requestPassword)
                .map(ResponsePassword::getResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> {
                    getViewState().onPasswordRecoverySuccess(email);
                }, throwable -> {
                    getViewState().onError(throwable.getMessage());
                });

    }
}
