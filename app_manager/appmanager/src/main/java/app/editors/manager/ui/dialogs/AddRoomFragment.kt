package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.viewModels.main.AddRoomData
import app.editors.manager.viewModels.main.AddRoomViewModel
import app.editors.manager.viewModels.main.AddRoomViewModelFactory
import app.editors.manager.viewModels.main.ViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTopAppBar
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.ChipData
import lib.compose.ui.views.RoomChip
import lib.editors.gbase.ui.views.compose.link.clickable
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.fragments.base.BaseFragment
import java.io.File

private enum class Navigation(val route: String) {
    Main("MainScreen"), Select("SelectScreen")
}

class AddRoomFragment : BaseFragment() {

    companion object {

        private const val TAG_ROOM_INFO = "room_info"
        const val TAG_RESULT = "add_room_result"

        val TAG: String = AddRoomFragment::class.java.simpleName
        fun newInstance(roomType: Int) = AddRoomFragment().putArgs(TAG_ROOM_INFO to roomType)

        fun show(fragmentManager: FragmentManager, roomType: Int) {
            FragmentUtils.showFragment(
                fragmentManager = fragmentManager,
                fragment = newInstance(roomType),
                frameId = android.R.id.content,
                tag = TAG, isAdd = true
            )
        }
    }

    private lateinit var navController: NavHostController

