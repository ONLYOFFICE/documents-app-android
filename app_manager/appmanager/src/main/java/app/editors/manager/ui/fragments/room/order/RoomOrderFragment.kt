package app.editors.manager.ui.fragments.room.order

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import lib.toolkit.base.managers.utils.putArgs
import java.util.Collections

class RoomOrderFragment : DocsCloudFragment() {

    companion object {

        fun newInstance(folderId: String): DocsCloudFragment {
            return RoomOrderFragment().putArgs(
                KEY_PATH to folderId
            )
        }

        fun getTouchHelperCallback(onMove: (from: Int, to: Int) -> Unit): ItemTouchHelper.SimpleCallback {
            return object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    onMove(
                        viewHolder.absoluteAdapterPosition,
                        target.absoluteAdapterPosition
                    )
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.isEnabled = false
        explorerAdapter?.pickerMode = PickerMode.Ordering
        ItemTouchHelper(getTouchHelperCallback(::onMove)).attachToRecyclerView(recyclerView)
    }

    private fun onMove(from: Int, to: Int) {
        try {
            Collections.swap(explorerAdapter?.itemList.orEmpty(), from, to)
            explorerAdapter?.notifyItemMoved(from, to)
        } catch (_: IndexOutOfBoundsException) {

        }
    }

    override fun onActionBarTitle(title: String) {}
}