package app.editors.manager.mvp.views.login;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface WebDavSignInView extends BaseView {
    void onDialogWaiting(String string);
    void onDialogClose();
    void onLogin();
    void onUrlError(String string);
    void onNextCloudLogin(String url);
}