    override fun onBackPressed(): Boolean {
        if (!navController.popBackStack()) {
            setFragmentResult(TAG_RESULT, Bundle())
            FragmentUtils.removeFragment(parentFragmentManager, this)
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

            val roomType = remember {
                arguments?.getInt(TAG_ROOM_INFO)
            }

            val viewModel =
                viewModel<AddRoomViewModel>(
                    factory = AddRoomViewModelFactory(
                        application = requireActivity().application,
                        roomProvider = requireContext().roomProvider
                    )
                )
            val roomState = viewModel.roomState.collectAsState()
            val viewState = viewModel.viewState.collectAsState()

            ManagerTheme {
                NavHost(navController = navController, startDestination = "${Navigation.Main.route}/{roomType}") {
                    composable(
                        "${Navigation.Main.route}/{roomType}",
                        arguments = listOf(navArgument("roomType") { type = NavType.IntType })
                    ) {
                        val type = it.arguments?.getInt("roomType", roomType ?: -1) ?: -1
                        viewModel.setType(type)
                        MainScreen(navController, viewState.value, roomState.value, { name, tags ->
                            viewModel.saveData(name, tags)
                        }, { type1, name, image, tags ->
                            viewModel.createRoom(type1, name, image, tags)
                        }, { imageUri ->
                            viewModel.setImageUri(imageUri)
                        }, ::onBackPressed)
                    }
                    composable(
                        "${Navigation.Select.route}/{roomType}",
                        arguments = listOf(navArgument("roomType") { type = NavType.IntType })
                    ) {
                        val type = it.arguments?.getInt("roomType") ?: -1
                        SelectRoomScreen(type, navController)
                    }
                }

            }
        }
    }


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainScreen(
    navController: NavHostController,
    viewState: ViewState,
    roomState: AddRoomData,
    saveData: (String, List<ChipData>) -> Unit = {  _, _ -> },
    create: (Int, String, Uri?, List<ChipData>) -> Unit = { _, _, _, _ -> },
    imageCallBack: (uri: Uri?) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val chipDataSnapshotStateList = remember {
        mutableStateListOf<ChipData>().apply { addAll(roomState.tags) }
    }

    val name = remember {
        mutableStateOf(roomState.name)
    }

    val roomInfo = RoomUtils.getRoomInfo(roomState.type)

    if (viewState is ViewState.Success) {
        onBackPressed()
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
                title = stringResource(id = R.string.dialog_create_room),
                isClose = true,
                actions = {
                    TextButton(onClick = { onBackPressed() }) {
                        Text(
                            text = stringResource(id = R.string.login_create_signin_create_button),
                            modifier = Modifier.clickable {
                                create(roomState.type, name.value, roomState.imageUri, chipDataSnapshotStateList)
                            })
                    }
                })
        }) {
            Column {
                if (viewState is ViewState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(8.dp))
                AddRoomItem(icon = roomInfo.icon, title = roomInfo.title, description = roomInfo.description) {
                    saveData(name.value, chipDataSnapshotStateList)
                    navController.navigate("${Navigation.Select.route}/${roomState.type}")
                }
                Row(
                    modifier = Modifier
                        .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_two_line_height))
                        .padding(horizontal = 16.dp)
                ) {
                    if (roomState.roomImage != null) {
                        Image(
                            bitmap = roomState.roomImage,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(40.dp)
                                .clickable(noRipple = false, onClick = {
                                    scope.launch {
                                        modalBottomSheetState.show()
                                    }
                                })
                        )
                    } else {
                        Image(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_empty_image),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(40.dp)
                                .clickable(noRipple = false, onClick = {
                                    scope.launch {
                                        modalBottomSheetState.show()
                                    }
                                })
                        )
                    }
                    AppTextField(
                        state = name,
                        hint = stringResource(id = R.string.room_name_hint),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                TextFieldWithChips(list = chipDataSnapshotStateList, onChipCreated = {
                    chipDataSnapshotStateList.add(it)
                }, chip = { data: ChipData, index: Int ->
                    RoomChip(data) {
                        chipDataSnapshotStateList.removeAt(index)
                    }
                })
            }
            if (viewState is ViewState.Error) {
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
private fun SelectRoomScreen(type: Int, navController: NavHostController) {
    AppScaffold(topBar = {
        AppTopBar(backListener = {
            navController.popBackStack()
        }, title = stringResource(id = R.string.rooms_choose_room), isClose = false)
    }) {
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
    imageUri: Uri? = null,
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
            photo = ContentResolverUtils.getFileUri(context, tempPhoto!!).also { uri ->
                photoLauncher.launch(uri)
            }
        }
        AppArrowItem(
            title = stringResource(id = R.string.list_action_image_from_library),
            startIcon = lib.editors.gbase.R.drawable.ic_image,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TextFieldWithChips(
    list: List<ChipData> = emptyList(),
    onChipCreated: (ChipData) -> Unit,
    chip: @Composable (data: ChipData, index: Int) -> Unit
) {

    val text = remember {
        mutableStateOf("")
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .drawWithContent {
                drawContent()
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        list.forEachIndexed { index, item ->
            key(item.id) {
                chip(item, index)
            }
        }
        Box(
            modifier = Modifier
                .height(54.dp)
                // This minimum width that TextField can have
                // if remaining space in same row is smaller it's moved to next line
                .widthIn(min = 80.dp)
                // TextField can grow as big as Composable width
                .weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            AppTextField(state = text, singleLine = true, onDone = {
                if (text.value.isNotEmpty()) {
                    onChipCreated(ChipData(text.value))
                    text.value = ""
                }
            }, focusManager = LocalFocusManager.current)
        }

    }

}

@Preview
@Composable
private fun TextFieldPreview() {
    val chipDataSnapshotStateList = remember {
        mutableStateListOf(
            ChipData("Hello"),
            ChipData("Hello"),
            ChipData("Hello"),
            ChipData("Hello"),
        )
    }
    ManagerTheme {
        TextFieldWithChips(list = chipDataSnapshotStateList, onChipCreated = {}) { _: ChipData, _: Int ->

        }
    }
}

@Preview
@Composable
private fun MainScreen() {
    ManagerTheme {
        MainScreen(navController = rememberNavController(), viewState = ViewState.None, roomState = AddRoomData(2))
    }
}

@Preview
@Composable
private fun SelectScreen() {
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
                uriCallback = {})
        }
    }
}
