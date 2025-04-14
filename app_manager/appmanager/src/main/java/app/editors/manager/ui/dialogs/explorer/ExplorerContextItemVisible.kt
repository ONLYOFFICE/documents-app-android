package app.editors.manager.ui.dialogs.explorer

import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
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
            ExplorerContextItem.EditIndex -> editIndex
            is ExplorerContextItem.Edit -> edit
            is ExplorerContextItem.Fill -> fill
            is ExplorerContextItem.View -> view
            is ExplorerContextItem.ExternalLink -> externalLink
            is ExplorerContextItem.Restore -> restore
            is ExplorerContextItem.Header -> true
            is ExplorerContextItem.Pin -> pin
            is ExplorerContextItem.Delete -> delete
            is ExplorerContextItem.Notifications -> notifications
            is ExplorerContextItem.Favorites -> favorites(contextItem.enabled)
            is ExplorerContextItem.Lock -> lock
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
        get() = if (provider == PortalProvider.Cloud.DocSpace)
            item.security?.download == true
        else true

    private val ExplorerContextState.reconnect: Boolean
        get() = item is CloudFolder && item.providerItem

    private val ExplorerContextState.fill: Boolean
        get() {
            val file = item as? CloudFile ?: return false
            return when (section) {
                is ApiContract.Section.Device -> file.isPdfForm
                else -> when (provider) {
                    PortalProvider.Cloud.DocSpace -> file.security?.fillForms == true
                    else -> item.isPdfForm
                }
            }
        }

    private val ExplorerContextState.view: Boolean
        get() = item is CloudFile && !item.isPdfForm

    private val ExplorerContextState.edit: Boolean
        get() {
            return when (section) {
                ApiContract.Section.Device -> isExtensionEditable(item)
                else -> {
                    if (provider == PortalProvider.Cloud.DocSpace) {
                        with(item.security ?: return false) {
                            when (item) {
                                is CloudFile -> {
                                    edit && (isExtensionEditable(item) || item.isPdfForm)
                                }
                                is CloudFolder -> editRoom && item.isRoom
                                else -> false
                            }
                        }
                    } else {
                        isExtensionEditable(item) && access == Access.ReadWrite
                    }
                }
            }
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
            access == Access.ReadWrite || section in listOf(
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
                Access.ReadWrite,
                Access.None
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

    private val ExplorerContextState.lock: Boolean
        get() = (item is CloudFile) && item.security?.lock == true

    private fun ExplorerContextState.favorites(enabled: Boolean): Boolean =
        enabled && !isFolder && !listOf(
            ApiContract.Section.Trash,
            ApiContract.Section.Webdav
        ).contains(section)

    private fun isExtensionEditable(item: Item): Boolean {
        return if (item is CloudFile) {
            isExtensionEditable(item.fileExst) || item.isPdfForm
        } else {
            false
        }
    }

    private fun isExtensionEditable(ext: String): Boolean {
        return StringUtils.getExtension(ext) in listOf(
            StringUtils.Extension.FORM,
            StringUtils.Extension.DOC,
            StringUtils.Extension.SHEET,
            StringUtils.Extension.PRESENTATION
        )
    }

    private fun isShareVisible(access: Access, section: ApiContract.Section): Boolean =
        section == ApiContract.Section.User || access in listOf(
            Access.Comment,
            Access.ReadWrite
        )

}