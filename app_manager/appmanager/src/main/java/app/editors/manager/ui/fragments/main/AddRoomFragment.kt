@file:OptIn(ExperimentalGlideComposeApi::class)

package app.editors.manager.ui.fragments.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.ui.activities.main.StorageActivity
import app.editors.manager.ui.dialogs.AddRoomItem
import app.editors.manager.ui.dialogs.fragments.AddRoomDialog
import app.editors.manager.viewModels.main.AddRoomData
import app.editors.manager.viewModels.main.AddRoomViewModel
import app.editors.manager.viewModels.main.StorageState
import app.editors.manager.viewModels.main.ViewState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.ChipData
import lib.compose.ui.views.ChipsTextField
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.fragments.base.BaseFragment
import java.io.File

private enum class Navigation(val route: String) {
    Main("MainScreen"), Select("SelectScreen"), Folder("FolderScreen")
}

class AddRoomFragment : BaseFragment() {

    companion object {

        const val TAG_ROOM_TYPE = "room_type"
        const val TAG_ROOM_INFO = "room_info"
        const val TAG_COPY = "files_copy"
        const val TAG_RESULT = "add_room_result"

        val TAG: String = AddRoomFragment::class.java.simpleName

        fun newInstance(roomType: Int, room: Item?, isCopy: Boolean = false) =
            AddRoomFragment().putArgs(TAG_ROOM_TYPE to roomType, TAG_ROOM_INFO to room, TAG_COPY to isCopy)

        fun show(
            fragmentManager: FragmentManager,
            roomType: Int,
            roomInfo: Item? = null,
            isCopy: Boolean = false
        ) {
            FragmentUtils.showFragment(
                fragmentManager = fragmentManager,
                fragment = newInstance(roomType, roomInfo, isCopy),
                frameId = android.R.id.content,
                tag = TAG,
                isAdd = true
            )
        }
    }

    private lateinit var navController: NavHostController

    private val isEdit: Boolean
        get() = arguments?.getSerializableExt<Item>(TAG_ROOM_INFO) != null && arguments?.getBoolean(TAG_COPY) == false

    override fun onBackPressed(): Boolean {
        if (!navController.popBackStack()) {
            if (parentFragment is AddRoomDialog) {
                (parentFragment as AddRoomDialog).dismiss()
            }
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            navController = rememberNavController()
            val roomType = remember { arguments?.getInt(TAG_ROOM_TYPE) }
            val viewModel = viewModel {
                AddRoomViewModel(
                    context = requireActivity().application,
                    roomProvider = requireContext().roomProvider,
                    roomInfo = arguments?.getSerializableExt(TAG_ROOM_INFO),
                    isCopy = arguments?.getBoolean(TAG_COPY) ?: false
                )
            }
            val roomState = viewModel.roomState.collectAsState()
            val viewState = viewModel.viewState.collectAsState()
            val storageActivityLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        it.data?.getSerializableExt<CloudFolder>(StorageActivity.TAG_RESULT)?.let { folder ->
                            viewModel.connectStorage(folder)
                        }
                    }
                }

