package app.editors.manager.ui.fragments.room.order

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.app.App
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.adapters.ExplorerAdapter.Companion.getTouchHelperCallback
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import app.editors.manager.viewModels.room.RoomOrderHelper
import lib.toolkit.base.managers.utils.putArgs
import javax.inject.Inject

class RoomOrderFragment : DocsCloudFragment() {

    companion object {

        fun newInstance(folderId: String): DocsCloudFragment {
            return RoomOrderFragment().putArgs(KEY_PATH to folderId)
        }
    }

    @Inject
    lateinit var roomOrderHelper: RoomOrderHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cloudPresenter.isIndexing = true
        isGridView = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.isEnabled = false
        explorerAdapter?.let { adapter ->
            val touchHelper = ItemTouchHelper(getTouchHelperCallback(adapter, ::onMove))
            touchHelper.attachToRecyclerView(recyclerView)
            adapter.onDragStartListener = touchHelper::startDrag
            adapter.pickerMode = PickerMode.Ordering
            adapter.isGridView = false
        }
    }

    private fun onMove(items: List<Item>) {
        roomOrderHelper.setItems(items)
    }

    override fun onActionBarTitle(title: String) {}
}