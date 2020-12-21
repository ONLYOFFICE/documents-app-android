package app.editors.manager.mvp.views.share;

import androidx.annotation.Nullable;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.views.base.BaseViewExt;


@StateStrategyType(OneExecutionStateStrategy.class)
public interface AddView extends BaseViewExt {
    void onGetUsers(List<Entity> list);
    void onGetGroups(List<Entity> list);
    void onGetCommon(List<Entity> list);
    void onSuccessAdd();
    void onSearchValue(@Nullable String value);
}