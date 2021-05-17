package app.editors.manager.ui.adapters

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.Recent
import app.editors.manager.R
import app.editors.manager.app.App
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.StringUtils.getExtension
import lib.toolkit.base.managers.utils.StringUtils.getExtensionFromPath
import lib.toolkit.base.managers.utils.StringUtils.getFormattedSize
import lib.toolkit.base.managers.utils.TimeUtils.getWeekDate
import lib.toolkit.base.managers.utils.UiUtils
import java.util.*

class RecentViewHolder(
    private val view: View,
    private val itemListener: ((recent: Recent, position: Int) -> Unit)? = null,
    private val contextListener: ((recent: Recent, position: Int) -> Unit)? = null
) :
    RecyclerView.ViewHolder(view) {

    companion object {
        private const val COUNT_SEPARATOR = 4
        private const val PATH_SEPARATOR = '/'
    }

    private val recentImage: AppCompatImageView = view.findViewById(R.id.view_icon_selectable_image)
    private val recentFileName: AppCompatTextView = view.findViewById(R.id.list_explorer_file_name)
    private val recentFileInfo: AppCompatTextView = view.findViewById(R.id.list_explorer_file_info)
    private val recentContext: AppCompatImageButton = view.findViewById(R.id.list_explorer_file_context)
    private val recentFileLayout: ConstraintLayout = view.findViewById(R.id.list_explorer_file_layout)

    fun bind(recent: Recent) {
        recentFileName.text = recent.name
        val info = getWeekDate(Date(recent.date)) + App.getApp().getString(R.string.placeholder_point) +
                getFormattedSize(view.context, recent.size)
        recentFileInfo.text = info

        recentFileLayout.setOnClickListener {
            itemListener?.invoke(recent, absoluteAdapterPosition)
        }
        recentContext.setOnClickListener {
            contextListener?.invoke(recent, absoluteAdapterPosition)
        }

        setFileIcon(recentImage, getExtensionFromPath(recent.name.toLowerCase(Locale.ROOT)))
    }

    private fun getPath(path: String): String {
        var countSeparator = 0
        for (i in path.indices) {
            if (path[i] == PATH_SEPARATOR) {
                countSeparator++
                if (countSeparator == COUNT_SEPARATOR) {
                    return path.substring(i, path.length - 1)
                }
            }
        }
        return path
    }

    private fun setFileIcon(view: AppCompatImageView, ext: String) {
        val extension = getExtension(ext)
        @DrawableRes var resId = R.drawable.ic_type_file
        @ColorRes var colorId = R.color.colorGrey
        when (extension) {
            StringUtils.Extension.DOC -> {
                resId = R.drawable.ic_type_text_document
                colorId = R.color.colorDocTint
            }
            StringUtils.Extension.SHEET -> {
                resId = R.drawable.ic_type_spreadsheet
                colorId = R.color.colorSheetTint
            }
            StringUtils.Extension.PRESENTATION -> {
                resId = R.drawable.ic_type_presentation
                colorId = R.color.colorPresentationTint
            }
            StringUtils.Extension.IMAGE, StringUtils.Extension.IMAGE_GIF -> {
                resId = R.drawable.ic_type_image
                colorId = R.color.colorPicTint
            }
            StringUtils.Extension.HTML, StringUtils.Extension.EBOOK, StringUtils.Extension.PDF -> {
                resId = R.drawable.ic_type_pdf
                colorId = R.color.colorPdfTint
            }
            StringUtils.Extension.VIDEO_SUPPORT -> {
                resId = R.drawable.ic_type_video
                colorId = R.color.colorVideoTint
            }
            StringUtils.Extension.VIDEO -> {
                setAlphaIcon(view, R.drawable.ic_type_video)
                return
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

    private fun setAlphaIcon(view: AppCompatImageView, @DrawableRes res: Int) {
        view.setImageResource(res)
        view.alpha = UiUtils.getFloatResource(view.context, R.dimen.alpha_medium)
        view.clearColorFilter()
    }

}