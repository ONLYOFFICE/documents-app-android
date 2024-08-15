package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.ui.fragments.main.AddRoomFragment
import app.editors.manager.viewModels.main.CopyItems
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getIntExt
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs


class AddRoomDialog : BaseDialogFragment() {

    companion object {

        val TAG: String = AddRoomDialog::class.java.simpleName

        fun newInstance(roomType: Int?, room: Item? = null, copyItems: CopyItems?): AddRoomDialog {
            return AddRoomDialog().putArgs(
                AddRoomFragment.TAG_ROOM_TYPE to roomType,
                AddRoomFragment.TAG_ROOM_INFO to room,
                AddRoomFragment.TAG_COPY_ITEMS to copyItems
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
        return AddRoomFragment.newInstance(
            arguments?.getIntExt(AddRoomFragment.TAG_ROOM_TYPE),
            arguments?.getSerializableExt(AddRoomFragment.TAG_ROOM_INFO),
            arguments?.getSerializableExt(AddRoomFragment.TAG_COPY_ITEMS)
        )
    }
}