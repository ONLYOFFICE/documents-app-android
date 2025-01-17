package app.editors.manager.ui.adapters.holders

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter


abstract class BaseViewHolderExplorer<T>(
    itemView: View,
    @JvmField protected var adapter: ExplorerAdapter
) : RecyclerView.ViewHolder(itemView) {

    protected val icon: Bitmap
        get() {
            val view = getCachedIcon() ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            return try {
                val bitmap = Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                bitmap
            } catch (_: Exception) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }

    protected abstract val selectIcon: ImageView?

    protected abstract val root: View?

    abstract fun bind(element: T)

    protected abstract fun getCachedIcon(): View?

    protected open fun initSelecting(element: T): Boolean {
        val selectable = when (adapter.pickerMode) {
            is PickerMode.Files.Any -> element is CloudFile
            is PickerMode.Files.PDFForm -> element is CloudFile && element.isPdfForm
            is PickerMode.Ordering -> false
            else -> adapter.isSelectMode
        }

        val selected = when (val adapter = adapter.pickerMode) {
            is PickerMode.Files -> (element as? CloudFile)?.id in adapter.selectedIds
            else -> (element as? Item)?.isSelected == true
        }

        selectIcon?.isVisible = selectable

        if (selected) {
            selectIcon?.setImageResource(R.drawable.ic_select_checked)
        } else {
            selectIcon?.setImageResource(R.drawable.ic_select_not_checked)
        }

        return selected
    }

    protected fun setElementClickable(element: T) {
        val pickerMode = adapter.pickerMode
        if (pickerMode == PickerMode.None) {
            return
        }

        if (pickerMode == PickerMode.Ordering) {
            root?.let { root ->
                root.isClickable = false
                root.isFocusable = false
                root.setOnClickListener(null)
                root.foreground = null
            }
            return
        }

        val clickable = when (element) {
            is CloudFile -> {
                if (pickerMode is PickerMode.Files) {
                    element.folderId != pickerMode.destFolderId
                    if (pickerMode is PickerMode.Files.PDFForm) element.isPdfForm else false
                } else {
                    true
                }
            }

            is CloudFolder -> {
                element.type !in arrayOf(25, 26)
            }

            else -> true
        }

        if (clickable) {
            root?.let { root ->
                root.alpha = 1f
                root.isClickable = true
                root.isFocusable = true
                root.setOnClickListener { view ->
                    adapter.mOnItemClickListener?.onItemClick(view, layoutPosition)
                }
            }
        } else {
            root?.let { root ->
                root.alpha = .6f
                root.isClickable = false
                root.isFocusable = false
                root.setOnClickListener(null)
            }
        }
    }

    protected fun bindFolderStorageImage(
        folder: CloudFolder,
        storageImageView: ImageView
    ) {
        with(folder) {
            storageImageView.isVisible = false
            if (providerItem && providerKey.isNotEmpty() && adapter.isRoot) {
                storageImageView.isVisible = true
                StorageUtils.getStorageIconLarge(providerKey)
                    ?.let(storageImageView::setImageResource)
            }
        }
    }

    protected fun setRoomExpiring(element: T, textView: TextView) {
        if (adapter.pickerMode == PickerMode.Folders) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                AppCompatResources.getDrawable(
                    textView.context,
                    lib.toolkit.base.R.drawable.ic_expiring
                ).takeIf { element is CloudFolder && element.lifetime != null },
                null
            )
        }
    }

    protected fun setFileExpiring(element: CloudFile, textView: TextView) {
        if (element.isExpiringSoon) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_small_clock, 0)
        } else {
            textView.setCompoundDrawables(null, null, null, null)
        }
    }
}