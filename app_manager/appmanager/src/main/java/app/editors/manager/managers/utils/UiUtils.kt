package app.editors.manager.managers.utils

import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.webdav.WebDavService
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.BuildConfig
import app.editors.manager.R
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.StringUtils

object ManagerUiUtils {

    @JvmStatic
    fun setWebDavImage(providerName: String?, image: ImageView) {
        when (WebDavService.Providers.valueOf(providerName ?: "")) {
            WebDavService.Providers.NextCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_nextcloud
                )
            )
            WebDavService.Providers.OwnCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_owncloud
                )
            )
            WebDavService.Providers.Yandex -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_yandex
                )
            )
            WebDavService.Providers.KDrive -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_kdrive
                )
            )
            WebDavService.Providers.WebDav -> image.setImageDrawable(
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

    fun ImageView.setDropboxImage(account: CloudAccount) {
        if (account.avatarUrl?.isNotEmpty() == true) {
            Glide.with(this)
                .load(GlideUtils.getCorrectLoad(account.avatarUrl ?: "", account.token))
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

    fun getIcon(item: Item): Int {
        return if (item is CloudFolder) {
            getFolderIcon(item)
        } else {
            getFileIcon(StringUtils.getExtensionFromPath(item.title))
        }
    }

    fun getFileIcon(ext: String): Int {
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
                if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                    if (ext == ".${LocalContentTools.OFORM_EXTENSION}") R.drawable.ic_format_oform
                    else R.drawable.ic_format_docxf
                } else {
                    R.drawable.ic_type_file
                }
            }
            else -> R.drawable.ic_type_file
        }
    }

    fun ImageView.setFileIcon(ext: String) {
        setImageResource(getFileIcon(ext))
        alpha = 1.0f
    }

    fun ImageView.setFolderIcon(folder: CloudFolder, isRoot: Boolean) {
        setImageResource(getFolderIcon(folder, isRoot))
    }

    private fun getFolderIcon(folder: CloudFolder, isRoot: Boolean = false): Int {
        return when {
            folder.shared && folder.providerKey.isEmpty() -> R.drawable.ic_type_folder_shared
            isRoot && folder.providerItem -> StorageUtils.getStorageIcon(folder.providerKey)
            ApiContract.SectionType.isArchive(folder.rootFolderType)-> R.drawable.ic_type_archive
            folder.isRoom -> getRoomIcon(folder)
            else -> R.drawable.ic_type_folder
        }
    }

    private fun getRoomIcon(folder: CloudFolder): Int {
        return when (folder.roomType) {
            ApiContract.RoomType.FILLING_FORM_ROOM -> R.drawable.ic_room_fill_forms
            ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_room_custom
            ApiContract.RoomType.READ_ONLY_ROOM -> R.drawable.ic_room_view_only
            ApiContract.RoomType.REVIEW_ROOM -> R.drawable.ic_room_review
            ApiContract.RoomType.EDITING_ROOM -> R.drawable.ic_room_collaboration
            else -> throw IllegalArgumentException("No this type of room")
        }
    }

    fun setAccessIcon(icon: ImageView?, accessCode: Int) {
        if (icon == null) return
        when (accessCode) {
            ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> {
                icon.setImageResource(R.drawable.ic_access_deny)
                return
            }
            ApiContract.ShareCode.REVIEW -> icon.setImageResource(R.drawable.ic_access_review)
            ApiContract.ShareCode.READ -> icon.setImageResource(R.drawable.ic_access_read)
            ApiContract.ShareCode.ROOM_ADMIN -> icon.setImageResource(R.drawable.ic_drawer_menu_my_docs)
            ApiContract.ShareCode.READ_WRITE -> icon.setImageResource(R.drawable.ic_access_full)
            ApiContract.ShareCode.EDITOR -> icon.setImageResource(R.drawable.ic_access_full)
            ApiContract.ShareCode.COMMENT -> icon.setImageResource(R.drawable.ic_access_comment)
            ApiContract.ShareCode.FILL_FORMS -> icon.setImageResource(R.drawable.ic_access_fill_form)
        }
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