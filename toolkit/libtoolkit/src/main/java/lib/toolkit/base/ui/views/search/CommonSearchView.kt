package lib.toolkit.base.ui.views.search

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class CommonSearchView(
    private val coroutineScope: CoroutineScope,
    private val searchView: SearchView,
    private val hint: Int? = null,
    private val isExpanded: Boolean = false,
    private val onExpand: () -> Unit = {},
    private val onQuery: (String) -> Unit
) : SearchView.OnQueryTextListener {

    companion object {

        private const val DEBOUNCE_TIME = 400L
    }

    private val closeButton: ImageView by lazy { searchView.findViewById(androidx.appcompat.R.id.search_close_btn) }

    private var closeButtonEnabled: Boolean
        get() = closeButton.isEnabled
        set(enabled) {
            closeButton.isEnabled = enabled
            closeButton.alpha = if (enabled) 1f else 0.4f
        }

    private val debounceFlow: MutableStateFlow<String> = MutableStateFlow("")

    var query: String?
        get() = searchView.query?.toString()
        set(value) {
            searchView.setQuery(value, false)
        }

    init {
        closeButtonEnabled = false
        with(searchView) {

            textDirection = TextView.TEXT_DIRECTION_LOCALE
            maxWidth = Int.MAX_VALUE
            isIconified = !isExpanded

            closeButton.setOnClickListener { this@CommonSearchView.query = "" }
            setOnQueryTextListener(this@CommonSearchView)
            setOnSearchClickListener {
                onExpand.invoke()
            }

            findViewById<View>(androidx.appcompat.R.id.search_plate)?.background = null
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
                setTextAppearance(lib.toolkit.base.R.style.TextAppearance_Common_Title)
                hint = context.getString(this@CommonSearchView.hint ?: androidx.appcompat.R.string.abc_search_hint)
            }
        }

        coroutineScope.launch {
            debounceFlow
                .debounce(DEBOUNCE_TIME)
                .cancellable()
                .collect(onQuery)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        updateQuery(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        updateQuery(query)
        return false
    }

    // return true if now it is collapsed
    fun collapse() {
        query = null
        searchView.isIconified = true
    }

    private fun updateQuery(value: String?) {
        closeButtonEnabled = !value.isNullOrEmpty()
        if (value != null) {
            debounceFlow.value = value
        } else {
            coroutineScope.cancel()
        }
    }

}