package app.editors.manager.managers.utils

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
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
import lib.toolkit.base.managers.utils.UiUtils

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
                .load(GlideUtils.getCorrectLoad(account.avatarUrl!!, account.token ?: ""))
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

    @JvmStatic
    fun setFileIcon(view: ImageView, ext: String) {
        val extension = StringUtils.getExtension(ext)
        @DrawableRes var resId = R.drawable.ic_type_file
        @ColorRes var colorId = lib.toolkit.base.R.color.colorGrey
        when (extension) {
            StringUtils.Extension.DOC -> {
                resId = R.drawable.ic_type_text_document
                colorId = lib.toolkit.base.R.color.colorDocTint
            }
            StringUtils.Extension.FORM -> {
                resId = if (ext == ".${LocalContentTools.OFORM_EXTENSION}") {
                    R.drawable.ic_format_oform
                } else {
                    R.drawable.ic_format_docxf
                }
                colorId = lib.toolkit.base.R.color.colorTransparent
            }
            StringUtils.Extension.SHEET -> {
                resId = R.drawable.ic_type_spreadsheet
                colorId = lib.toolkit.base.R.color.colorSheetTint
            }
            StringUtils.Extension.PRESENTATION -> {
                resId = R.drawable.ic_type_presentation
                colorId = lib.toolkit.base.R.color.colorPresentationTint
            }
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> {
                resId = R.drawable.ic_type_image
                colorId = lib.toolkit.base.R.color.colorPicTint
            }
            StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.PDF -> {
                resId = R.drawable.ic_type_pdf
                colorId = lib.toolkit.base.R.color.colorLightRed
            }
            StringUtils.Extension.VIDEO_SUPPORT, StringUtils.Extension.VIDEO -> {
                resId = R.drawable.ic_type_video
            }
            StringUtils.Extension.ARCH -> {
                setAlphaIcon(view, R.drawable.ic_type_archive)
                return
            }
            StringUtils.Extension.UNKNOWN -> {
                setAlphaIcon(view, R.drawable.ic_type_file)
                return
            }
        }
        view.setImageResource(resId)
        view.alpha = 1.0f
        view.setColorFilter(ContextCompat.getColor(view.context, colorId))
    }

    private fun setAlphaIcon(view: ImageView, @DrawableRes resId: Int) {
        view.setImageResource(resId)
        view.alpha = UiUtils.getFloatResource(view.context, lib.toolkit.base.R.dimen.alpha_medium)
        view.clearColorFilter()
    }

    fun setFolderIcon(view: ImageView, folder: CloudFolder, isRoot: Boolean) {
        @DrawableRes var resId = R.drawable.ic_type_folder
        if (folder.shared && folder.providerKey.isEmpty()) {
            resId = R.drawable.ic_type_folder_shared
        } else if (isRoot && folder.providerItem && folder.providerKey.isNotEmpty()) {
            when (folder.providerKey) {
                ApiContract.Storage.BOXNET -> resId = R.drawable.ic_storage_box
                ApiContract.Storage.DROPBOX -> resId = R.drawable.ic_storage_dropbox
                ApiContract.Storage.SHAREPOINT -> resId = R.drawable.ic_storage_sharepoint
                ApiContract.Storage.GOOGLEDRIVE -> resId = R.drawable.ic_storage_google
                ApiContract.Storage.ONEDRIVE, ApiContract.Storage.SKYDRIVE -> resId =
                    R.drawable.ic_storage_onedrive
                ApiContract.Storage.YANDEX -> resId = R.drawable.ic_storage_yandex
                ApiContract.Storage.WEBDAV -> {
                    resId = R.drawable.ic_storage_webdav
                    view.setImageResource(resId)
                    view.alpha =
                        UiUtils.getFloatResource(view.context, lib.toolkit.base.R.dimen.alpha_medium)
                    return
                }
            }
            view.setImageResource(resId)
            view.alpha = 1.0f
            view.clearColorFilter()
            return
        }
        view.setImageResource(resId)
        view.alpha =
            UiUtils.getFloatResource(view.context, lib.toolkit.base.R.dimen.alpha_medium)
        if (UiUtils.isDarkMode(view.context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                view.colorFilter = BlendModeColorFilter(
                    ContextCompat.getColor(view.context, lib.toolkit.base.R.color.colorOnSurface),
                    BlendMode.SRC_ATOP
                )
            } else {
                view.setColorFilter(
                    ContextCompat.getColor(view.context, lib.toolkit.base.R.color.colorOnSurface),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    fun setAccessIcon(imageView: ImageView, accessCode: Int) {
        when (accessCode) {
            ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> {
                imageView.setImageResource(R.drawable.ic_access_deny)
                return
            }
            ApiContract.ShareCode.REVIEW -> imageView.setImageResource(R.drawable.ic_access_review)
            ApiContract.ShareCode.READ -> imageView.setImageResource(R.drawable.ic_access_read)
            ApiContract.ShareCode.READ_WRITE -> imageView.setImageResource(R.drawable.ic_access_full)
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

var View.isVisible: Boolean
    get() = this.visibility == View.VISIBLE
    set(isVisible) {
        this.visibility = if (isVisible) View.VISIBLE else View.GONE
    }