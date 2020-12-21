package app.editors.manager.mvp.views.main;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface AppSettingsView extends MvpView {

    void onSetCacheSize(String size);

    void onSetWifiState(boolean state);

    void onMessage(String message);

    void onAnalyticState(boolean isAnalyticEnable);
}