            ManagerTheme {
                NavHost(navController = navController, startDestination = "${Navigation.Main.route}/{roomType}") {
                    composable(
                        "${Navigation.Main.route}/{roomType}",
                        arguments = listOf(navArgument("roomType") { type = NavType.IntType })
                    ) {
                        val type = it.arguments?.getInt("roomType", roomType ?: -1) ?: -1
                        viewModel.setType(type)
                        MainScreen(
                            isEdit = isEdit,
                            navController = navController,
                            viewState = viewState.value,
                            roomState = roomState.value,
                            saveData = viewModel::saveData,
                            imageCallBack = viewModel::setImageUri,
                            onBackPressed = ::onBackPressed,
                            onCreateNewFolder = viewModel::setCreateNewFolder,
                            onLocationClick = { navController.navigate("${Navigation.Folder.route}/$it") },
                            create = { type1, name, image, tags ->
                                if (isEdit) {
                                    viewModel.edit(name, tags)
                                } else {
                                    viewModel.createRoom(type1, name, image, tags)
                                }
                            },
                            created = { id ->
                                requireActivity().supportFragmentManager.setFragmentResult(
                                    TAG_RESULT,
                                    Bundle(1).apply { putString("id", id) })
                                onBackPressed()
                            },
                            onStorageConnect = { isConnect, roomName ->
                                if (isConnect) {
                                    storageActivityLauncher.launch(
                                        StorageActivity.getIntent(
                                            context = requireContext(),
                                            isMySection = true,
                                            title = roomName,
                                            isRoomStorage = true,
                                            providerKey = null,
                                            providerId = null
                                        )
                                    )
                                } else {
                                    viewModel.disconnectStorage()
                                }
                            }
                        )
                    }
                    composable(
                        route = "${Navigation.Select.route}/{roomType}",
                        arguments = listOf(navArgument("roomType") { type = NavType.IntType })
                    ) {
                        val type = it.arguments?.getInt("roomType") ?: -1
                        SelectRoomScreen(type, navController)
                    }
                    composable(
                        route = "${Navigation.Folder.route}/{folderId}",
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
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainScreen(
    isEdit: Boolean,
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
    onStorageConnect: (Boolean, String) -> Unit
) {
    val keyboardController = LocalFocusManager.current
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tags = remember(roomState.tags::toMutableStateList)
    val name = remember { mutableStateOf(roomState.name) }
    val roomInfo = RoomUtils.getRoomInfo(roomState.type)

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
                            create(roomState.type, name.value, roomState.imageUri, tags.map(ChipData::text))
                        },
                        enabled = viewState !is ViewState.Loading
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
            Column {
                if (viewState is ViewState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                AddRoomItem(
                    isClickable = !isEdit,
                    icon = roomInfo.icon,
                    title = roomInfo.title,
                    description = roomInfo.description
                ) {
                    saveData(name.value, tags)
                    navController.navigate("${Navigation.Select.route}/${roomState.type}")
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
                        hint = stringResource(id = R.string.room_name_hint)
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
fun ThirdPartyBlock(
    isEdit: Boolean,
    state: StorageState?,
    roomName: State<String>,
    onLocationClick: (String) -> Unit,
    onCreateNewFolder: (Boolean) -> Unit,
    onStorageConnect: (Boolean, String) -> Unit
) {
    Column {
        val storageName = state?.providerKey?.let(StorageUtils::getStorageTitle)

        if (!isEdit) {
            AppSwitchItem(
                title = R.string.room_create_thirdparty_storage_title,
                checked = storageName != null,
                onCheck = { onStorageConnect.invoke(it, roomName.value) }
            )
        }

        if (state != null && storageName != null) {
            AppArrowItem(
                title = stringResource(id = R.string.room_create_thirdparty_storage),
                option = stringResource(id = storageName),
                enabled = !isEdit,
                arrowVisible = !isEdit,
                onClick = { onStorageConnect.invoke(true, roomName.value) }
            )
            if (!isEdit) {
                AppArrowItem(
                    title = stringResource(id = R.string.room_create_thirdparty_location),
                    option = if (state.createAsNewFolder) {
                        state.location?.let { "$it${roomName.value}" } ?: "/${roomName.value}"
                    } else {
                        state.location ?: stringResource(id = R.string.room_create_thirdparty_location_root)
                    },
                    enabled = !isEdit,
                    arrowVisible = !isEdit,
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

@Composable
private fun SelectRoomScreen(type: Int, navController: NavHostController) {
    AppScaffold(topBar = {
        AppTopBar(
            title = stringResource(id = R.string.rooms_choose_room),
            backListener = navController::popBackStack
        )
    }, useTablePaddings = false) {
        Column {
            AddRoomItem(
                icon = R.drawable.ic_collaboration_room,
                title = R.string.rooms_add_collaboration,
                description = R.string.rooms_add_collaboration_des,
                isSelect = type == 2
            ) {
                navController.navigate("${Navigation.Main.route}/2") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
            AddRoomItem(
                icon = R.drawable.ic_public_room,
                title = R.string.rooms_add_public_room,
                description = R.string.rooms_add_public_room_des,
                isSelect = type == 6
            ) {
                navController.navigate("${Navigation.Main.route}/6") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
            AddRoomItem(
                icon = R.drawable.ic_custom_room,
                title = R.string.rooms_add_custom,
                description = R.string.rooms_add_custom_des,
                isSelect = type == 5
            ) {
                navController.navigate("${Navigation.Main.route}/5") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChooseImageBottomView(
    scope: CoroutineScope,
    state: ModalBottomSheetState,
    imageUri: Any? = null,
    uriCallback: (Uri?) -> Unit
) {
    val context = LocalContext.current
    var photo: Uri? = null

    val photoLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture(), onResult = { success ->
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
                photoLauncher.launch(photo)
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(), onResult = {
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
                FileUtils.createFile(File(context.cacheDir.absolutePath), TimeUtils.fileTimeStamp, "png")
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
                title = stringResource(id = R.string.list_action_delete_link),
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
            navController = rememberNavController(),
            viewState = ViewState.None,
            roomState = AddRoomData(
                name = "name",
                type = ApiContract.RoomType.PUBLIC_ROOM,
                storageState = StorageState(
                    id = "",
                    providerKey = ApiContract.Storage.DROPBOX,
                    location = null,
                    createAsNewFolder = false
                )
            ),
            onCreateNewFolder = {},
            onStorageConnect = { _, _ -> },
            onLocationClick = {}
        )
    }
}

@Preview
@Composable
private fun SelectScreenPreview() {
    ManagerTheme {
        SelectRoomScreen(2, navController = rememberNavController())
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
