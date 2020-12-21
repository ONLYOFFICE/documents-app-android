package app.editors.manager.mvp.views.login;

import androidx.annotation.StringRes;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface EnterpriseCreateValidateView extends BaseView {
    void onValidatePortalSuccess();
    void onPortalNameError(String message);
    void onEmailNameError(String message);
    void onFirstNameError(String message);
    void onLastNameError(String message);
    void onRegionDomain(String domain);
    void onShowWaitingDialog(@StringRes int title);
}