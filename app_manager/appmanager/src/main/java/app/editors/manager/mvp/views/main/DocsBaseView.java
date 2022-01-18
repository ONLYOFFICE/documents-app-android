package app.editors.manager.mvp.views.main;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.CloudFile;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.views.base.BaseViewExt;
import app.editors.manager.ui.dialogs.ContextBottomDialog;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;


@StateStrategyType(OneExecutionStateStrategy.class)
public interface DocsBaseView extends BaseViewExt {

    /*
     * Get docs
     * */
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsGet(@Nullable List<Entity> list);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsRefresh(@Nullable List<Entity> list);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsFilter(@Nullable List<Entity> list);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsNext(@Nullable List<Entity> list);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsAccess(boolean isAccess, @NonNull String message);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDocsBatchOperation();

    /*
     * States for update
     * */
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateUpdateRoot(boolean isRoot);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateUpdateFilter(boolean isFilter, @Nullable String value);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateUpdateSelection(boolean isSelection);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateAdapterRoot(boolean isRoot);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateMenuDefault(@NonNull String sortBy, boolean isAsc);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateMenuSelection();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateMenuEnabled(boolean isEnabled);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateActionButton(boolean isVisible);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onStateEmptyBackStack();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onClearMenu();

    /*
     * Change docs
     * */
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCreateFolder(CloudFolder folder);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCreateFile(CloudFile file);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDeleteBatch(List<Entity> list);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRename(Item item, int position);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onBatchMove(@NonNull Explorer explorer);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onBatchCopy(@NonNull Explorer explorer);

    /*
     * On click
     * */
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onActionBarTitle(String title);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemsSelection(String countSelected);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemSelected(int position, String countSelected);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onItemContext(@NonNull ContextBottomDialog.State state);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onActionDialog(boolean isThirdParty, boolean isShowDocs);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDownloadActivity(Uri uri);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFileMedia(Explorer explorer, boolean isWebDav);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFileDownloadPermission();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFileUploadPermission();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onScrollToPosition(int position);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onSwipeEnable(boolean isSwipeEnable);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onPlaceholder(PlaceholderViews.Type type);

    /*
     * On views
     * */
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogClose();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogWaiting(@Nullable String title, @Nullable String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogQuestion(@Nullable String title, @Nullable String question, @Nullable String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogProgress(@Nullable String title, boolean isHideButtons, @Nullable String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogProgress(int total, int progress);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onSnackBar(@NonNull String message);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onSnackBarWithAction(@NonNull String message, @NonNull String button, @NonNull View.OnClickListener action);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onUploadFileProgress(int progress, String id);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRemoveUploadHead();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDeleteUploadFile(String id);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onAddUploadsFile(List<? extends Entity> uploadFiles);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onOpenLocalFile(CloudFile file);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onNoProvider();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCreateDownloadFile(String name);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRemoveItemFromFavorites();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onReverseSortOrder(String order);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFinishDownload(Uri uri);
}