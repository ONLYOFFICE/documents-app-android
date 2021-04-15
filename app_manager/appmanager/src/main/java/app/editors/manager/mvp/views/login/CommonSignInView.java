package app.editors.manager.mvp.views.login;

import android.content.Intent;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.mvp.views.base.BaseView;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface CommonSignInView extends BaseView {
    void onSuccessLogin();
    void onTwoFactorAuth(final boolean isPhone, AccountsSqlData sqlData);
    void onGooglePermission(final Intent intent);
    void onEmailNameError(final String message);
    void onTwoFactorAuthTfa(boolean isSecret, AccountsSqlData sqlData);
    void onWaitingDialog(@NonNull String message, @Nullable String tag);
    void showGoogleLogin(boolean isShow);
    void showFacebookLogin(boolean isShow);
    void onSuccessSendEmail(String message);
}