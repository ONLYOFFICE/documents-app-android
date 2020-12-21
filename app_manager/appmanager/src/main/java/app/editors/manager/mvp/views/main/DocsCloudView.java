package app.editors.manager.mvp.views.main;;

import java.util.ArrayList;

import app.editors.manager.mvp.models.explorer.File;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface DocsCloudView extends DocsBaseView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFileWebView(File file);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void showMoveCopyDialog(ArrayList<String> names, String action, String title);

}
