package app.editors.manager.mvp.views.login;

import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(SkipStrategy.class)
public interface EnterprisePhoneView extends BaseView {
    void onSuccessChange();
}