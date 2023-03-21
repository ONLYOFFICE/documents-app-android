package app.editors.manager.mvp.views.storage;

import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import app.documents.core.network.manager.models.explorer.CloudFolder;
import app.editors.manager.mvp.views.base.BaseViewExt;


@StateStrategyType(SkipStrategy.class)
public interface ConnectView extends BaseViewExt {
    void onConnect(CloudFolder folder);
}