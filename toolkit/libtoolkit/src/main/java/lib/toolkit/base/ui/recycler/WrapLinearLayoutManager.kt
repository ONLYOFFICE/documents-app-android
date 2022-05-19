package lib.toolkit.base.ui.recycler

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class WrapLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens")
        }
    }
}