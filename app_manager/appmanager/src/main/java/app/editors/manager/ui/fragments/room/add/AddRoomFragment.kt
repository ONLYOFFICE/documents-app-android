package app.editors.manager.ui.fragments.room.add

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.AddRoomItem
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.main.SelectFolderScreen
import app.editors.manager.viewModels.main.CopyItems
import app.editors.manager.viewModels.main.RoomAddViewModel
import app.editors.manager.viewModels.main.RoomSettingsEffect
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getIntExt
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs

private enum class Screens {
    Main, Select, Folder
}

class AddRoomFragment : ComposeDialogFragment() {

    companion object {

        const val TAG_ROOM_TYPE = "room_type"
        const val TAG_COPY_ITEMS = "files_copy"
        const val TAG_RESULT = "add_room_result"

        val TAG: String = EditRoomFragment::class.java.simpleName

        private fun newInstance(roomType: Int?, items: CopyItems?) =
            AddRoomFragment().putArgs(
                TAG_ROOM_TYPE to roomType,
                TAG_COPY_ITEMS to items
            )

        fun show(
            activity: FragmentActivity,
            type: Int? = null,
            copyItems: CopyItems? = null,
            onResult: (Bundle) -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                TAG_RESULT, activity
            ) { _, bundle -> onResult(bundle) }

            newInstance(type, copyItems).show(activity.supportFragmentManager, TAG)
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val navController = rememberNavController()
            val roomType = remember { arguments?.getIntExt(TAG_ROOM_TYPE) } ?: -1
            val copyItems = remember { arguments?.getSerializableExt<CopyItems>(TAG_COPY_ITEMS) }
            val viewModel = viewModel {
                RoomAddViewModel(
                    roomType = roomType,
                    copyItems = copyItems,
                    contentResolver = requireContext().contentResolver,
                    roomProvider = requireContext().roomProvider
                )
            }

            val storageActivityLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = {
                    if (it.resultCode == Activity.RESULT_OK) {
                        it.data?.getSerializableExt<CloudFolder>(StorageActivity.TAG_RESULT)
                            ?.let { folder -> viewModel.connectStorage(folder) }
                    }
                }
            )

            val state = viewModel.state.collectAsState()
            val logoState = viewModel.logoState.collectAsState()
            val watermarkState = viewModel.watermarkState.collectAsState()
            val loadingState = viewModel.loading.collectAsState()

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
                                TAG_RESULT,
                                bundleOf("id" to id, "type" to state.value.type)
                            )
                            dismiss()
                        }
                    }
                }
            }

            NavHost(navController = navController, startDestination = Screens.Main.name) {
                composable(route = Screens.Main.name) {
                    RoomSettingsScreen(
                        isEdit = false,
                        canApplyChanges = viewModel.canApplyChangesFlow.collectAsState(false).value,
                        isRoomTypeEditable = copyItems == null,
                        state = state.value,
                        logoState = logoState.value,
                        watermarkState = watermarkState.value,
                        loadingState = loadingState.value,
                        onCreateNewFolder = viewModel::setCreateNewFolder,
                        onLocationClick = { navController.navigate("${Screens.Folder.name}/$it") },
                        onApply = viewModel::applyChanges,
                        onBack = ::dismiss,
                        onSetImage = { uri, isWatermark ->
                            if (isWatermark) {
                                viewModel.setWatermarkImageUri(uri)
                            } else {
                                viewModel.setLogoUri(uri)
                            }
                        },
                        onStorageConnect = { isConnect ->
                            if (isConnect) {
                                storageActivityLauncher.launch(
                                    StorageActivity.getIntent(
                                        requireContext()
                                    )
                                )
                            } else {
                                viewModel.disconnectStorage()
                            }
                        },
                        onSelectType = { navController.navigate(Screens.Select.name) },
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
                            viewModel.updateLifeTimeState { it.copy(enabled = enabled) }
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
                composable(Screens.Select.name) {
                    RoomSettingsSelectRoomScreen(
                        currentType = state.value.type,
                        navController = navController,
                        select = viewModel::setType
                    )
                }
                composable(
                    route = "${Screens.Folder.name}/{folderId}",
                    arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                ) {
                    SelectFolderScreen(
                        folderId = it.arguments?.getString("folderId").orEmpty(),
                        onBack = navController::popBackStack,
                        onAccept = viewModel::setStorageLocation
                    )
                }
            }
        }
    }
}

@Composable
fun RoomSettingsSelectRoomScreen(
    currentType: Int,
    navController: NavHostController,
    select: (Int) -> Unit
) {
    AppScaffold(topBar = {
        AppTopBar(
            title = stringResource(id = R.string.rooms_choose_room),
            backListener = navController::popBackStack
        )
    }, useTablePaddings = false) {
        Column {
            for (type in RoomUtils.roomTypes) {
                AddRoomItem(roomType = type, selected = currentType == type) { newType ->
                    select(newType)
                    navController.navigate(Screens.Main.name) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}