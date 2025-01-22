package app.editors.manager.ui.fragments.room.order

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.roomProvider
import app.editors.manager.databinding.FragmentDialogRoomOrderBinding
import app.editors.manager.viewModels.room.RoomOrderEffect
import app.editors.manager.viewModels.room.RoomOrderHelper
import app.editors.manager.viewModels.room.RoomOrderViewModel
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs
import javax.inject.Inject

class RoomOrderDialogFragment : DialogFragment() {

    companion object {

        private val tag = RoomOrderDialogFragment::class.java.simpleName
        private const val KEY_FOLDER_ID = "folder_id"
        private const val TAG_RESULT = "room_order_tag_result"

        private fun newInstance(folderId: String): RoomOrderDialogFragment =
            RoomOrderDialogFragment()
                .putArgs(KEY_FOLDER_ID to folderId)

        fun show(activity: FragmentActivity, folderId: String, onSuccess: () -> Unit) {
            activity.supportFragmentManager
                .setFragmentResultListener(
                    TAG_RESULT,
                    activity,
                ) { _, _ -> onSuccess() }

            newInstance(folderId).show(activity.supportFragmentManager, tag)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    @Inject
    lateinit var roomOrderHelper: RoomOrderHelper

    private val viewModel: RoomOrderViewModel by viewModels<RoomOrderViewModel> {
        RoomOrderViewModel.Factory(
            roomProvider = requireContext().roomProvider,
            roomOrderHelper = roomOrderHelper
        )
    }

    private var binding: FragmentDialogRoomOrderBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialog
        )
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
        collectEffect()
    }

    private fun collectEffect() {
        lifecycleScope.launch {
            viewModel.effect.collect { result ->
                when (result) {
                    RoomOrderEffect.Error -> UiUtils.getSnackBar(requireView())
                        .setText(R.string.errors_unknown_error)
                        .show()

                    RoomOrderEffect.Success -> {
                        refreshParentFragment()
                        dismiss()
                    }

                    RoomOrderEffect.Refresh -> {
                        refreshParentFragment()
                        inflateCloudFragment()
                    }
                }
            }
        }
    }

    private fun initViews() {
        binding?.let { binding ->
            binding.toolbar.setNavigationOnClickListener { close() }
            binding.apply.setOnClickListener { viewModel.apply() }
            binding.reorder.setOnClickListener { reorder() }

            lifecycleScope.launch {
                viewModel.loading.collect { loading ->
                    binding.loadingIndicator.isVisible = loading
                    binding.apply.isEnabled = !loading
                }
            }
        }
    }

    private fun inflateCloudFragment() {
        FragmentUtils.showFragment(
            childFragmentManager,
            RoomOrderFragment.newInstance(arguments?.getString(KEY_FOLDER_ID)!!),
            R.id.fragmentContainer
        )
    }

    private fun reorder() {
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = getString(R.string.dialogs_warning_title),
            description = getString(R.string.rooms_index_reorder_desc),
            acceptListener = { viewModel.reorder(arguments?.getString(KEY_FOLDER_ID).orEmpty()) },
            acceptTitle = getString(R.string.rooms_index_reorder)
        )
    }

    private fun close() {
        if (roomOrderHelper.hasChanges) {
            UiUtils.showQuestionDialog(
                context = requireContext(),
                title = getString(R.string.dialogs_warning_title),
                description = getString(R.string.rooms_index_exit_no_saving),
                acceptListener = ::dismiss
            )
        } else {
            dismiss()
        }
    }

    private fun refreshParentFragment() {
        requireActivity().supportFragmentManager.setFragmentResult(
            TAG_RESULT,
            Bundle.EMPTY
        )
    }
}