package app.editors.manager.ui.dialogs.explorer

import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import lib.toolkit.base.managers.utils.StringUtils


interface ExplorerContextItemVisible {

    fun ExplorerContextState.visible(contextItem: ExplorerContextItem): Boolean {
        return when (contextItem) {
            ExplorerContextItem.AddUsers -> addUsers
            ExplorerContextItem.Archive -> archive
            ExplorerContextItem.Copy -> copy
            ExplorerContextItem.Duplicate -> duplicate
            ExplorerContextItem.Download -> download
            ExplorerContextItem.Location -> location
            ExplorerContextItem.Move -> move
            ExplorerContextItem.Rename -> rename
            ExplorerContextItem.RoomInfo -> roomInfo
            ExplorerContextItem.Send -> send
            ExplorerContextItem.Share -> share
            ExplorerContextItem.ShareDelete -> shareDelete
            ExplorerContextItem.Upload -> upload
            ExplorerContextItem.CreateRoom -> createRoom
            ExplorerContextItem.Reconnect -> reconnect
            is ExplorerContextItem.Edit -> edit
            is ExplorerContextItem.ExternalLink -> externalLink
            is ExplorerContextItem.Restore -> restore
            is ExplorerContextItem.Header -> true
            is ExplorerContextItem.Pin -> pin
            is ExplorerContextItem.Delete -> delete
            is ExplorerContextItem.Notifications -> notifications
            is ExplorerContextItem.Favorites -> favorites(contextItem.enabled)
        }
    }

    private val ExplorerContextState.addUsers: Boolean
        get() = section != ApiContract.Section.Room.Archive && item.security?.editAccess == true

    private val ExplorerContextState.archive: Boolean
        get() = item.security?.moveTo == true && item.security?.editRoom == true && section !is ApiContract.Section.Room.Archive

    private val ExplorerContextState.copy: Boolean
        get() = if (section.isRoom) item.security?.copy == true else section != ApiContract.Section.Trash

    private val ExplorerContextState.duplicate: Boolean
        get() = item.security?.duplicate == true

    private val ExplorerContextState.notifications: Boolean
        get() = section.isRoom && item is CloudFolder && item.isRoom

    private val ExplorerContextState.download: Boolean
        get() = !section.isLocal

    private val ExplorerContextState.reconnect: Boolean
        get() = item is CloudFolder && item.providerItem

    private val ExplorerContextState.edit: Boolean
        get() {
            val item = this.item
            return if (item is CloudFile && (isExtensionEditable(item.fileExst) || item.isPdfForm)) {
                when (section) {
                    ApiContract.Section.Recent,
                    ApiContract.Section.User,
                    ApiContract.Section.Device -> true

                    ApiContract.Section.Trash,
                    ApiContract.Section.Room.Archive -> false
                    else -> access != ApiContract.Access.Read || item.security?.editAccess == true
                }
            } else section.isRoom && isRoot && item.security?.editRoom == true
        }

    private val ExplorerContextState.move: Boolean
        get() = if (!section.isRoom) {
            section in listOf(
                ApiContract.Section.User,
                ApiContract.Section.Device
            ) || section is ApiContract.Section.Storage
        } else item.security?.move == true

    private val ExplorerContextState.externalLink: Boolean
        get() = when (section) {
            is ApiContract.Section.Room -> true
            else -> isShareVisible(access, section) && !isFolder
        }

    private val ExplorerContextState.rename: Boolean
        get() = if (!section.isRoom) {
            access == ApiContract.Access.ReadWrite || section in listOf(
                ApiContract.Section.User,
                ApiContract.Section.Device
            )
        } else item.security?.rename == true

    private val ExplorerContextState.restore: Boolean
        get() = when (section) {
            ApiContract.Section.Trash -> true
            ApiContract.Section.Room.Archive -> item.security?.move == true
            else -> false
        }

    private val ExplorerContextState.roomInfo: Boolean
        get() = section.isRoom

    private val ExplorerContextState.send: Boolean
        get() = section != ApiContract.Section.Trash && !isFolder

    private val ExplorerContextState.share: Boolean
        get() = if (provider is PortalProvider.Cloud.DocSpace) {
            section == ApiContract.Section.User && item is CloudFile
        } else if (item is CloudFile) {
            !item.isDenySharing && access in arrayOf(
                ApiContract.Access.ReadWrite,
                ApiContract.Access.None
            )
        } else {
            isShareVisible(access, section)
        }

    private val ExplorerContextState.shareDelete: Boolean
        get() = section == ApiContract.Section.Share

    private val ExplorerContextState.upload: Boolean
        get() = section == ApiContract.Section.Device && !isFolder

    private val ExplorerContextState.pin: Boolean
        get() = item.security?.pin == true

    private val ExplorerContextState.delete: Boolean
        get() = when (section) {
            ApiContract.Section.Share,
            ApiContract.Section.Favorites,
            ApiContract.Section.Projects -> false

            is ApiContract.Section.Room -> isRoot || item.security?.delete == true
            else -> true
        }

    private val ExplorerContextState.createRoom: Boolean
        get() {
            if (section is ApiContract.Section.Room.Archive) return false
            return item.security?.create == true || item is CloudFile
        }

    private val ExplorerContextState.location: Boolean
        get() = isSearching

    private fun ExplorerContextState.favorites(enabled: Boolean): Boolean =
        enabled && !isFolder && !listOf(
            ApiContract.Section.Trash,
            ApiContract.Section.Webdav
        ).contains(section)

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