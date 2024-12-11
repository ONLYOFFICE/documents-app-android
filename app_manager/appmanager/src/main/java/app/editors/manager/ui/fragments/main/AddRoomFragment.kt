@file:OptIn(ExperimentalGlideComposeApi::class)

package app.editors.manager.ui.fragments.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.Lifetime
import app.documents.core.network.manager.models.explorer.Watermark
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.ui.SizeUnit
import app.editors.manager.mvp.models.ui.StorageQuota
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.AddRoomItem
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.viewModels.main.AddRoomData
import app.editors.manager.viewModels.main.AddRoomEffect
import app.editors.manager.viewModels.main.AddRoomViewModel
import app.editors.manager.viewModels.main.CopyItems
import app.editors.manager.viewModels.main.RoomUserListViewModel
import app.editors.manager.viewModels.main.StorageState
import app.editors.manager.viewModels.main.UserListMode
import app.editors.manager.viewModels.main.ViewState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.ChipData
import lib.compose.ui.views.ChipsTextField
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.managers.utils.getIntExt
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import java.io.File

private enum class Screens {
    Main, Select, Folder, ChangeOwner
}

class AddRoomFragment : ComposeDialogFragment() {

    companion object {

        const val TAG_ROOM_TYPE = "room_type"
        const val TAG_ROOM_INFO = "room_info"
        const val TAG_COPY_ITEMS = "files_copy"
        const val TAG_RESULT = "add_room_result"

        val TAG: String = AddRoomFragment::class.java.simpleName

        private fun newInstance(roomType: Int?, room: Item?, items: CopyItems?) =
            AddRoomFragment().putArgs(
                TAG_ROOM_TYPE to roomType,
                TAG_ROOM_INFO to room,
                TAG_COPY_ITEMS to items
            )

        fun show(
            activity: FragmentActivity,
            type: Int? = null,
            room: CloudFolder? = null,
            copyItems: CopyItems? = null,
            onResult: (Bundle) -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                TAG_RESULT, activity
            ) { _, bundle -> onResult(bundle) }
            newInstance(type, room, copyItems)
                .show(activity.supportFragmentManager, TAG)
        }
    }

    private val copyItems: CopyItems? by lazy { arguments?.getSerializableExt(TAG_COPY_ITEMS) }

    private val roomData: CloudFolder? by lazy { arguments?.getSerializableExt(TAG_ROOM_INFO) }

    private val isEdit: Boolean get() = roomData != null

    @Composable
    override fun Content() {
        ManagerTheme {
            val navController = rememberNavController()
            val room = remember { arguments?.getSerializableExt<Item>(TAG_ROOM_INFO) }
            val roomType =
                remember { arguments?.getIntExt(TAG_ROOM_TYPE) ?: (room as? CloudFolder)?.roomType }
            val viewModel = viewModel {
                AddRoomViewModel(
                    context = requireActivity().application,
                    roomProvider = requireContext().roomProvider,
                    roomType = roomType,
                    roomInfo = room,
                    copyItems = copyItems
                )
            }
            val roomState = viewModel.roomState.collectAsState()
            val viewState = viewModel.viewState.collectAsState()
            val storageActivityLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        it.data?.getSerializableExt<CloudFolder>(StorageActivity.TAG_RESULT)
                            ?.let { folder ->
                                viewModel.connectStorage(folder)
                            }
                    }
                }

            LaunchedEffect(viewModel) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is AddRoomEffect.Error -> {
                            UiUtils.getSnackBar(requireActivity())
                                .setText(effect.message)
                                .show()
                        }
                    }
                }
            }

            NavHost(navController = navController, startDestination = Screens.Main.name) {
                composable(route = Screens.Main.name) {
                    MainScreen(
                        isEdit = isEdit,
                        isRoomTypeEditable = !isEdit && copyItems == null,
                        navController = navController,
                        viewState = viewState.value,
                        roomState = roomState.value,
                        saveData = viewModel::saveData,
                        imageCallBack = viewModel::setImageUri,
                        onBackPressed = ::dismiss,
                        onCreateNewFolder = viewModel::setCreateNewFolder,
                        onLocationClick = { navController.navigate("${Screens.Folder.name}/$it") },
                        onSetOwnerClick = { navController.navigate(Screens.ChangeOwner.name) },
                        create = { roomType, name, image, tags ->
                            if (isEdit) {
                                viewModel.edit(name, tags)
                            } else {
                                viewModel.createRoom(roomType, name, image, tags)
                            }
                        },
                        created = { id ->
                            requireActivity().supportFragmentManager.setFragmentResult(
                                TAG_RESULT,
                                bundleOf("id" to id, "type" to roomState.value.type)
                            )
                            dismiss()
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
                        updateState = viewModel::updateState
                    )
                }
                composable(Screens.Select.name) {
                    SelectRoomScreen(
                        currentType = roomState.value.type,
                        navController = navController,
                        select = { viewModel.setRoomType(it) }
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
                composable(Screens.ChangeOwner.name) {
                    val userListViewModel = RoomUserListViewModel(
                        roomId = room?.id.orEmpty(),
                        roomType = roomType,
                        roomOwnerId = roomState.value.owner.id,
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun MainScreen(
    isEdit: Boolean,
    isRoomTypeEditable: Boolean,
    navController: NavHostController,
    viewState: ViewState,
    roomState: AddRoomData,
    saveData: (String, List<ChipData>) -> Unit = { _, _ -> },
    create: (Int, String, Any?, List<String>) -> Unit = { _, _, _, _ -> },
    imageCallBack: (uri: Uri?) -> Unit = {},
    onBackPressed: () -> Unit = {},
    created: (String) -> Unit = {},
    onLocationClick: (String) -> Unit,
    onCreateNewFolder: (Boolean) -> Unit,
    onStorageConnect: (Boolean) -> Unit,
    onSetOwnerClick: () -> Unit,
    updateState: (AddRoomData) -> Unit = {}
) {
    val keyboardController = LocalFocusManager.current
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tags = remember(roomState.tags::toMutableStateList)
    val name = remember { mutableStateOf(roomState.name) }

    if (viewState is ViewState.Success) {
        if (viewState.id != null) {
            // Need to dialog
            scope.launch {
                created(viewState.id)
            }
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            ChooseImageBottomView(scope, modalBottomSheetState, roomState.imageUri) { uri ->
                imageCallBack(uri)
            }
        },
        sheetState = modalBottomSheetState,
        scrimColor = if (!isSystemInDarkTheme()) {
            ModalBottomSheetDefaults.scrimColor
        } else {
            MaterialTheme.colors.background.copy(alpha = 0.60f)
        }
    ) {
        AppScaffold(topBar = {
            AppTopBar(
                backListener = onBackPressed,
                title = if (isEdit)
                    stringResource(id = R.string.list_context_edit_room) else
                    stringResource(id = R.string.dialog_create_room),
                isClose = true,
                actions = {
                    TextButton(
                        onClick = {
                            keyboardController.clearFocus()
                            create(
                                roomState.type,
                                name.value,
                                roomState.imageUri,
                                tags.map(ChipData::text)
                            )
                        },
                        enabled = viewState !is ViewState.Loading && roomState.canApplyChanges
                    ) {
                        Text(
                            text = if (isEdit)
                                stringResource(id = lib.toolkit.base.R.string.common_done) else
                                stringResource(id = R.string.login_create_signin_create_button).capitalize(),
                        )
                    }
                }
            )
        }, useTablePaddings = false) {
            NestedColumn {
                if (viewState is ViewState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                AddRoomItem(
                    roomType = roomState.type,
                    clickable = isRoomTypeEditable
                ) {
                    saveData(name.value, tags)
                    navController.navigate(Screens.Select.name)
                }
                Row(
                    modifier = Modifier
                        .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_two_line_height))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    GlideImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .clickable(onClick = {
                                keyboardController.clearFocus()
                                scope.launch {
                                    modalBottomSheetState.show()
                                }
                            }),
                        model = if (roomState.imageUri is String && roomState.imageUri.isNotEmpty()) {
                            // TODO need interceptor
                            GlideUtils.getCorrectLoad(
                                roomState.imageUri,
                                AccountUtils.getToken(
                                    context,
                                    context.appComponent.accountOnline?.accountName.orEmpty()
                                ).orEmpty()
                            )
                        } else {
                            roomState.imageUri
                        },
                        contentDescription = null,
                        loading = placeholder(R.drawable.ic_empty_image),
                        failure = placeholder(R.drawable.ic_empty_image)
                    )
                    AppTextFieldListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(start = 16.dp),
                        state = name,
                        hint = stringResource(id = R.string.room_name_hint),
                        onValueChange = { updateState(roomState.copy(name = it)) }
                    )
                }
                ChipsTextField(
                    modifier = Modifier.padding(start = 16.dp),
                    label = stringResource(id = R.string.room_add_tag_hint),
                    chips = tags,
                    onChipAdd = { tag ->
                        val exists = tags.any { it.text == tag }
                        if (!exists) tags.add(ChipData(tag))
                    },
                    onChipDelete = { tags.remove(it) }
                )

                if (isEdit) {
                    AppArrowItem(
                        title = R.string.share_access_room_owner,
                        option = roomState.owner.displayName,
                        onClick = onSetOwnerClick
                    )
                }

                if (roomState.type == ApiContract.RoomType.PUBLIC_ROOM) {
                    ThirdPartyBlock(
                        isEdit = isEdit,
                        state = roomState.storageState,
                        roomName = name,
                        onLocationClick = { folderId ->
                            saveData.invoke(name.value, tags)
                            onLocationClick.invoke(folderId)
                        },
                        onCreateNewFolder = onCreateNewFolder,
                        onStorageConnect = onStorageConnect
                    )
                }


                if (roomState.type == ApiContract.RoomType.VIRTUAL_ROOM) {
                    VdrRoomBlock(state = roomState, updateState = updateState)
                }

                QuotaBlock(state = roomState, updateState = updateState)
            }
            if (viewState is ViewState.Error) {
                keyboardController.clearFocus(true)
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(
                        message = viewState.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}


@Composable
private fun ThirdPartyBlock(
    isEdit: Boolean,
    state: StorageState?,
    roomName: State<String>,
    onLocationClick: (String) -> Unit,
    onCreateNewFolder: (Boolean) -> Unit,
    onStorageConnect: (Boolean) -> Unit,
) {
    Column {
        val storageName = state?.providerKey?.let(StorageUtils::getStorageTitle)

        if (!isEdit) {
            AppSwitchItem(
                title = R.string.room_create_thirdparty_storage_title,
                checked = storageName != null,
                onCheck = onStorageConnect::invoke
            )
        }

        if (state != null && storageName != null) {
            AppArrowItem(
                title = stringResource(id = R.string.room_create_thirdparty_storage),
                option = stringResource(id = storageName),
                enabled = !isEdit,
                arrowVisible = !isEdit,
                onClick = { onStorageConnect.invoke(true) }
            )
            if (!isEdit) {
                AppArrowItem(
                    title = stringResource(id = R.string.room_create_thirdparty_location),
                    option = if (state.createAsNewFolder) {
                        state.location?.let { "$it${roomName.value}" } ?: "/${roomName.value}"
                    } else {
                        state.location
                            ?: stringResource(id = R.string.room_create_thirdparty_location_root)
                    },
                    onClick = { onLocationClick.invoke(state.id) }
                )
                AppSwitchItem(
                    title = R.string.room_create_thirdparty_new_folder,
                    checked = state.createAsNewFolder,
                    onCheck = onCreateNewFolder
                )
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.room_create_thirdparty_desc
                )
            }
        }
    }
}


//TODO Vdr content
@Composable
private fun VdrRoomBlock(state: AddRoomData, updateState: (AddRoomData) -> Unit) {
    AppSwitchItem(
        title = stringResource(R.string.rooms_vdr_indexing_title),
        checked = state.indexing,
        onCheck = { checked -> updateState(state.copy(indexing = checked)) }
    )
    Text(
        text = stringResource(R.string.rooms_vdr_indexing_desc),
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = R.string.file_lifetime_title,
        checked = state.lifetime != null,
        onCheck = { checked ->
            updateState(
                state.copy(
                    lifetime = if (checked)
                        Lifetime(value = Lifetime.DEFAULT_VALUE) else
                        null
                )
            )
        }
    )
    AnimatedVisibilityVerticalFade(visible = state.lifetime != null) {
        LifeTimeBlock(
            lifetime = state.lifetime,
            onChangeValue = {
                updateState(state.copy(lifetime = state.lifetime?.copy(value = it)))
            },
            onChangePeriod = {
                updateState(state.copy(lifetime = state.lifetime?.copy(period = it)))
            },
            onChangeAction = {
                updateState(state.copy(lifetime = state.lifetime?.copy(deletePermanently = it)))
            }
        )
    }
    Text(
        stringResource(R.string.file_lifetime_desc),
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = stringResource(R.string.rooms_info_file_rectrict),
        checked = state.denyDownload,
        singleLine = false,
        onCheck = { checked -> updateState(state.copy(denyDownload = checked)) }
    )
    Text(
        text = stringResource(R.string.rooms_vdr_file_restrict_desc),
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = stringResource(R.string.rooms_vdr_watermark_title),
        checked = state.watermark != null,
        onCheck = { checked -> updateState(state.copy(watermark = if (checked) Watermark() else null)) }
    )
    Text(
        text = stringResource(R.string.rooms_vdr_watermark_desc),
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    )
}

@Composable
private fun QuotaBlock(state: AddRoomData, updateState: (AddRoomData) -> Unit) {
    Column {
        AppSwitchItem(
            title = stringResource(R.string.rooms_vdr_storage_quota_title),
            checked = state.storageQuota != null,
            onCheck = { checked ->
                updateState(state.copy(storageQuota = if (checked) StorageQuota() else null))
            }
        )
        AnimatedVisibilityVerticalFade(visible = state.storageQuota != null) {
            Column {
                val quota = remember { mutableStateOf(state.storageQuota?.value.toString()) }

                AppTextField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    state = quota,
                    onValueChange = { value ->
                        if (!value.isDigitsOnly()) return@AppTextField
                        val digitValue = if (value.isEmpty()) 0 else value.toLong()
                        updateState(state.copy(storageQuota = state.storageQuota?.copy(value = digitValue)))
                        quota.value = digitValue.toString()
                    },
                    keyboardType = KeyboardType.Number,
                    label = R.string.rooms_vdr_size_quota,
                )
                AppListItem(
                    title = stringResource(R.string.rooms_vdr_measurement_unit),
                    endContent = {
                        val popupVisible = remember { mutableStateOf(false) }

                        DropdownMenuButton(
                            title = state.storageQuota?.unit?.let { stringResource(it.title) }.orEmpty(),
                            state = popupVisible,
                            items = {
                                SizeUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        title = stringResource(unit.title),
                                        selected = state.storageQuota?.unit == unit,
                                        onClick = {
                                            updateState(
                                                state.copy(
                                                    storageQuota = state.storageQuota?.copy(unit = unit)
                                                )
                                            )
                                            popupVisible.value = false
                                        }
                                    )
                                }
                            },
                            onDismiss = { popupVisible.value = false }
                        ) { popupVisible.value = true }
                    }
                )
            }
        }
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            text = stringResource(R.string.rooms_vdr_storage_quota_desc),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun LifeTimeBlock(
    lifetime: Lifetime?,
    onChangeValue: (Int) -> Unit,
    onChangePeriod: (Int) -> Unit,
    onChangeAction: (Boolean) -> Unit
) {
    val lifetimeValue = remember { mutableStateOf(lifetime?.value.toString()) }

    Column {
        AppTextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            state = lifetimeValue,
            onValueChange = { value ->
                if (!value.isDigitsOnly() || value.length > 3) return@AppTextField
                val digitValue = if (value.isEmpty()) 0 else value.toInt()
                onChangeValue(digitValue)
                lifetimeValue.value = digitValue.toString()
            },
            keyboardType = KeyboardType.Number,
            label = R.string.file_lifetime_hint,
        )
        AppListItem(
            title = stringResource(R.string.file_lifetime_time_period),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (lifetime?.period) {
                            1 -> R.string.file_lifetime_period_months
                            2 -> R.string.file_lifetime_period_years
                            else -> R.string.file_lifetime_period_days
                        }
                    ),
                    state = popupVisible,
                    items = {
                        DropdownMenuItem(
                            title = stringResource(R.string.file_lifetime_period_days),
                            selected = lifetime?.period == Lifetime.PERIOD_DAYS
                        ) {
                            onChangePeriod(Lifetime.PERIOD_DAYS)
                            popupVisible.value = false
                        }
                        DropdownMenuItem(
                            title = stringResource(R.string.file_lifetime_period_months),
                            selected = lifetime?.period == Lifetime.PERIOD_MONTHS
                        ) {
                            onChangePeriod(Lifetime.PERIOD_MONTHS)
                            popupVisible.value = false
                        }
                        DropdownMenuItem(
                            title = stringResource(R.string.file_lifetime_period_years),
                            selected = lifetime?.period == Lifetime.PERIOD_YEARS
                        ) {
                            onChangePeriod(Lifetime.PERIOD_YEARS)
                            popupVisible.value = false
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
        AppListItem(
            title = stringResource(R.string.file_lifetime_action),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (lifetime?.deletePermanently) {
                            true -> R.string.file_lifetime_action_delete
                            else -> R.string.file_lifetime_action_move_to_trash
                        }
                    ),
                    state = popupVisible,
                    onDismiss = { popupVisible.value = false },
                    items = {
                        DropdownMenuItem(
                            title = stringResource(R.string.file_lifetime_action_move_to_trash),
                            selected = lifetime?.deletePermanently != true
                        ) {
                            onChangeAction(false)
                            popupVisible.value = false
                        }
                        DropdownMenuItem(
                            title = stringResource(R.string.file_lifetime_action_delete),
                            selected = lifetime?.deletePermanently == true
                        ) {
                            onChangeAction(true)
                            popupVisible.value = false
                        }
                    }
                ) { popupVisible.value = true }
            }
        )
    }
}

@Preview
@Composable
private fun LifeTimeBlockPreview() {
    AppScaffold {
        LifeTimeBlock(Lifetime(), {}, {}) {

        }
    }
}

@Composable
private fun SelectRoomScreen(
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

@SuppressLint("MissingPermission")
@Composable
private fun ChooseImageBottomView(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    imageUri: Any? = null,
    uriCallback: (Uri?) -> Unit,
) {
    val context = LocalContext.current
    var photo: Uri? = null

    val photoLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success) {
                    scope.launch {
                        state.hide()
                        uriCallback.invoke(photo)
                    }
                }
            })

    val cameraPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                photo?.let { photoLauncher.launch(it) }
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = {
                scope.launch {
                    state.hide()
                    uriCallback.invoke(it)
                }
            })

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        Image(
            painter = painterResource(id = lib.toolkit.base.R.drawable.ic_bottom_divider),
            contentDescription = null,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        )
        AppArrowItem(
            title = stringResource(id = R.string.list_action_photo),
            startIcon = R.drawable.ic_list_action_photo,
            startIconTint = MaterialTheme.colors.onSurface,
            arrowVisible = false,
            dividerVisible = false,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            val tempPhoto =
                FileUtils.createFile(
                    File(context.cacheDir.absolutePath),
                    TimeUtils.fileTimeStamp,
                    "png"
                )
            photo = ContentResolverUtils.getFileUri(context, tempPhoto!!).also {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        AppArrowItem(
            title = stringResource(id = R.string.list_action_image_from_library),
            startIcon = lib.toolkit.base.R.drawable.ic_image,
            startIconTint = MaterialTheme.colors.onSurface,
            arrowVisible = false,
            dividerVisible = false
        ) {
            galleryLauncher.launch("image/*")
        }

        imageUri?.let {
            Divider()
            AppArrowItem(
                title = stringResource(id = R.string.list_action_delete_image),
                titleColor = MaterialTheme.colors.error,
                startIcon = R.drawable.ic_trash,
                startIconTint = MaterialTheme.colors.error,
                arrowVisible = false,
                dividerVisible = false
            ) {
                scope.launch {
                    state.hide()
                    uriCallback(null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun TextFieldPreview() {
    ManagerTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            ChipsTextField(
                label = "Add tag",
                chips = listOf(
                    ChipData("one"),
                    ChipData("two"),
                    ChipData("two"),
                    ChipData("three"),
                ),
                onChipAdd = {},
                onChipDelete = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    ManagerTheme {
        MainScreen(
            isEdit = true,
            isRoomTypeEditable = false,
            navController = rememberNavController(),
            viewState = ViewState.None,
            roomState = AddRoomData(
                name = "name",
                owner = User(displayName = "Owner Name"),
                type = ApiContract.RoomType.PUBLIC_ROOM,
                storageState = StorageState(
                    id = "",
                    providerKey = ApiContract.Storage.DROPBOX,
                    location = null,
                    createAsNewFolder = false
                )
            ),
            onCreateNewFolder = {},
            onStorageConnect = { _ -> },
            onLocationClick = {},
            onSetOwnerClick = {}
        )
    }
}

@Preview
@Composable
private fun SelectScreenPreview() {
    ManagerTheme {
        SelectRoomScreen(2, navController = rememberNavController()) {}
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SelectImagePreview() {
    ManagerTheme {
        Surface {
            ChooseImageBottomView(
                scope = rememberCoroutineScope(),
                state = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
                imageUri = null,
                uriCallback = {}
            )
        }
    }
}
