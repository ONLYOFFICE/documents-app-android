package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.ui.fragments.main.AddRoomFragment
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs


class AddRoomDialog : BaseDialogFragment() {

    companion object {

        val TAG: String = AddRoomDialog::class.java.simpleName

        fun newInstance(roomType: Int, room: Item? = null, isCopy: Boolean = false): AddRoomDialog {
            return AddRoomDialog().putArgs(
                AddRoomFragment.TAG_ROOM_TYPE to roomType,
                AddRoomFragment.TAG_ROOM_INFO to room,
                AddRoomFragment.TAG_COPY to isCopy
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UiUtils.isTablet(requireContext())) {
            setStyle(
                STYLE_NORMAL,
                R.style.FullScreenDialog
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.appbarLayout?.isVisible = false
        showFragment(getInstance())
    }

    override fun onBackPressed(): Boolean {
        return if ((childFragmentManager.findFragmentByTag(AddRoomFragment.TAG)
                    as? AddRoomFragment)?.onBackPressed() == true
        ) true
        else super.onBackPressed()
    }

    private fun getInstance(): Fragment {
        return AddRoomFragment().putArgs(
            AddRoomFragment.TAG_ROOM_TYPE to arguments?.getInt(AddRoomFragment.TAG_ROOM_TYPE),
            AddRoomFragment.TAG_ROOM_INFO to arguments?.getSerializableExt(AddRoomFragment.TAG_ROOM_INFO),
            AddRoomFragment.TAG_COPY to arguments?.getBoolean(AddRoomFragment.TAG_COPY)
        )
    }
}