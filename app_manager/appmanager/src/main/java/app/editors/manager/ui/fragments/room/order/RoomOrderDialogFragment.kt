package app.editors.manager.ui.fragments.room.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.databinding.FragmentDialogRoomOrderBinding
import app.editors.manager.viewModels.room.RoomOrderViewModel
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class RoomOrderDialogFragment : DialogFragment() {

    companion object {

        private val tag = RoomOrderDialogFragment::class.java.simpleName
        private const val KEY_FOLDER_ID = "folder_id"

        private fun newInstance(folderId: String): RoomOrderDialogFragment = RoomOrderDialogFragment()
            .putArgs(KEY_FOLDER_ID to folderId)

        fun show(fragmentManager: FragmentManager, folderId: String) {
            newInstance(folderId).show(fragmentManager, tag)
        }
    }

    private val viewModel: RoomOrderViewModel by viewModels<RoomOrderViewModel> {
        RoomOrderViewModel.Factory(
            folderId = arguments?.getString(KEY_FOLDER_ID).orEmpty(),
            roomProvider = requireContext().roomProvider
        )
    }

    private var binding: FragmentDialogRoomOrderBinding? = null

    override fun onStart() {
        super.onStart()
        if (UiUtils.isTablet(requireContext())) {
            setDialogSize()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDialogRoomOrderBinding.inflate(inflater).also { binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        inflateCloudFragment()
    }

    private fun initViews() {
        binding?.let { binding ->
            binding.toolbar.setNavigationOnClickListener { dismiss() }
            binding.apply.setOnClickListener { viewModel.apply() }
            binding.reorder.setOnClickListener { viewModel.reorder() }
        }
    }

    private fun inflateCloudFragment() {
        FragmentUtils.showFragment(
            childFragmentManager,
            RoomOrderFragment.newInstance(arguments?.getString(KEY_FOLDER_ID)!!),
            R.id.fragmentContainer
        )
    }

    private fun setDialogSize() {
        val width = resources.getDimension(lib.toolkit.base.R.dimen.accounts_dialog_fragment_width)
        val height = if (UiUtils.isLandscape(requireContext())) {
            resources.displayMetrics.heightPixels / 1.2
        } else {
            width * 1.3
        }
        dialog?.window?.setLayout(width.toInt(), height.toInt())
    }
}