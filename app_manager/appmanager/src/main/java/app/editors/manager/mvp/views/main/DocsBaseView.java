package app.editors.manager.mvp.views.main;

import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

import app.documents.core.network.manager.models.base.Entity;
import app.documents.core.network.manager.models.explorer.CloudFile;
import app.documents.core.network.manager.models.explorer.CloudFolder;
import app.documents.core.network.manager.models.explorer.Explorer;
import app.documents.core.network.manager.models.explorer.Item;
import app.editors.manager.mvp.models.states.OperationsState;
import app.editors.manager.mvp.views.base.BaseViewExt;
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
    void onDeleteMessage(int count);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onRename(Item item, int position);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onBatchMoveCopy(@NonNull OperationsState.OperationType operation, @NonNull Explorer explorer);

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
    void onDialogDownloadWaiting();
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogQuestion(@Nullable String title, @Nullable String question, @Nullable String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogDelete(int count, boolean toTrash, String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogProgress(@Nullable String title, boolean isHideButtons, @Nullable String tag);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onDialogProgress(int total, int progress);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void onSnackBar(String message);
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
    void onUpdateFavoriteItem();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onFinishDownload(Uri uri);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onShowCamera(Uri photoUri);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onSendCopy(@NonNull File file);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onOpenDocumentServer(@Nullable CloudFile file, @Nullable String info);
}