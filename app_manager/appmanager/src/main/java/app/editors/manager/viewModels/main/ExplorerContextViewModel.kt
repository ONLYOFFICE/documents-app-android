package app.editors.manager.viewModels.main

import androidx.lifecycle.ViewModel
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.isFavorite
import app.documents.core.network.manager.models.explorer.isLocked
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
                ExplorerContextItem.View,
                ExplorerContextItem.Delete(state)
            )

            state.section.isRoom && state.isRoot -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.RoomInfo,
                ExplorerContextItem.Reconnect,
                ExplorerContextItem.Pin(state.pinned),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.AddUsers,
                ExplorerContextItem.ExternalLink(state),
                ExplorerContextItem.Notifications((state.item as? CloudFolder)?.mute == true),
                ExplorerContextItem.SaveAsTemplate,
                ExplorerContextItem.Duplicate,
                ExplorerContextItem.Download,
                ExplorerContextItem.Archive,
                ExplorerContextItem.Restore,
                ExplorerContextItem.Delete(state)
            )

            state.section.isTemplates -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.RoomInfo,
                ExplorerContextItem.CreateRoom,
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.AccessSettings,
                ExplorerContextItem.ExternalLink(state),
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Storage -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.View,
                ExplorerContextItem.Send,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Device -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Fill(),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.View,
                ExplorerContextItem.Send,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Upload,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Delete(state)
            )

            state.section is ApiContract.Section.Trash && state.provider == PortalProvider.Cloud.DocSpace -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Restore,
                ExplorerContextItem.Delete(state)
            )

            else -> listOf(
                ExplorerContextItem.Header(state),
                ExplorerContextItem.Fill(),
                ExplorerContextItem.Edit(state),
                ExplorerContextItem.View,
                ExplorerContextItem.StartFilling,
                ExplorerContextItem.VersionHistory,
                ExplorerContextItem.FillingStatus.takeIf { state.roomType == ApiContract.RoomType.VIRTUAL_ROOM },
                ExplorerContextItem.ResetFilling,
                ExplorerContextItem.Share,
                ExplorerContextItem.CreateRoom.takeIf { state.provider == PortalProvider.Cloud.DocSpace },
                ExplorerContextItem.ExternalLink(state),
                ExplorerContextItem.Favorites(preferenceTool.isFavoritesEnabled, state.item.isFavorite),
                ExplorerContextItem.CustomFilter((state.item as? CloudFile)?.customFilterEnabled),
                ExplorerContextItem.Send,
                ExplorerContextItem.Location,
                ExplorerContextItem.Move,
                ExplorerContextItem.Copy,
                ExplorerContextItem.Download,
                ExplorerContextItem.Upload,
                ExplorerContextItem.EditIndex,
                ExplorerContextItem.Rename,
                ExplorerContextItem.Restore,
                ExplorerContextItem.ShareDelete,
                ExplorerContextItem.StopFilling.takeIf { state.roomType == ApiContract.RoomType.VIRTUAL_ROOM },
                ExplorerContextItem.Delete(state),
                ExplorerContextItem.Lock(state.item.isLocked)
            )
        }.filterNotNull()
            .mapNotNull { item -> item.get(state) }
            .sortedBy { item -> item.order }
    }

}
