package app.editors.manager.mvp.views.storage;

import java.util.List;

import app.editors.manager.mvp.models.account.Storage;
import app.editors.manager.mvp.views.base.BaseView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface SelectView extends BaseView {

    void onUpdate(List<String> storages);
    void showWebTokenFragment(Storage storage);
    void showWebDavFragment(String providerKey, String url, String title);
    void showProgress(boolean isVisible);
}
