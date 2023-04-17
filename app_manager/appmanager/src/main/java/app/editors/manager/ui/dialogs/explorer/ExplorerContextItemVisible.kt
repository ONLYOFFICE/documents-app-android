package app.editors.manager.ui.dialogs.explorer

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import lib.toolkit.base.managers.utils.StringUtils


interface ExplorerContextItemVisible {

    fun ExplorerContextState.visible(contextItem: ExplorerContextItem): Boolean {
        return when (contextItem) {
            ExplorerContextItem.Archive -> archive
            ExplorerContextItem.Copy -> copy
            ExplorerContextItem.Download -> download
            ExplorerContextItem.Edit -> edit
            ExplorerContextItem.ExternalLink -> externalLink
            ExplorerContextItem.Location -> location
            ExplorerContextItem.Move -> move
            ExplorerContextItem.Rename -> rename
            ExplorerContextItem.Restore -> restore
            ExplorerContextItem.RoomInfo -> roomInfo
            ExplorerContextItem.Send -> send
            ExplorerContextItem.Share -> share
            ExplorerContextItem.ShareDelete -> shareDelete
            ExplorerContextItem.Upload -> upload
            is ExplorerContextItem.Header -> true
            is ExplorerContextItem.Pin -> pin
            is ExplorerContextItem.Delete -> delete
            is ExplorerContextItem.Favorites -> favorites(contextItem.enabled)
        }
    }

    private val ExplorerContextState.archive: Boolean
        get() = item.security.moveTo && item.security.editRoom && section !is ApiContract.Section.Room.Archive

    private val ExplorerContextState.copy: Boolean
        get() = section != ApiContract.Section.Trash

    private val ExplorerContextState.download: Boolean
        get() = !section.isLocal

    private val ExplorerContextState.edit: Boolean
        get() = if (item is CloudFile && isExtensionEditable(item.fileExst)) {
            when (section) {
                ApiContract.Section.Recent,
                ApiContract.Section.User,
                ApiContract.Section.Device -> true
                ApiContract.Section.Trash -> false
                else -> access != ApiContract.Access.Read
            }
        } else false

    private val ExplorerContextState.move: Boolean
        get() = section in listOf(ApiContract.Section.User, ApiContract.Section.Device)

    private val ExplorerContextState.externalLink: Boolean
        get() = isShareVisible(access, section) && !isFolder

    private val ExplorerContextState.rename: Boolean
        get() = access == ApiContract.Access.ReadWrite || section in listOf(
            ApiContract.Section.User,
            ApiContract.Section.Device
        )

    private val ExplorerContextState.restore: Boolean
        get() = section == ApiContract.Section.Trash


    private val ExplorerContextState.roomInfo: Boolean
        get() = section.isRoom

    private val ExplorerContextState.send: Boolean
        get() = section !is ApiContract.Section.Storage && section != ApiContract.Section.Trash && !isFolder

    private val ExplorerContextState.share: Boolean
        get() = isShareVisible(access, section)


    private val ExplorerContextState.shareDelete: Boolean
        get() = section == ApiContract.Section.Share


    private val ExplorerContextState.upload: Boolean
        get() = section == ApiContract.Section.Device

    private val ExplorerContextState.pin: Boolean
        get() = item.security.pin

    private val ExplorerContextState.delete: Boolean
        get() = section !in listOf(
            ApiContract.Section.Share,
            ApiContract.Section.Favorites,
            ApiContract.Section.Projects
        )

    private val ExplorerContextState.location: Boolean
        get() = isSearching

    private fun ExplorerContextState.favorites(enabled: Boolean): Boolean =
        enabled && !isFolder && section != ApiContract.Section.Trash


    private fun isExtensionEditable(ext: String): Boolean {
        return StringUtils.getExtension(ext) in listOf(
            StringUtils.Extension.FORM,
            StringUtils.Extension.DOC,
            StringUtils.Extension.SHEET,
            StringUtils.Extension.PRESENTATION
        )
    }

    private fun isShareVisible(access: ApiContract.Access, section: ApiContract.Section): Boolean =
        section == ApiContract.Section.User || access in listOf(
            ApiContract.Access.Comment,
            ApiContract.Access.ReadWrite
        )
}