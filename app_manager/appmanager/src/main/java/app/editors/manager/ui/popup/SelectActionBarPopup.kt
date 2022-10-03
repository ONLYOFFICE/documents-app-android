package app.editors.manager.ui.popup

import android.content.Context
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import app.editors.manager.R
import lib.toolkit.base.databinding.IncludeActionBarItemBinding
import lib.toolkit.base.databinding.PopupActionBarBinding
import lib.toolkit.base.ui.popup.ActionBarPopup
import lib.toolkit.base.ui.popup.ActionBarPopupItem
import java.lang.IndexOutOfBoundsException

class SelectActionBarPopup(
    context: Context,
    clickListener: (ActionBarPopupItem) -> Unit,
    excluded: List<ActionBarPopupItem> = emptyList()
) : ActionBarPopup(context) {

    object Move : ActionBarPopupItem(R.string.toolbar_menu_main_move)
    object Copy : ActionBarPopupItem(R.string.toolbar_menu_main_copy)
    object Deselect : ActionBarPopupItem(R.string.toolbar_menu_main_deselect)
    object SelectAll : ActionBarPopupItem(R.string.toolbar_menu_main_select_all)
    object Restore : ActionBarPopupItem(R.string.device_trash_files_restore)
    object Download : ActionBarPopupItem(R.string.toolbar_menu_main_download)

    companion object {
        private val items: MutableList<ActionBarPopupItem> =
            mutableListOf(Move, Copy, Deselect, SelectAll, Restore, Download)

    }

    private var viewBinding: PopupActionBarBinding? = null

    init {
        viewBinding = PopupActionBarBinding.inflate(inflater).apply {
            divider.isVisible = false
            popupMenu.children
                .filterIsInstance(LinearLayout::class.java)
                .forEachIndexed { index, view ->
                    try {
                        with(IncludeActionBarItemBinding.bind(view)) {
                            popupMenuOrder.isVisible = false
                            popupMenuText.text = context.getText(items[index].title)
                            popupMenuItem.isVisible = !excluded.contains(items[index])
                            popupMenuItem.setOnClickListener {
                                clickListener(items[index])
                                hide()
                            }
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        view.isVisible = false
                    }
                }
        }.also {
            contentView = it.root
        }
    }

    override fun hide() {
        super.hide()
        viewBinding = null
    }
}