package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.dialogs.explorer.ExplorerContextState
import javax.inject.Inject

class ExplorerContextViewModel : ViewModel() {

    @Inject
    lateinit var preferenceTool: PreferenceTool

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getContextItems(state: ExplorerContextState?): List<ExplorerContextItem> {
        return getItems(state ?: return emptyList())
    }

    private fun getItems(state: ExplorerContextState): List<ExplorerContextItem> {
        return when {

            state.section == ApiContract.Section.Recent -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.Delete(state)
            )

            state.section.isRoom && state.isRoot -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.RoomInfo,
                ExplorerContextItem.Pin(state.pinned),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.AddUsers,
                ExplorerContextItem.ExternalLink(state),
                ExplorerContextItem.Download,
                ExplorerContextItem.Archive,
                ExplorerContextItem.Restore,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Storage -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.Send,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Device -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.Send,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Upload,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Delete(state)
            )

            else -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.Share,
                ExplorerContextItem.CreateRoom,
                ExplorerContextItem.ExternalLink(state),
                ExplorerContextItem.Favorites(preferenceTool.isFavoritesEnabled, state.item.favorite),
                ExplorerContextItem.Send,
                ExplorerContextItem.Location,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Upload,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Restore,
                ExplorerContextItem.ShareDelete,
                ExplorerContextItem.Delete(state)
            )
        }.mapNotNull { item -> item.get(state) }.sortedBy { item -> item.order }
    }

}
