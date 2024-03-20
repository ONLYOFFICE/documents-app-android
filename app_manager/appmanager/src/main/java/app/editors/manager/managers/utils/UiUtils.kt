package app.editors.manager.managers.utils

import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils.setRoomLogo
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.StringUtils

object ManagerUiUtils {

    @JvmStatic
    fun setWebDavImage(webDavProvider: PortalProvider.Webdav?, image: ImageView) {
        when (webDavProvider?.provider) {

            is WebdavProvider.NextCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_nextcloud
                )
            )

            WebdavProvider.OwnCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_owncloud
                )
            )

            WebdavProvider.Yandex -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_yandex
                )
            )

            WebdavProvider.KDrive -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_kdrive
                )
            )

            else -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_webdav
                )
            )
        }
    }

    fun ImageView.setOneDriveImage() {
        this.setImageDrawable(
            ContextCompat.getDrawable(
                this.context,
                R.drawable.ic_storage_onedrive
            )
        )
    }

    fun ImageView.setDropboxImage(account: CloudAccount, token: String) {
        if (account.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(GlideUtils.getCorrectLoad(account.avatarUrl, token))
                .apply(GlideUtils.avatarOptions)
                .into(this)
        } else {
            this.setImageDrawable(
                ContextCompat.getDrawable(
                    this.context,
                    R.drawable.ic_storage_dropbox
                )
            )
        }
    }

    fun ImageView.setItemIcon(item: Item?, root: Boolean) {
        when (item) {
            is CloudFolder -> setFolderIcon(item, root)
            is CloudFile -> setFileIcon(StringUtils.getExtensionFromPath(item.title))
        }
    }

    fun getIcon(item: Item): Int {
        return if (item is CloudFolder) {
            getFolderIcon(item)
        } else {
            getFileIcon(StringUtils.getExtensionFromPath(item.title))
        }
    }

    private fun getFileIcon(ext: String): Int {
        return when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC -> R.drawable.ic_type_text_document
            StringUtils.Extension.SHEET -> R.drawable.ic_type_spreadsheet
            StringUtils.Extension.PRESENTATION -> R.drawable.ic_type_presentation
            StringUtils.Extension.IMAGE,
            StringUtils.Extension.IMAGE_GIF -> R.drawable.ic_type_image
            StringUtils.Extension.HTML,
            StringUtils.Extension.EBOOK,
            StringUtils.Extension.PDF -> R.drawable.ic_type_pdf
            StringUtils.Extension.VIDEO_SUPPORT,
            StringUtils.Extension.VIDEO -> R.drawable.ic_type_video
            StringUtils.Extension.ARCH -> R.drawable.ic_type_archive
            StringUtils.Extension.FORM -> {
                if (ext == ".${LocalContentTools.OFORM_EXTENSION}") R.drawable.ic_format_oform
                else R.drawable.ic_format_docxf
            }

            else -> R.drawable.ic_type_file
        }
    }

    fun ImageView.setFileIcon(ext: String) {
        setImageResource(getFileIcon(ext))
        imageAlpha = 255
    }

    fun ImageView.setFolderIcon(folder: CloudFolder, isRoot: Boolean) {
        val icon = getFolderIcon(folder, isRoot)
        val logo = folder.logo?.large
        if (!logo.isNullOrEmpty()) {
            setRoomLogo(logo, icon)
        } else {
            setImageResource(icon)
        }
    }

    private fun getFolderIcon(folder: CloudFolder, isRoot: Boolean = false): Int {
        return when {
            folder.isRoom -> getRoomIcon(folder)
            folder.shared && folder.providerKey.isEmpty() -> R.drawable.ic_type_folder_shared
            isRoot && folder.providerItem -> StorageUtils.getStorageIcon(folder.providerKey)
            ApiContract.SectionType.isArchive(folder.rootFolderType) -> R.drawable.ic_type_archive
            else -> R.drawable.ic_type_folder
        }
    }

    private fun getRoomIcon(folder: CloudFolder): Int {
        return when (folder.roomType) {
            ApiContract.RoomType.COLLABORATION_ROOM -> R.drawable.ic_collaboration_room
            ApiContract.RoomType.PUBLIC_ROOM -> R.drawable.ic_public_room
            ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_custom_room
            else -> R.drawable.ic_type_folder
        }
    }

    fun getAccessIcon(accessCode: Int): Int {
        return when (accessCode) {
            ApiContract.ShareCode.NONE,
            ApiContract.ShareCode.RESTRICT -> R.drawable.ic_access_deny
            ApiContract.ShareCode.REVIEW -> R.drawable.ic_access_review
            ApiContract.ShareCode.READ -> R.drawable.ic_access_read
            ApiContract.ShareCode.ROOM_ADMIN -> R.drawable.ic_room_admin
            ApiContract.ShareCode.POWER_USER -> R.drawable.ic_room_power_user
            ApiContract.ShareCode.READ_WRITE -> R.drawable.ic_access_full
            ApiContract.ShareCode.EDITOR -> R.drawable.ic_access_full
            ApiContract.ShareCode.COMMENT -> R.drawable.ic_access_comment
            ApiContract.ShareCode.FILL_FORMS -> R.drawable.ic_access_fill_form
            ApiContract.ShareCode.CUSTOM_FILTER -> R.drawable.ic_access_custom_filter
            else -> R.drawable.ic_access_deny
        }
    }

    fun setAccessIcon(icon: ImageView?, accessCode: Int) {
        icon?.setImageResource(getAccessIcon(accessCode))
    }

    fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.leftMargin = left
        layoutParams.topMargin = top
        layoutParams.rightMargin = right
        layoutParams.bottomMargin = bottom
        this.layoutParams = layoutParams
    }
}

fun Modifier.fillMaxWidth(isTablet: Boolean): Modifier {
    return if (isTablet) fillMaxWidth(0.3f) else fillMaxWidth()
}