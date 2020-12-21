package app.editors.manager.mvp.views.main;

import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface DocsWebDavView extends DocsBaseView {

    @StateStrategyType(SkipStrategy.class)
    void onActionDialog();
}
