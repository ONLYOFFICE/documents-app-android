package app.editors.manager.mvp.views.share;

import androidx.annotation.Nullable;

import java.util.List;

import app.editors.manager.mvp.models.ui.UserUi;
import app.editors.manager.mvp.views.base.BaseViewExt;
import lib.toolkit.base.ui.adapters.holder.ViewType;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface AddView extends BaseViewExt {
    void onGetUsers(List<ViewType> list);
    void onGetGroups(List<ViewType> list);
    void onGetCommon(List<ViewType> list);
    void onSuccessAdd();
    void onSearchValue(@Nullable String value);
}