package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
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
                ExplorerContextItem.Edit,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Room && state.isRoot -> listOf(
                ExplorerContextItem.Header(
                    state = state,
                    logo = if (state.item is CloudFolder) state.item.logo?.large.takeIf { !it.isNullOrEmpty() } else null
                ),
                ExplorerContextItem.Share,
                ExplorerContextItem.RoomInfo,
                ExplorerContextItem.AddUsers,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Pin(state.pinned),
                ExplorerContextItem.Archive,
                ExplorerContextItem.Restore(true),
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Storage -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit,
                ExplorerContextItem.Send,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Device -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit,
                ExplorerContextItem.Send,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Upload,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Delete(state)
            )

            else -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit,
                ExplorerContextItem.Share,
                ExplorerContextItem.ExternalLink,
                ExplorerContextItem.Favorites(preferenceTool.isFavoritesEnabled, state.item.favorite),
                ExplorerContextItem.Send,
                ExplorerContextItem.Location,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Upload,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Restore(false),
                ExplorerContextItem.ShareDelete,
                ExplorerContextItem.Delete(state)
            )
        }.mapNotNull { item -> item.get(state) }.sortedBy { item -> item.order }
    }

}
