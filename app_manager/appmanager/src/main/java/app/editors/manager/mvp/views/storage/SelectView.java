package app.editors.manager.mvp.views.storage;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.managers.utils.StorageUtils;
import app.editors.manager.mvp.views.base.BaseView;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface SelectView extends BaseView {

    void onUpdate(List<String> storages);
    void showWebTokenFragment(StorageUtils.Storage storage);
    void showWebDavFragment(String providerKey, String url, String title);
    void showProgress(boolean isVisible);
}
