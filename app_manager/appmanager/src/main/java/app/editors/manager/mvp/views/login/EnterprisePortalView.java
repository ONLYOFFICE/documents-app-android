package app.editors.manager.mvp.views.login;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface EnterprisePortalView extends BaseView {
    void onSuccessPortal(String portal);
    void onHttpPortal(String portal);
    void onPortalSyntax(String message);
    void onLoginPortal(String portal);
    void onShowDialog();
}