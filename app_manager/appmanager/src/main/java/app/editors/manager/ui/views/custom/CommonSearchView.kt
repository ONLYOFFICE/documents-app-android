package app.editors.manager.ui.views.custom

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import app.editors.manager.R

class CommonSearchView(
    var searchView: SearchView?,
    var isIconified: Boolean = false,
    var queryTextListener: SearchView.OnQueryTextListener? = null,
    var searchClickListener: (() -> Unit)? = null,
    var closeClickListener: (() -> Unit)? = null
) {
    val closeButton: ImageView?
        get() = searchView?.findViewById(androidx.appcompat.R.id.search_close_btn)

    fun build(): SearchView {
        return searchView?.apply {
            textDirection = TextView.TEXT_DIRECTION_LOCALE
            maxWidth = Int.MAX_VALUE
            isIconified = this@CommonSearchView.isIconified
            closeButton?.setOnClickListener { closeClickListener?.invoke() }
            setOnQueryTextListener(queryTextListener)
            setOnSearchClickListener { searchClickListener?.invoke() } // On search open
            findViewById<View>(androidx.appcompat.R.id.search_plate)?.background = null
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
                setTextAppearance(lib.toolkit.base.R.style.TextAppearance_Common_Title)
                hint = context.getString(R.string.toolbar_menu_search_view_hint)
            }
        } ?: throw NullPointerException("View can not be null")
    }
}