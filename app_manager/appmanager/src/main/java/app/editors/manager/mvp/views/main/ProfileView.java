package app.editors.manager.mvp.views.main;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.user.Thirdparty;
import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface ProfileView extends BaseView {
    void onWebDavState();
    void onCloudState();
    void onOnlineState();
    void onEmptyThirdparty();
    void onSetServices(List<Thirdparty> list);
    void onAccountType(String type);
    void onClose();
}
