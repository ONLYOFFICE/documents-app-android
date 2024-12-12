package app.editors.manager.ui.fragments.room.add

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.viewModels.main.RoomEditViewModel
import app.editors.manager.viewModels.main.RoomSettingsEffect
import app.editors.manager.viewModels.main.RoomUserListViewModel
import app.editors.manager.viewModels.main.UserListMode
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class EditRoomFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_ROOM_ID = "key_room_id"
        private const val TAG_RESULT = "edit_room_tag_result"

        private val TAG: String = EditRoomFragment::class.java.simpleName

        private fun newInstance(roomId: String): EditRoomFragment =
            EditRoomFragment().putArgs(KEY_ROOM_ID to roomId)

        fun show(activity: FragmentActivity, roomId: String, onResult: () -> Unit) {
            activity.supportFragmentManager
                .setFragmentResultListener(
                    TAG_RESULT,
                    activity,
                ) { _, _ -> onResult() }

            newInstance(roomId).show(activity.supportFragmentManager, TAG)
        }
    }

    private enum class Screens {
        Main, ChangeOwner
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val navController = rememberNavController()
            val roomId = remember { arguments?.getString(KEY_ROOM_ID) }.orEmpty()
            val viewModel = viewModel {
                RoomEditViewModel(
                    roomId = roomId,
                    contentResolver = requireContext().contentResolver,
                    roomProvider = requireContext().roomProvider
                )
            }

            val state = viewModel.state.collectAsState()
            val logoState = viewModel.logoState.collectAsState()
            val watermarkState = viewModel.watermarkState.collectAsState()
            val loadingState = viewModel.loading.collectAsState()
            val loadingRoomState = viewModel.loadingRoom.collectAsState()

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is RoomSettingsEffect.Error -> {
                            UiUtils.getSnackBar(requireActivity())
                                .setText(effect.message)
                                .show()
                        }

                        is RoomSettingsEffect.Success -> {
                            requireActivity().supportFragmentManager.setFragmentResult(
                                AddRoomFragment.TAG_RESULT,
                                Bundle.EMPTY
                            )
                            dismiss()
                        }
                    }
                }
            }


            NavHost(navController = navController, startDestination = Screens.Main.name) {
                composable(route = Screens.Main.name) {
                    RoomSettingsScreen(
                        isEdit = true,
                        isRoomTypeEditable = false,
                        loadingRoom = loadingRoomState.value,
                        state = state.value,
                        logoState = logoState.value,
                        watermarkState = watermarkState.value,
                        loadingState = loadingState.value,
                        onApply = viewModel::applyChanges,
                        onBack = ::dismiss,
                        onSetOwner = { navController.navigate(Screens.ChangeOwner.name) },
                        onSetImage = { uri, isWatermark ->
                            if (isWatermark) {
                                viewModel.setWatermarkImageUri(uri)
                            } else {
                                viewModel.setLogoUri(uri)
                            }
                        },
                        onSetName = viewModel::setName,
                        onAddTag = viewModel::addTag,
                        onRemoveTag = viewModel::removeTag,
                        onSetIndexing = viewModel::setIndexing,
                        onSetRestrict = viewModel::setRestrict,
                        onSetLifetimeEnable = { enabled ->
                            viewModel.updateLifeTimeState { it.copy(enabled = enabled) }
                        },
                        onSetLifetimeValue = { value ->
                            viewModel.updateLifeTimeState { it.copy(value = value) }
                        },
                        onSetLifetimePeriod = { period ->
                            viewModel.updateLifeTimeState { it.copy(period = period) }
                        },
                        onSetLifetimeAction = { isDeletePermanently ->
                            viewModel.updateLifeTimeState { it.copy(deletePermanently = isDeletePermanently) }
                        },
                        onWatermarkEnable = { enabled ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(enabled = enabled))
                            }
                        },
                        onSetWatermarkAddition = { additions ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(additions = additions))
                            }
                        },
                        onSetWatermarkStaticText = { text ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(text = text))
                            }
                        },
                        onSetWatermarkTextPosition = { rotate ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(rotate = rotate))
                            }
                        },
                        onSetWatermarkImageScale = { scale ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(imageScale = scale))
                            }
                        },
                        onSetWatermarkImageRotate = { rotate ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(rotate = rotate))
                            }
                        },
                        onSetWatermarkType = { type ->
                            viewModel.updateWatermarkState {
                                it.copy(watermark = it.watermark.copy(type = type))
                            }
                        },
                        onSetQuotaEnabled = { enabled ->
                            viewModel.updateStorageQuota {
                                it.copy(enabled = enabled)
                            }
                        },
                        onSetQuotaValue = { value ->
                            viewModel.updateStorageQuota {
                                it.copy(value = value)
                            }
                        },
                        onSetQuotaMeasurementUnit = { unit ->
                            viewModel.updateStorageQuota {
                                it.copy(unit = unit)
                            }
                        }
                    )
                }
                composable(Screens.ChangeOwner.name) {
                    val userListViewModel = RoomUserListViewModel(
                        roomId = roomId,
                        roomType = state.value.type,
                        roomOwnerId = state.value.owner.id,
                        mode = UserListMode.ChangeOwner,
                        shareService = requireContext().shareApi,
                        roomProvider = requireContext().roomProvider,
                        resourcesProvider = ResourcesProvider(requireContext())
                    )
                    UserListScreen(
                        viewModel = userListViewModel,
                        title = R.string.room_set_owner_title,
                        disableInvited = false,
                        onClick = { userId -> userListViewModel.setOwner(userId, leave = false) },
                        onBack = navController::popBackStackWhenResumed,
                        onSuccess = {
                            navController.popBackStackWhenResumed()
                            viewModel.setOwner(it)
                            requireActivity().supportFragmentManager.setFragmentResult(
                                TAG_RESULT,
                                Bundle.EMPTY
                            )
                        },
                        onSnackBar = {
                            UiUtils.getSnackBar(requireActivity()).setText(it).show()
                        }
                    )
                }
            }
        }
    }
}