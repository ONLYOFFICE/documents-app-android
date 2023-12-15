package app.editors.manager.ui.views.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.ItemIconImageLayoutBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.editors.manager.managers.utils.ManagerUiUtils.setFolderIcon
import app.editors.manager.managers.utils.RoomUtils

class ItemIconImageView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val binding: ItemIconImageLayoutBinding

    var selectMode: Boolean = false
        set(value) {
            field = value
            background = if (value)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_background) else null
        }

    var itemSelected: Boolean = false
        set(value) {
            field = value
            foreground = if (value)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_foreground) else null
        }

    var item: Item? = null
        set(value) {
            field = value
            when (value) {
                is CloudFile -> setCloudFile(value)
                is CloudFolder -> setCloudFolder(value)
                else -> throw IllegalArgumentException(
                    "Item must be ${CloudFile::class.simpleName}" +
                            " or ${CloudFolder::class.simpleName}"
                )
            }
        }

    private val imageView: ImageView get() = binding.image

    private val textView: TextView get() = binding.text

    init {
        val view = inflate(context, R.layout.item_icon_image_layout, this)
        binding = ItemIconImageLayoutBinding.bind(view)
    }

    fun setIconFromExtension(extension: String) {
        imageView.setFileIcon(extension)
    }

    private fun setCloudFile(file: CloudFile) {
        imageView.setFileIcon(file.fileExst)
    }

    private fun setCloudFolder(folder: CloudFolder) {
        val initials = RoomUtils.getRoomInitials(folder.title)
        if (folder.isRoom) {
            if (!folder.logo?.large.isNullOrEmpty()) {
                textView.isVisible = false
                imageView.isVisible = true
                imageView.setFolderIcon(folder, false)
            } else if (!initials.isNullOrEmpty()) {
                imageView.isVisible = false
                textView.isVisible = true
                textView.text = initials
                textView.backgroundTintList =
                    ColorStateList.valueOf(
                        folder.logo?.color?.let { color -> Color.parseColor("#$color") }
                            ?: context.getColor(lib.toolkit.base.R.color.colorPrimary)
                    )
            }
        } else {
            textView.isVisible = false
            imageView.isVisible = true
            imageView.setFolderIcon(folder, false)
        }
    }

}