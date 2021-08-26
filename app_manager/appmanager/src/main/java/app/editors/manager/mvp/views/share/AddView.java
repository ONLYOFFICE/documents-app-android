package app.editors.manager.mvp.views.share;

import androidx.annotation.Nullable;
import lib.toolkit.base.ui.adapters.holder.ViewType;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import java.util.List;
import app.editors.manager.mvp.views.base.BaseViewExt;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface AddView extends BaseViewExt {
    void onGetUsers(List<ViewType> list);
    void onGetGroups(List<ViewType> list);
    void onGetCommon(List<ViewType> list);
    void onSuccessAdd();
    void onSearchValue(@Nullable String value);
}