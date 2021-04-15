package app.editors.manager.mvp.presenters.login;

import android.content.Intent;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.exceptions.UrlSyntaxMistake;
import app.editors.manager.managers.tools.RetrofitTool;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.models.request.RequestPassword;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.response.ResponseCapabilities;
import app.editors.manager.mvp.models.response.ResponsePassword;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.mvp.views.login.CommonSignInView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lib.toolkit.base.managers.utils.StringUtils;
import moxy.InjectViewState;
import retrofit2.Response;

@InjectViewState
public class EnterpriseLoginPresenter extends BaseLoginPresenter<CommonSignInView, ResponseSignIn> {

    public static final String TAG = EnterpriseLoginPresenter.class.getSimpleName();

    public static final String TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING";
    public static final String TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK";
    public static final String TAG_DIALOG_FORGOT_PASSWORD = "TAG_DIALOG_LOGIN_FORGOT_PASSWORD";

    protected Disposable mDisposable;

    public EnterpriseLoginPresenter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onTwoFactorAuth(boolean isPhone, AccountsSqlData sqlData) {
        super.onTwoFactorAuth(isPhone, sqlData);
        getViewState().onTwoFactorAuth(isPhone, sqlData);
    }

    @Override
    protected void onGetUser(User user) {
        super.onGetUser(user);
        getViewState().onSuccessLogin();
    }

    @Override
    protected void onTwoFactorAuthApp(boolean isSecret, AccountsSqlData sqlData) {
        super.onTwoFactorAuthApp(isSecret, sqlData);
        getViewState().onTwoFactorAuthTfa(isSecret, sqlData);
    }

    @Override
    protected void onGooglePermission(Intent intent) {
        super.onGooglePermission(intent);
        getViewState().onGooglePermission(intent);
    }

    public void signInPortal(final String login, final String password, final String portal) {
//        mPreferenceTool.setLogin(login);
        if (!StringUtils.isEmailValid(login)) {
            getViewState().onEmailNameError(mContext.getString(R.string.errors_email_syntax_error));
            return;
        }

        getViewState().onWaitingDialog(mContext.getString(R.string.dialogs_sign_in_portal_header_text), TAG_DIALOG_WAITING);
        final RequestSignIn requestSignIn = new RequestSignIn();
        requestSignIn.setUserName(login);
        requestSignIn.setPassword(password);
        signIn(requestSignIn, portal);
    }

    public void checkProviders() {
        final String portal = mPreferenceTool.getPortalFullPath();
        if (portal != null && !portal.isEmpty()) {
            try {
                new RetrofitTool(mContext)
                        .setSslOn(mPreferenceTool.getSslState())
                        .setCiphers(mPreferenceTool.getSslCiphers())
                        .init(portal)
                        .getApi("")
                        .capabilities().enqueue(new CommonCallback<ResponseCapabilities>() {
                    @Override
                    public void onSuccessResponse(Response<ResponseCapabilities> response) {
                        final List<String> providers = response.body().getResponse().getProviders();
                        if (providers != null && !providers.isEmpty()) {
                            getViewState().showGoogleLogin(providers.contains("google"));
                            getViewState().showFacebookLogin(providers.contains("facebook"));
                        }
                    }
                });
            } catch (UrlSyntaxMistake urlSyntaxMistake) {
                urlSyntaxMistake.printStackTrace();
            }
        }
    }

    public void checkEmail(String email) {
        if(!StringUtils.isEmailValid(email)) {
            getViewState().onError(mContext.getString(R.string.errors_email_syntax_error));
        } else {
            sendEmailNotification(email);
        }
    }

    private void sendEmailNotification(String email) {

        RequestPassword requestPassword = new RequestPassword();
        requestPassword.setPortal(mPreferenceTool.getPortal());
        requestPassword.setEmail(email);

        mDisposable = mRetrofitTool.getApiWithPreferences().forgotPassword(requestPassword)
                .map(ResponsePassword::getResponse)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> {
                    getViewState().onSuccessSendEmail(message);
                }, throwable -> {
                    getViewState().onError(throwable.getMessage());
                });

    }

}
