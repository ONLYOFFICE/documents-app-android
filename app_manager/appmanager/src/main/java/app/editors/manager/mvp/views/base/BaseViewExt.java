package app.editors.manager.mvp.views.base;

import androidx.annotation.Nullable;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface BaseViewExt extends BaseView {
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onUnauthorized(@Nullable String message);
}