package app.editors.manager.managers.utils

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.google.android.material.imageview.ShapeableImageView
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

    fun getItemThumbnail(item: Item, isGrid: Boolean): Int {
        return if (item is CloudFile) {
            getFileThumbnail(StringUtils.getExtensionFromPath(item.title), isGrid)
        } else -1
    }

    fun getFileThumbnail(ext: String, isGrid: Boolean): Int {
        return when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC -> if (!isGrid) R.drawable.ic_type_document_row else R.drawable.ic_type_document_column
            StringUtils.Extension.SHEET -> if (!isGrid) R.drawable.ic_type_spreadsheet_row else R.drawable.ic_type_spreadsheet_column
            StringUtils.Extension.PRESENTATION -> if (!isGrid) R.drawable.ic_type_presentation_row else R.drawable.ic_type_presentation_column
            StringUtils.Extension.IMAGE,
            StringUtils.Extension.IMAGE_GIF -> if (!isGrid) R.drawable.ic_type_picture_row else R.drawable.ic_type_picture_column

            StringUtils.Extension.HTML,
            StringUtils.Extension.EBOOK,
            StringUtils.Extension.PDF -> if (!isGrid) R.drawable.ic_type_pdf_row else R.drawable.ic_type_pdf_column

            StringUtils.Extension.VIDEO_SUPPORT,
            StringUtils.Extension.VIDEO -> if (!isGrid) R.drawable.ic_type_video_row else R.drawable.ic_type_video_column

            StringUtils.Extension.ARCH -> if (!isGrid) R.drawable.ic_type_archive_row else R.drawable.ic_type_archive_column
            StringUtils.Extension.FORM -> if (!isGrid) R.drawable.ic_type_docxf_row else R.drawable.ic_type_docxf_column
            else -> if (!isGrid) R.drawable.ic_type_other_row else R.drawable.ic_type_other_column
        }
    }

    fun CardView.setRoomIcon(
        room: CloudFolder,
        image: ImageView,
        text: TextView,
        badge: ShapeableImageView,
        isGrid: Boolean
    ) {
        val logo = room.logo?.large

        fun setInitials() {
            val initials = RoomUtils.getRoomInitials(room.title)
            if (!initials.isNullOrEmpty()) {
                image.isVisible = false
                text.isVisible = true
                text.text = initials
            }
            setCardBackgroundColor(
                room.logo?.color?.let { color -> Color.parseColor("#$color") }
                    ?: context.getColor(lib.toolkit.base.R.color.colorPrimary)
            )
        }

        if (!logo.isNullOrEmpty()) {
            text.isVisible = false
            image.isVisible = true
            image.setRoomLogo(logo, isGrid, ::setInitials)
            setCardBackgroundColor(context.getColor(lib.toolkit.base.R.color.colorTransparent))
        } else {
            setInitials()
        }

        if (room.providerItem && room.providerKey.isNotEmpty()) {
            badge.setImageResource(StorageUtils.getStorageIcon(room.providerKey))
            badge.isVisible = true
        } else if (room.roomType == ApiContract.RoomType.PUBLIC_ROOM) {
            badge.setImageResource(
                if (isGrid) {
                    R.drawable.ic_public_room_big
                } else {
                    R.drawable.ic_public_room_badge
                }
            )
            badge.isVisible = true
        } else {
            badge.isVisible = false
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

    fun getAccessList(
        extension: StringUtils.Extension,
        removable: Boolean = false,
        isDocSpace: Boolean = false,
    ): List<Int> {
        return buildList {
            if (isDocSpace) {
                add(ApiContract.ShareCode.EDITOR)
            } else {
                add(ApiContract.ShareCode.READ_WRITE)
            }
            when (extension) {
                StringUtils.Extension.DOC, StringUtils.Extension.DOCXF -> {
                    add(ApiContract.ShareCode.REVIEW)
                    add(ApiContract.ShareCode.COMMENT)
                }

                StringUtils.Extension.PRESENTATION -> {
                    add(ApiContract.ShareCode.COMMENT)
                }

                StringUtils.Extension.SHEET -> {
                    add(ApiContract.ShareCode.CUSTOM_FILTER)
                    add(ApiContract.ShareCode.COMMENT)
                }

                StringUtils.Extension.PDF, StringUtils.Extension.OFORM -> {
                    add(ApiContract.ShareCode.FILL_FORMS)
                }
                else -> Unit
            }
            add(ApiContract.ShareCode.READ)
            add(ApiContract.ShareCode.RESTRICT)
            if (removable) {
                add(ApiContract.ShareCode.NONE)
            }
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