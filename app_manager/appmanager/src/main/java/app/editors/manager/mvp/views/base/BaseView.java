package app.editors.manager.mvp.views.base;


import androidx.annotation.Nullable;

import moxy.MvpView;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface BaseView extends MvpView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onError(@Nullable String message);
}