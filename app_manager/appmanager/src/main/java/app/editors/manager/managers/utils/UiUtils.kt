package app.editors.manager.managers.utils

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.mvp.models.explorer.CloudFolder
import com.bumptech.glide.Glide
import lib.toolkit.base.managers.tools.LocalContentTools
import lib.toolkit.base.managers.utils.StringUtils

object ManagerUiUtils {

    @JvmStatic
    fun setWebDavImage(providerName: String?, image: ImageView) {
        when (WebDavApi.Providers.valueOf(providerName ?: "")) {
            WebDavApi.Providers.NextCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_nextcloud
                )
            )
            WebDavApi.Providers.OwnCloud -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_owncloud
                )
            )
            WebDavApi.Providers.Yandex -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_yandex
                )
            )
            WebDavApi.Providers.KDrive -> image.setImageDrawable(
                ContextCompat.getDrawable(
                    image.context,
                    R.drawable.ic_storage_kdrive
                )
            )
            WebDavApi.Providers.WebDav -> image.setImageDrawable(
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

    fun ImageView.setFileIcon(ext: String) {
        @DrawableRes val resId = when (StringUtils.getExtension(ext)) {
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
        setImageResource(resId)
        alpha = 1.0f
    }

    fun ImageView.setFolderIcon(folder: CloudFolder, isRoot: Boolean) {
        @DrawableRes var resId = R.drawable.ic_type_folder
        if (folder.shared && folder.providerKey.isEmpty()) {
            resId = R.drawable.ic_type_folder_shared
        } else if (isRoot && folder.providerItem && folder.providerKey.isNotEmpty()) {
            when (folder.providerKey) {
                ApiContract.Storage.BOXNET -> resId = R.drawable.ic_storage_box
                ApiContract.Storage.NEXTCLOUD -> resId = R.drawable.ic_storage_nextcloud
                ApiContract.Storage.DROPBOX -> resId = R.drawable.ic_storage_dropbox
                ApiContract.Storage.SHAREPOINT -> resId = R.drawable.ic_storage_sharepoint
                ApiContract.Storage.GOOGLEDRIVE -> resId = R.drawable.ic_storage_google
                ApiContract.Storage.KDRIVE -> resId = R.drawable.ic_storage_kdrive
                ApiContract.Storage.ONEDRIVE, ApiContract.Storage.SKYDRIVE -> resId =
                    R.drawable.ic_storage_onedrive
                ApiContract.Storage.YANDEX -> resId = R.drawable.ic_storage_yandex
                ApiContract.Storage.WEBDAV -> {
                    resId = R.drawable.ic_storage_webdav
                    this.setImageResource(resId)
                    return
                }
            }
            this.setImageResource(resId)
            this.alpha = 1.0f
            return
        }
        if (folder.isRoom) resId = getRoomIcon(folder)
        this.setImageResource(resId)
    }

    fun getFolderIcon(folder: CloudFolder): Int {
        @DrawableRes var resId = R.drawable.ic_type_folder
        if (folder.shared && folder.providerKey.isEmpty()) {
            resId = R.drawable.ic_type_folder_shared
        } else if ( folder.providerItem && folder.providerKey.isNotEmpty()) {
            when (folder.providerKey) {
                ApiContract.Storage.BOXNET -> resId = R.drawable.ic_storage_box
                ApiContract.Storage.NEXTCLOUD -> resId = R.drawable.ic_storage_nextcloud
                ApiContract.Storage.DROPBOX -> resId = R.drawable.ic_storage_dropbox
                ApiContract.Storage.SHAREPOINT -> resId = R.drawable.ic_storage_sharepoint
                ApiContract.Storage.GOOGLEDRIVE -> resId = R.drawable.ic_storage_google
                ApiContract.Storage.KDRIVE -> resId = R.drawable.ic_storage_kdrive
                ApiContract.Storage.ONEDRIVE, ApiContract.Storage.SKYDRIVE -> resId =
                    R.drawable.ic_storage_onedrive
                ApiContract.Storage.YANDEX -> resId = R.drawable.ic_storage_yandex
                ApiContract.Storage.WEBDAV -> {
                    resId = R.drawable.ic_storage_webdav
                    return resId
                }
            }

            return resId
        }
        if (folder.isRoom) resId = getRoomIcon(folder)
        return resId
    }

    fun getRoomIcon(folder: CloudFolder): Int {
        return when (folder.roomType) {
            ApiContract.RoomType.FILLING_FORM_ROOM -> R.drawable.ic_room_fill_forms
            ApiContract.RoomType.CUSTOM_ROOM -> R.drawable.ic_room_custom
            ApiContract.RoomType.READ_ONLY_ROOM -> R.drawable.ic_room_view_only
            ApiContract.RoomType.REVIEW_ROOM -> R.drawable.ic_room_review
            ApiContract.RoomType.EDITING_ROOM -> R.drawable.ic_room_collaboration
            else -> throw IllegalArgumentException("No this type of room")
        }
    }

    fun setAccessIcon(imageView: ImageView, accessCode: Int, isRoom: Boolean = false) {
        when (accessCode) {
            ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> {
                imageView.setImageResource(R.drawable.ic_access_deny)
                return
            }
            ApiContract.ShareCode.REVIEW -> imageView.setImageResource(R.drawable.ic_access_review)
            ApiContract.ShareCode.READ -> imageView.setImageResource(R.drawable.ic_access_read)
            ApiContract.ShareCode.READ_WRITE -> {
                if (isRoom) {
                    imageView.setImageResource(R.drawable.ic_drawer_menu_my_docs)
                } else {
                    imageView.setImageResource(R.drawable.ic_access_full)
                }
            }
            ApiContract.ShareCode.COMMENT -> imageView.setImageResource(R.drawable.ic_access_comment)
            ApiContract.ShareCode.FILL_FORMS -> imageView.setImageResource(R.drawable.ic_access_fill_form)
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
