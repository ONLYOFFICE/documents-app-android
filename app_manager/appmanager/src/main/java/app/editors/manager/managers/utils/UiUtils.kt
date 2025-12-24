package app.editors.manager.managers.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
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
import app.documents.core.network.share.models.ShareType
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils.setRoomLogo
import app.editors.manager.mvp.models.ui.AccessUI
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import lib.toolkit.base.managers.tools.FileExtensionUtils
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.tools.FileGroup
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
        return when (FileExtensionUtils.getDocumentType(ext)) {
            FileGroup.DOCUMENT -> if (FileExtensions.fromExtension(ext) == FileExtensions.TXT) {
                if (!isGrid) {
                    R.drawable.ic_type_txt_row
                } else {
                    R.drawable.ic_type_txt_column
                }
            } else {
                if (!isGrid) {
                    R.drawable.ic_type_document_row
                } else {
                    R.drawable.ic_type_document_column
                }
            }
            FileGroup.SHEET -> if (!isGrid) R.drawable.ic_type_spreadsheet_row else R.drawable.ic_type_spreadsheet_column
            FileGroup.PRESENTATION -> if (!isGrid) R.drawable.ic_type_presentation_row else R.drawable.ic_type_presentation_column
            FileGroup.IMAGE, FileGroup.IMAGE_GIF -> if (!isGrid) R.drawable.ic_type_picture_row else R.drawable.ic_type_picture_column
            FileGroup.HTML, FileGroup.PDF -> if (!isGrid) R.drawable.ic_type_pdf_row else R.drawable.ic_type_pdf_column
            FileGroup.VIDEO -> if (!isGrid) R.drawable.ic_type_video_row else R.drawable.ic_type_video_column
            FileGroup.ARCHIVE -> if (!isGrid) R.drawable.ic_type_archive_row else R.drawable.ic_type_archive_column
//            StringUtils.Extension.FORM -> if (!isGrid) R.drawable.ic_type_docxf_row else R.drawable.ic_type_docxf_column
            else -> if (!isGrid) R.drawable.ic_type_other_row else R.drawable.ic_type_other_column
        }
    }

    fun getFileBadge(ext: String): Int {
        return when (StringUtils.getExtension(ext)) {
            StringUtils.Extension.DOC -> R.drawable.ic_type_document_badge
            StringUtils.Extension.SHEET -> R.drawable.ic_type_spreadsheet_badge
            StringUtils.Extension.PRESENTATION -> R.drawable.ic_type_presentation_badge

            StringUtils.Extension.HTML,
            StringUtils.Extension.EBOOK,
            StringUtils.Extension.PDF -> R.drawable.ic_type_pdf_badge

            else -> 0
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
        val backgroundColor = room.logo?.color?.let { color -> "#$color".toColorInt() }
            ?: context.getColor(lib.toolkit.base.R.color.colorPrimary)

        setIconCommon(
            item = room,
            image = image,
            text = text,
            isGrid = isGrid,
            isTemplate = false,
            backgroundColor = backgroundColor
        ) { setCardBackgroundColor(backgroundColor) }

        publicBadge.isVisible = false
        externalBadge.isVisible = false

        if (room.external) {
            externalBadge.isVisible = true
        } else if (room.providerItem && room.providerKey.isNotEmpty()) {
            publicBadge.setImageResource(StorageUtils.getStorageIcon(room.providerKey))
            publicBadge.isVisible = true
        } else if (ApiContract.RoomType.hasExternalLink(room.roomType)) {
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

    fun CardView.setTemplateIcon(
        template: CloudFolder,
        image: ImageView,
        text: TextView,
        isGrid: Boolean,
    ) {
        val logoColor = template.logo?.color?.let { color -> "#$color".toColorInt() }
            ?: context.getColor(lib.toolkit.base.R.color.colorTextTertiary)
        val backgroundColor = context.getColor(lib.toolkit.base.R.color.colorTransparent)

        setIconCommon(
            item = template,
            image = image,
            text = text,
            isGrid = isGrid,
            isTemplate = true,
            backgroundColor = backgroundColor
        ) { text.setTextColor(logoColor) }

        val frame = AppCompatResources.getDrawable(context, R.drawable.room_template_logo)?.mutate()
        frame?.setTint(logoColor)
        background = frame
    }

    private fun CardView.setIconCommon(
        item: CloudFolder,
        image: ImageView,
        text: TextView,
        isGrid: Boolean,
        isTemplate: Boolean,
        backgroundColor: Int,
        onSetInitials: (() -> Unit)? = null
    ) {
        val logo = item.logo?.large

        fun setInitials() {
            val initials = RoomUtils.getRoomInitials(item.title)
            if (!initials.isNullOrEmpty()) {
                image.isVisible = false
                text.isVisible = true
                text.text = initials
            }
            onSetInitials?.invoke()
        }

        if (!logo.isNullOrEmpty()) {
            text.isVisible = false
            image.isVisible = true
            image.setRoomLogo(logo, isGrid, isTemplate, ::setInitials)
            setCardBackgroundColor(backgroundColor)
        } else {
            setInitials()
        }
    }

    fun getItemAccessList(
        extension: FileExtensions?,
        forLink: Boolean = false,
        withRemove: Boolean = false
    ): List<Access> {
        return if (extension != null) {
            getFileAccessList(extension = extension, withRemove = withRemove, forLink = forLink)
        } else {
            getFolderAccessList(withRemove = withRemove, forLink = forLink)
        }
    }

    fun getFolderAccessList(
        withRemove: Boolean,
        forLink: Boolean = false,
    ): List<Access> {
        return listOfNotNull(
            Access.ReadWrite.takeIf { !forLink },
            Access.Editor,
            Access.Review,
            Access.Comment,
            Access.Read,
            Access.Restrict.takeIf { !forLink },
            Access.None.takeIf { withRemove && !forLink }
        )
    }

    fun getFileAccessList(
        extension: FileExtensions,
        withRemove: Boolean = false,
        forLink: Boolean = false,
    ): List<Access> {
        return buildList {
            if (!forLink) add(Access.ReadWrite)
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
            if (!forLink) add(Access.Restrict)
            if (withRemove && !forLink) {
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

fun Access.toUi(useActionNames: Boolean = false): AccessUI {
    return when (this) {
        Access.Comment -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_comment,
            title = if (useActionNames)
                R.string.share_popup_access_comment else
                R.string.share_access_room_commentator
        )

        Access.CustomFilter -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_custom_filter,
            title = R.string.share_popup_access_custom_filter
        )

        Access.ReadWrite -> AccessUI(
            access = this,
            icon = if (useActionNames)
                R.drawable.ic_room_power_user else
                R.drawable.ic_access_editing,
            title = if (useActionNames)
                R.string.share_popup_access_full else
                R.string.share_access_room_editor
        )

        Access.Editor -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_editing,
            title = if (useActionNames)
                R.string.share_popup_access_editing else
                R.string.share_access_room_editor
        )

        Access.FormFiller -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_fill_form,
            title = if (useActionNames)
                R.string.share_popup_access_fill_forms else
                R.string.share_access_room_form_filler
        )

        Access.Read -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_read,
            title = if (useActionNames)
                R.string.share_popup_access_read_only else
                R.string.share_access_room_viewer
        )

        Access.Review -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_review,
            title = if (useActionNames)
                R.string.share_popup_access_review else
                R.string.share_access_room_reviewer
        )

        Access.RoomManager -> AccessUI(
            access = this,
            icon = R.drawable.ic_room_manager,
            title = R.string.share_access_room_manager
        )

        Access.ContentCreator -> AccessUI(
            access = this,
            icon = R.drawable.ic_room_power_user,
            title = R.string.share_access_room_power_user
        )

        Access.Restrict -> AccessUI(
            access = this,
            icon = R.drawable.ic_access_deny,
            title = R.string.share_popup_access_deny_access
        )

        Access.None -> AccessUI(
            access = this,
            icon = R.drawable.ic_list_context_delete,
            title = R.string.share_popup_access_remove
        )
    }
}

fun User.getTypeTitle(provider: PortalProvider?): Int {
    return when (type) {
        UserType.Owner -> {
            when (provider) {
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

val ShareType.titleWithCount: Int
    get() = when (this) {
        ShareType.Admin -> R.string.rooms_info_admin_title
        ShareType.User -> R.string.rooms_info_users_title
        ShareType.Group -> R.string.rooms_info_groups_title
        ShareType.Guests -> R.string.rooms_info_guests_title
        ShareType.Expected -> R.string.rooms_info_expected_title
        ShareType.Owner -> R.string.share_access_room_owner
    }