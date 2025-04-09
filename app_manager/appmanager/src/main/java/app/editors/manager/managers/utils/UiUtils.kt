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
import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.UserType
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils.setRoomLogo
import app.editors.manager.mvp.models.ui.AccessUI
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
            StringUtils.Extension.IMAGE_GIF,
                -> if (!isGrid) R.drawable.ic_type_picture_row else R.drawable.ic_type_picture_column

            StringUtils.Extension.HTML,
            StringUtils.Extension.EBOOK,
            StringUtils.Extension.PDF,
                -> if (!isGrid) R.drawable.ic_type_pdf_row else R.drawable.ic_type_pdf_column

            StringUtils.Extension.VIDEO_SUPPORT,
            StringUtils.Extension.VIDEO,
                -> if (!isGrid) R.drawable.ic_type_video_row else R.drawable.ic_type_video_column

            StringUtils.Extension.ARCH -> if (!isGrid) R.drawable.ic_type_archive_row else R.drawable.ic_type_archive_column
            StringUtils.Extension.FORM -> if (!isGrid) R.drawable.ic_type_docxf_row else R.drawable.ic_type_docxf_column
            else -> if (!isGrid) R.drawable.ic_type_other_row else R.drawable.ic_type_other_column
        }
    }

    fun CardView.setRoomIcon(
        room: CloudFolder,
        image: ImageView,
        text: TextView,
        publicBadge: ShapeableImageView,
        externalBadge: ImageView,
        isGrid: Boolean,
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

        publicBadge.isVisible = false
        externalBadge.isVisible = false

        if (room.external) {
            externalBadge.isVisible = true
        } else if (room.providerItem && room.providerKey.isNotEmpty()) {
            publicBadge.setImageResource(StorageUtils.getStorageIcon(room.providerKey))
            publicBadge.isVisible = true
        } else if (room.roomType == ApiContract.RoomType.PUBLIC_ROOM) {
            publicBadge.setImageResource(
                if (isGrid) {
                    R.drawable.ic_public_room_big
                } else {
                    R.drawable.ic_public_room_badge
                }
            )
            publicBadge.isVisible = true
        }
    }

    fun getAccessList(
        extension: StringUtils.Extension,
        removable: Boolean = false,
        isDocSpace: Boolean = false,
    ): List<Access> {
        return buildList {
            if (isDocSpace) {
                add(Access.Editor)
            } else {
                add(Access.ReadWrite)
            }
            when (extension) {
                StringUtils.Extension.DOC, StringUtils.Extension.DOCXF -> {
                    add(Access.Review)
                    add(Access.Comment)
                }

                StringUtils.Extension.PRESENTATION -> {
                    add(Access.Comment)
                }

                StringUtils.Extension.SHEET -> {
                    add(Access.CustomFilter)
                    add(Access.Comment)
                }

                StringUtils.Extension.PDF, StringUtils.Extension.OFORM -> {
                    add(Access.FormFiller)
                }
                else -> Unit
            }
            add(Access.Read)
            if (!isDocSpace) {
                add(Access.Restrict)
            }
            if (removable) {
                add(Access.None)
            }
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

fun Access.toUi(): AccessUI {
    val (icon, title) = when (this) {
        Access.Comment -> arrayOf(
            R.drawable.ic_access_comment,
            R.string.share_access_room_commentator
        )

        Access.CustomFilter -> arrayOf(
            R.drawable.ic_access_custom_filter,
            R.string.share_popup_access_custom_filter
        )

        Access.ReadWrite, Access.Editor -> arrayOf(
            R.drawable.ic_access_full,
            R.string.share_access_room_editor
        )

        Access.FormFiller -> arrayOf(
            R.drawable.ic_access_fill_form,
            R.string.share_popup_access_fill_forms
        )

        Access.Read -> arrayOf(
            R.drawable.ic_access_read,
            R.string.share_popup_access_read_only
        )

        Access.Review -> arrayOf(
            R.drawable.ic_access_review,
            R.string.share_access_room_reviewer
        )

        Access.RoomManager -> arrayOf(
            R.drawable.ic_room_manager,
            R.string.share_access_room_manager
        )

        Access.ContentCreator -> arrayOf(
            R.drawable.ic_room_power_user,
            R.string.share_access_room_power_user
        )

        Access.Restrict -> arrayOf(
            R.drawable.ic_access_deny,
            R.string.share_popup_access_deny_access
        )

        Access.None -> arrayOf(
            R.drawable.ic_access_deny,
            R.string.share_popup_access_deny_remove
        )
    }
    return AccessUI(this, title, icon)
}

fun User.getTypeTitle(provider: PortalProvider?): Int {
    return when (type) {
        UserType.Owner -> {
            when(provider) {
                is PortalProvider.Cloud.Workspace -> R.string.share_user_type_room_workspace_owner
                is PortalProvider.Cloud.DocSpace -> R.string.share_user_type_room_docspace_owner
                else -> R.string.share_user_type_room_portal_owner
            }
        }
        UserType.Admin -> R.string.share_user_type_room_docspace_admin
        UserType.RoomAdmin -> R.string.share_user_type_room_admin
        UserType.User -> R.string.profile_type_user
        UserType.Guest -> R.string.profile_type_visitor
    }
}