package app.editors.manager.ui.fragments.room.order

import android.os.Bundle
import android.view.View
import app.editors.manager.mvp.presenters.main.PickerMode
import app.editors.manager.ui.fragments.main.DocsCloudFragment
import lib.toolkit.base.managers.utils.putArgs

class RoomOrderFragment : DocsCloudFragment() {

    companion object {

        fun newInstance(folderId: String): DocsCloudFragment {
            return RoomOrderFragment().putArgs(
                KEY_PATH to folderId
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.isEnabled = false
        explorerAdapter?.pickerMode = PickerMode.Ordering
    }

    override fun onActionBarTitle(title: String) {}
}