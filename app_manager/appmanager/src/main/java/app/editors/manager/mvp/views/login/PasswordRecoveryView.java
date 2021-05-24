package app.editors.manager.mvp.views.login;

import app.editors.manager.mvp.views.base.BaseView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface PasswordRecoveryView extends BaseView {
    void onPasswordRecoverySuccess(String email);
    void onEmailError();
}
