package app.editors.manager.ui.popup

import android.content.Context
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.mvp.models.states.OperationsState
import lib.toolkit.base.ui.popup.ActionBarPopup
import lib.toolkit.base.ui.popup.BasePopupItem

sealed class SelectPopupItem(title: Int) : BasePopupItem(title) {
    object Deselect : SelectPopupItem(R.string.toolbar_menu_main_deselect)
    object SelectAll : SelectPopupItem(R.string.toolbar_menu_main_select_all)
    object Download : SelectPopupItem(R.string.toolbar_menu_main_download)

    sealed class Operation(title: Int, val value: OperationsState.OperationType) :
        SelectPopupItem(title) {
        object Move : Operation(R.string.toolbar_menu_main_move, OperationsState.OperationType.MOVE)
        object Copy : Operation(R.string.toolbar_menu_main_copy, OperationsState.OperationType.COPY)
        object Restore : Operation(R.string.device_trash_files_restore, OperationsState.OperationType.RESTORE)
    }
}

class SelectPopup(
    context: Context,
    section: Int,
    clickListener: (SelectPopupItem) -> Unit,
    excluded: List<SelectPopupItem> = emptyList()
) : ActionBarPopup<SelectPopupItem>(
    context = context,
    items = getItems(section).filter(excluded),
    clickListener = clickListener
) {

    companion object {
        private fun getItems(section: Int): List<SelectPopupItem> =
            listOfNotNull(
                SelectPopupItem.Deselect,
                SelectPopupItem.SelectAll,
                SelectPopupItem.Download,
                SelectPopupItem.Operation.Move,
                SelectPopupItem.Operation.Copy,
                SelectPopupItem.Operation.Restore.takeIf { section == ApiContract.SectionType.CLOUD_TRASH }
            )
    }

}