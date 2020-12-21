package app.editors.manager.mvp.views.main;

import android.net.Uri;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.views.base.BaseView;
import app.editors.manager.ui.dialogs.ContextBottomDialog;

@StateStrategyType(OneExecutionStateStrategy.class)
public interface DocsRecentView extends DocsBaseView {

    void updateFiles(List<Entity> lastModifiedFiles);
    void openFile(File response);
    void onMoveElement(Recent recent, int position);
    void onContextShow(ContextBottomDialog.State state);
    void onDeleteItem(int mContextPosition);
    void onOpenDocs(Uri uri);
    void onOpenCells(Uri uri);
    void onOpenPresentation(Uri uri);
    void onOpenPdf(Uri uri);
    void onOpenMedia(Explorer images, boolean isWebDav);
}
