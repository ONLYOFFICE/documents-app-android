package app.editors.manager.mvp.views.main

import android.net.Uri
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import app.documents.core.model.cloud.Access
import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.models.states.OperationsState
import app.editors.manager.mvp.views.base.BaseViewExt
import app.editors.manager.ui.views.custom.PlaceholderViews
import lib.toolkit.base.managers.utils.EditType
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import java.io.File

@StateStrategyType(OneExecutionStateStrategy::class)
interface DocsBaseView : BaseViewExt {

    /*
     * Get docs
     */
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDocsGet(@Nullable list: List<Entity>?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDocsRefresh(@Nullable list: List<Entity>?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDocsFilter(@Nullable list: List<Entity>?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDocsNext(@Nullable list: List<Entity>?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDocsBatchOperation()

    /*
     * States for update
     */
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateUpdateRoot(isRoot: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateUpdateFilter(isFilter: Boolean, @Nullable value: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateUpdateSelection(isSelection: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateUpdateThumbnail(id: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateAdapterRoot(isRoot: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateMenuDefault(@NonNull sortBy: String, isAsc: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateMenuSelection()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateMenuEnabled(isEnabled: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateActionButton(isVisible: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onStateEmptyBackStack()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onClearMenu()

    /*
     * Change docs
     */
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onCreateFile(file: CloudFile)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDeleteBatch(list: List<Entity>)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDeleteMessage(count: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onRename(item: Item, position: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onBatchMoveCopy(
        @NonNull operation: OperationsState.OperationType,
        @NonNull explorer: Explorer
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onPickCloudFile(destFolderId: String)

    /*
     * On click
     */
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onActionBarTitle(title: String = "")

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onItemsSelection(countSelected: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onItemSelected(position: Int, countSelected: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onActionDialog(isThirdParty: Boolean, isDocs: Boolean, roomType: Int?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDownloadActivity(uri: Uri?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFileMedia(explorer: Explorer, isWebDav: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFileDownloadPermission()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFileUploadPermission(@Nullable extension: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onScrollToPosition(position: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onSwipeEnable(isSwipeEnable: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onPlaceholder(type: PlaceholderViews.Type)

    /*
     * On views
     */
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogClose(force: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogWaiting(@Nullable title: String?, @Nullable tag: String?, force: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogDownloadWaiting()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogQuestion(@Nullable title: String?, @Nullable question: String?, @Nullable tag: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogWarning(title: String, message: String, @Nullable tag: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogDelete(count: Int, toTrash: Boolean, tag: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogProgress(@Nullable title: String?, isHideButtons: Boolean, @Nullable tag: String?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onDialogProgress(total: Int, progress: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onSnackBar(message: String? = "")

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onSnackBarWithAction(
        @NonNull message: String,
        @NonNull button: String,
        @NonNull action: View.OnClickListener
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onOpenDocumentServer(file: CloudFile, info: String, type: EditType)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onOpenLocalFile(
        file: CloudFile,
        editType: EditType,
        access: Access = Access.None
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onOpenLocalFile(
        uri: Uri,
        extension: String,
        editType: EditType,
        access: Access = Access.None
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onNoProvider()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onCreateDownloadFile(name: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onUpdateItemState()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onFinishDownload(uri: Uri?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onShowCamera(@NonNull photoUri: Uri, @NonNull isOCR: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onSendCopy(@NonNull file: File)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onSetGridView(isGrid: Boolean)
}