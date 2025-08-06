package app.editors.manager.ui.fragments.share

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.viewModels.main.RoomUserListViewModel
import app.editors.manager.viewModels.main.UserListMode
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

class SetRoomOwnerFragment : ComposeDialogFragment() {

    companion object {
        val TAG: String = SetRoomOwnerFragment::class.java.simpleName
        private const val ROOM_ID_KEY = "room_key"
        private const val ROOM_CREATED_BY_ID_KEY = "room_created_by_id_key"
        private const val SET_OWNER_REQUEST_KEY = "set_owner_request_key"

        fun newInstance(room: CloudFolder?): SetRoomOwnerFragment {
            return SetRoomOwnerFragment().putArgs(
                ROOM_ID_KEY to room?.id,
                ROOM_CREATED_BY_ID_KEY to room?.createdBy?.id
            )
        }

        fun show(room: CloudFolder?, activity: FragmentActivity, onClose: () -> Unit) {
            activity.supportFragmentManager.setFragmentResultListener(
                SET_OWNER_REQUEST_KEY,
                activity
            ) { _, _ -> onClose() }
            newInstance(room).show(activity.supportFragmentManager, null)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val viewModel = viewModel {
                RoomUserListViewModel(
                    mode = UserListMode.ChangeOwner,
                    roomId = checkNotNull(arguments?.getSerializableExt<String>(ROOM_ID_KEY)),
                    roomOwnerId = checkNotNull(arguments?.getSerializableExt<String>(ROOM_CREATED_BY_ID_KEY)),
                    roomProvider = requireContext().roomProvider,
                    resourcesProvider = requireContext().appComponent.resourcesProvider,
                )
            }

            UserListScreen(
                viewModel = viewModel,
                title = R.string.room_set_owner_title,
                onClick = { userId -> viewModel.setOwner(userId, leave = true) },
                onBack = ::dismiss,
                onSuccess = {
                    requireActivity()
                        .supportFragmentManager
                        .setFragmentResult(SET_OWNER_REQUEST_KEY, Bundle.EMPTY)
                    dismiss()
                },
                onSnackBar = { UiUtils.getSnackBar(requireActivity()).setText(it).show() },
            )
        }
    }
}