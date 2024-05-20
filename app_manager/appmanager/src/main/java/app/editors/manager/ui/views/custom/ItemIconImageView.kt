package app.editors.manager.ui.views.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.ItemIconImageLayoutBinding
import app.editors.manager.managers.utils.ManagerUiUtils.setFileIcon
import app.editors.manager.managers.utils.ManagerUiUtils.setFolderIcon
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.StorageUtils

class ItemIconImageView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding: ItemIconImageLayoutBinding

    var selectMode: Boolean = false
        set(value) {
            field = value
            foreground = if (value)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_background) else null
        }

    var itemSelected: Boolean = false
        set(value) {
            field = value
            foreground = if (value)
                AppCompatResources.getDrawable(context, R.drawable.drawable_list_image_select_foreground) else null
        }

    private val imageView: ImageView get() = binding.image

    private val textView: TextView get() = binding.text

    init {
        val view = inflate(context, R.layout.item_icon_image_layout, this)
        binding = ItemIconImageLayoutBinding.bind(view)
        context.obtainStyledAttributes(attrs, R.styleable.ItemIconImageView).apply {
            val badge = getDrawable(R.styleable.ItemIconImageView_badge)
            if (badge != null) {
                binding.badge.setImageDrawable(badge)
                binding.badge.isVisible = true
            }
            recycle()
        }
    }

    fun setItem(item: Item, isRoot: Boolean = false) {
        when (item) {
            is CloudFile -> setCloudFile(item)
            is CloudFolder -> setCloudFolder(item, isRoot)
            else -> throw IllegalArgumentException(
                "Item must be ${CloudFile::class.simpleName}" +
                        " or ${CloudFolder::class.simpleName}"
            )
        }
    }

    fun setIconFromExtension(extension: String) {
        imageView.setFileIcon(extension)
    }

    private fun setCloudFile(file: CloudFile) {
        imageView.setFileIcon(file.fileExst)
    }

    private fun setCloudFolder(folder: CloudFolder, isRoot: Boolean) {
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

            if (folder.providerItem && folder.providerKey.isNotEmpty()) {
                binding.badge.setImageResource(StorageUtils.getStorageIcon(folder.providerKey))
                binding.badge.isVisible = true
            } else if (folder.roomType == ApiContract.RoomType.PUBLIC_ROOM) {
                binding.badge.setImageResource(R.drawable.ic_public_room_badge)
                binding.badge.isVisible = true
            } else {
                binding.badge.isVisible = false
            }
        } else {
            textView.isVisible = false
            imageView.isVisible = true
            imageView.setFolderIcon(folder, isRoot)
        }
    }
}