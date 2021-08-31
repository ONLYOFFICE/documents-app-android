package app.editors.manager.mvp.views.main;

import app.editors.manager.mvp.presenters.main.OpenState;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;
import android.net.Uri;


import java.util.List;

import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.Item;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface DocsOnDeviceView extends DocsBaseView {
    void onActionDialog();
    void onRemoveItem(Item item);
    void onRemoveItems(List<Item> items);
    void onShowCamera(Uri photoUri);
    void onShowFolderChooser();
    void onOpenMedia(OpenState.Media state);

    //Open file
    void onShowDocs(Uri uri);
    void onShowCells(Uri uri);
    void onShowSlides(Uri uri);
    void onShowPdf(Uri uri);
}
