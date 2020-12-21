package app.editors.manager.mvp.views.login;

import androidx.annotation.Nullable;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.views.base.BaseView;


@StateStrategyType(OneExecutionStateStrategy.class)
public interface SplashView extends BaseView {
    void onSuccessToken();
    void onGoToSignIn(@Nullable String message);
}