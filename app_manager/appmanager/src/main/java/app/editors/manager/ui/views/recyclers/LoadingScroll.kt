package app.editors.manager.ui.views.recyclers

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class LoadingScroll : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager) ?: return
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItemPosition >= totalItemCount - VISIBLE_THRESHOLD) {
                onListEnd()
            }
        }
    }

    abstract fun onListEnd()

    companion object {
        private const val VISIBLE_THRESHOLD = 15
    }
}