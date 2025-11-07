package app.editors.manager.managers.tools

import app.documents.core.model.cloud.Access
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.manager.models.explorer.AvailableShareRights
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.managers.utils.toUi
import app.editors.manager.mvp.models.ui.AccessUI
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.tools.FileGroup
import java.io.Serializable

data class ShareData(
    val itemId: String = "",
    val fileExt: String? = null,
    val roomType: Int? = null,
    val availableShareRights: AvailableShareRights? = null
) : Serializable {

    val isFolder: Boolean
        get() = fileExt == null

    fun getAccessList(target: AccessTarget, withRemove: Boolean = false): List<AccessUI> {
        return ShareAccessManager.getAccessList(shareData = this, target = target)
            .let { accessList ->
                if (withRemove) accessList + Access.None.toUi() else accessList
            }
    }

    companion object {

        fun from(item: Item): ShareData {
            val shareData = ShareData(
                itemId = item.id,
                availableShareRights = item.availableShareRights
            )

            return when {
                item is CloudFile -> shareData.copy(fileExt = item.fileExst)
                item is CloudFolder && item.isRoom -> shareData.copy(roomType = item.roomType)
                item is CloudFolder -> shareData
                else -> error("cannot get share data from this item")
            }
        }
    }
}

object ShareAccessManager {

    fun getAccessList(
        shareData: ShareData,
        target: AccessTarget
    ): List<AccessUI> {
        val availableShareRights = shareData.availableShareRights
        if (availableShareRights != null) {
            val accessList = availableShareRights.toBundle().fromTarget(target)
            if (accessList.isNotEmpty()) {
                return accessList.map { it.toUi(shareData.roomType == null) }
            }
        }

        return if (shareData.fileExt != null) {
            getFileAccessList(shareData.fileExt, target)
        } else if (shareData.roomType != null) {
            getRoomAccessList(ApiContract.RoomType.toObj(shareData.roomType), target)
        } else {
            getFolderAccessList(target)
        }
    }

//    fun getItemAccessBundle(item: Item): AccessBundle {
//        var bundle = item.availableShareRights?.toBundle() ?: return AccessBundle()
//
//        if (bundle.user.isEmpty()) {
//            bundle = bundle.copy(user = getItemAccessList(item, AccessTarget.User))
//        }
//
//        if (bundle.group.isEmpty()) {
//            bundle = bundle.copy(group = getItemAccessList(item, AccessTarget.Group))
//        }
//
//        if (bundle.externalLink.isEmpty()) {
//            bundle = bundle.copy(externalLink = getItemAccessList(item, AccessTarget.ExternalLink))
//        }
//
//        if (bundle.primaryLink.isEmpty()) {
//            bundle = bundle.copy(primaryLink = getItemAccessList(item, AccessTarget.PrimaryLink))
//        }
//
//        return bundle
//    }

//    fun getItemAccessList(
//        fileExtensions: FileExtensions?, // null for folder
//        target: AccessTarget
//    ) {
//        if (fileExtensions != null) {
//            getFileAccessList(fileExtensions, target)
//        } else {
//            getFolderAccessList(target)
//        }
//    }

    fun getRoomAccessList(
        roomType: ApiContract.RoomTypeObj,
        target: AccessTarget,
    ): List<AccessUI> {
        if (target.isLink) return getLinkAccessList()

        return buildList {
            add(Access.RoomManager)
            add(Access.ContentCreator)
            when (roomType) {
                ApiContract.RoomTypeObj.VDR -> {
                    add(Access.Editor)
                    add(Access.FormFiller)
                    add(Access.Read)
                }

                ApiContract.RoomTypeObj.FillingForms -> {
                    add(Access.FormFiller)
                }

                ApiContract.RoomTypeObj.Collaboration -> {
                    add(Access.Editor)
                    add(Access.Read)
                }

                ApiContract.RoomTypeObj.Custom -> {
                    add(Access.Editor)
                    add(Access.Review)
                    add(Access.Comment)
                    add(Access.Read)
                }

                ApiContract.RoomTypeObj.Public -> Unit
            }
        }.map { it.toUi() }
    }

    fun getLinkAccessList(): List<AccessUI> {
        return listOfNotNull(
            Access.Editor,
            Access.Review,
            Access.Comment,
            Access.Read,
        ).map { it.toUi() }
    }

    fun getFolderAccessList(target: AccessTarget): List<AccessUI> {
        if (target.isLink) {
            return getLinkAccessList()
        }

        return listOfNotNull(
            Access.ReadWrite,
            Access.Editor,
            Access.Review,
            Access.Comment,
            Access.Read,
            Access.Restrict,
            Access.None
        ).map { it.toUi(true) }
    }

    fun getFileAccessList(extension: String, target: AccessTarget): List<AccessUI> {
        return getFileAccessList(FileExtensions.fromExtension(extension), target)
    }

    fun getFileAccessList(extension: FileExtensions, target: AccessTarget): List<AccessUI> {
        return buildList {
            if (!target.isLink) {
                add(Access.ReadWrite)
            }
            add(Access.Editor)
            when (extension.group) {
                FileGroup.DOCUMENT -> {
                    add(Access.Review)
                    add(Access.Comment)
                    add(Access.Read)
                }

                FileGroup.SHEET -> {
                    add(Access.CustomFilter)
                    add(Access.Comment)
                    add(Access.Read)
                }

                FileGroup.PRESENTATION -> {
                    add(Access.Comment)
                    add(Access.Read)
                }

                FileGroup.PDF -> {
                    add(Access.FormFiller)
                }

                else -> Unit
            }
            if (!target.isLink) {
                add(Access.Restrict)
                add(Access.None)
            }
        }.map { it.toUi(true) }
    }
}