package app.editors.manager.ui.compose.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareType
import app.documents.core.network.share.models.SharedTo
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.titleWithCount
import app.editors.manager.managers.utils.toUi
import app.editors.manager.ui.fragments.share.InviteAccessScreen
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.share.link.ShareUsersList
import app.editors.manager.ui.fragments.share.link.SharedLinkItem
import app.editors.manager.ui.fragments.share.link.SharedLinkSettingsScreen
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.link.ShareSettingsEffect
import app.editors.manager.viewModels.link.ShareSettingsState
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import app.editors.manager.viewModels.link.SharedLinkSettingsViewModel
import app.editors.manager.viewModels.main.ShareAccessViewModel
import app.editors.manager.viewModels.main.ShareUserListViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppCircularProgress
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.tools.FileExtensions
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.getJsonString
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Route(val name: String) {

    data object SettingsScreen : Route("settings")
    data object LinkSettingsScreen : Route("link_settings")
    data object AddUserScreen : Route("add_user_screen")
}

@Composable
fun ShareDocSpaceScreen(
    roomProvider: RoomProvider,
    itemId: String,
    fileExtension: FileExtensions?, // null for folders
    useTabletPadding: Boolean,
    onSendLink: (String) -> Unit,
    onClose: () -> Unit,
) {
    val navController = rememberNavController()
    val isFolder = fileExtension == null

    val viewModel = viewModel {
        ShareSettingsViewModel(
            roomProvider = roomProvider,
            itemId = itemId,
            isFolder = isFolder
        )
    }

    navController.addOnDestinationChangedListener { _, destination, _ ->
        if (destination.route == Route.SettingsScreen.name) {
            viewModel.fetchData()
        }
    }

    BackHandler(onBack = onClose)

    NavHost(
        modifier = Modifier.background(MaterialTheme.colors.background),
        navController = navController,
        startDestination = Route.SettingsScreen.name
    ) {
        composable(Route.SettingsScreen.name) {
            MainScreen(
                viewModel = viewModel,
                useTabletPaddings = useTabletPadding,
                isFolder = isFolder,
                onShare = onSendLink,
                onBack = onClose,
                onAddUsers = {
                    navController.navigate(Route.AddUserScreen.name)
                },
                onLinkClick = { link ->
                    val json =
                        URLEncoder.encode(Json.encodeToString(link), Charsets.UTF_8.toString())
                    navController.navigate(
                        "${Route.LinkSettingsScreen.name}?" +
                                "link=$json&" +
                                "expired=${link.sharedTo.expirationDate}"
                    )
                }
            )
        }
        composable(
            route = "${Route.LinkSettingsScreen.name}?link={link}&expired={expired}",
            arguments = listOf(
                navArgument("link") {
                    type = NavType.StringType
                },
                navArgument("expired") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val json = URLDecoder.decode(it.arguments?.getString("link"), Charsets.UTF_8.toString())
            SharedLinkSettingsScreen(
                viewModel = viewModel {
                    SharedLinkSettingsViewModel(
                        externalLink = Json.decodeFromString<ExternalLink>(json),
                        expired = it.arguments?.getString("expired"),
                        roomProvider = viewModel.roomProvider,
                        itemId = viewModel.itemId,
                        isFolder = isFolder
                    )
                },
                accessList = ManagerUiUtils.getItemAccessList(
                    extension = fileExtension,
                    forLink = true
                ),
                useTabletPadding = useTabletPadding,
                onBack = navController::popBackStack,
            )
        }
        composable(Route.AddUserScreen.name) {
            val userListViewModel = viewModel {
                ShareUserListViewModel(
                    roomProvider = roomProvider,
                    itemId = itemId,
                    isFolder = isFolder,
                    accessList = ManagerUiUtils.getItemAccessList(extension = fileExtension)
                )
            }
            UserListScreen(
                title = R.string.setting_select_members_title,
                closeable = false,
                viewModel = userListViewModel,
                onClick = { userListViewModel.toggleSelect(it.id) },
                onBack = navController::popBackStack,
            ) { size, access ->
                UserListBottomContent(
                    nextButtonTitle = lib.toolkit.base.R.string.common_next,
                    count = size,
                    access = access,
                    accessList = remember {
                        ManagerUiUtils.getItemAccessList(extension = fileExtension)
                            .map { it.toUi(true) }
                    },
                    onAccess = userListViewModel::setAccess,
                    onDelete = userListViewModel::onDelete
                ) {
                    val users =
                        Json.encodeToString(userListViewModel.getSelectedUsers().ifEmpty { null })
                    val groups =
                        Json.encodeToString(userListViewModel.getSelectedGroups().ifEmpty { null })
                    navController.navigate(
                        Route.AddUserScreen.name +
                                "?emails=null&" +
                                "users=${URLEncoder.encode(users, Charsets.UTF_8.toString())}&" +
                                "groups=${URLEncoder.encode(groups, Charsets.UTF_8.toString())}&" +
                                "access=${access.code}"
                    )
                }
            }
        }
        composable(
            route = "${Route.AddUserScreen.name}?" +
                    "emails={emails}&" +
                    "users={users}&" +
                    "groups={groups}&" +
                    "access={access}",
            arguments = listOf(
                navArgument("emails") { type = NavType.StringType; nullable = true },
                navArgument("users") { type = NavType.StringType; nullable = true },
                navArgument("groups") { type = NavType.StringType; nullable = true },
                navArgument("access") { type = NavType.IntType; defaultValue = 2 }
            )
        ) {
            val shareAccessViewModel = viewModel {
                ShareAccessViewModel(
                    itemId = itemId,
                    roomProvider = roomProvider,
                    isFolder = isFolder,
                    access = Access.get(it.arguments?.getInt("access")),
                    users = it.arguments?.getJsonString<List<User>>("users", true).orEmpty(),
                    groups = it.arguments?.getJsonString<List<Group>>("groups", true).orEmpty(),
                    emails = it.arguments?.getJsonString<List<String>>("emails").orEmpty(),
                )
            }
            InviteAccessScreen(
                accessList = remember {
                    ManagerUiUtils.getItemAccessList(extension = fileExtension)
                        .map { access -> access.toUi(true) }
                },
                viewModel = shareAccessViewModel,
                onBack = navController::popBackStack,
                onSuccess = {
                    navController.navigate(Route.SettingsScreen.name) {
                        popUpTo(Route.SettingsScreen.name) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun MainScreen(
    viewModel: ShareSettingsViewModel,
    isFolder: Boolean,
    useTabletPaddings: Boolean,
    onShare: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var isCreateLoading by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShareSettingsEffect.Copy -> {
                    KeyboardUtils.setDataToClipboard(context, effect.link)
                    scaffoldState.snackbarHostState
                        .showSnackbar(context.getString(R.string.rooms_info_create_link_complete))
                }

                is ShareSettingsEffect.Error -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        effect.code?.let { code ->
                            context.getString(R.string.errors_client_error) + code
                        } ?: context.getString(R.string.errors_unknown_error)
                    )
                }

                is ShareSettingsEffect.OnCreate -> {
                    isCreateLoading = effect.loading
                }
            }
        }
    }

    ShareSettingsScreen(
        scaffoldState = scaffoldState,
        state = state,
        isFolder = isFolder,
        onBack = onBack,
        useTabletPaddings = useTabletPaddings,
        isCreateLoading = isCreateLoading,
        onCreate = viewModel::create,
        onShareClick = onShare,
        onLinkClick = onLinkClick,
        onAddUsers = onAddUsers
    )
}

@Composable
private fun ShareSettingsScreen(
    scaffoldState: ScaffoldState,
    isCreateLoading: Boolean,
    isFolder: Boolean,
    state: ShareSettingsState,
    useTabletPaddings: Boolean,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(
        scaffoldState = scaffoldState,
        useTablePaddings = useTabletPaddings,
        topBar = {
            AppTopBar(
                title = R.string.share_title_main,
                backListener = onBack,
                actions = {
                    TopAppBarAction(
                        icon = R.drawable.ic_add_users,
                        onClick = onAddUsers
                    )
                }
            )
        }
    ) {
        when (state) {
            is ShareSettingsState.Loading -> LoadingPlaceholder()
            is ShareSettingsState.Success -> {
                LazyColumn {
                    item {
                        Row {
                            AppHeaderItem(
                                modifier = Modifier.weight(1f),
                                title = stringResource(id = R.string.rooms_share_shared_links)
                            )
                            if (state.links.isNotEmpty()) {
                                IconButton(onClick = onCreate) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_default_add),
                                        tint = MaterialTheme.colors.primary,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                    if (state.links.isNotEmpty()) {
                        items(state.links, key = { it.sharedTo.id }) { link ->
                            SharedLinkItem(
                                modifier = Modifier.animateItem(),
                                access = link.access,
                                internal = link.sharedTo.internal == true,
                                expirationDate = link.sharedTo.expirationDate,
                                isExpired = link.sharedTo.isExpired,
                                onShareClick = { onShareClick.invoke(link.sharedTo.shareLink) },
                                onClick = { onLinkClick(link) }
                            )
                        }
                    } else if (!isCreateLoading) {
                        item {
                            AppTextButton(
                                modifier = Modifier.padding(start = 8.dp),
                                title = R.string.rooms_info_create_link,
                                onClick = onCreate
                            )
                        }
                    }
                    if (isCreateLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height))
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AppCircularProgress()
                            }
                        }
                    }
                    item {
                        AppDescriptionItem(
                            modifier = Modifier.padding(top = 8.dp),
                            text = if (!isFolder) {
                                R.string.rooms_share_shared_desc
                            } else {
                                R.string.rooms_share_shared_folder_desc
                            }
                        )
                    }
                    item {
                        val groupedShareList = state.users.groupBy { it.itemAccessType }
                        val context = LocalContext.current
                        val view = LocalView.current
                        val portal = remember {
                            context.accountOnline?.portal?.url?.takeIf { !view.isInEditMode }
                        }
                        groupedShareList.forEach { (shareType, shareList) ->
                            ShareUsersList(
                                canBeCollapsed = shareType != ShareType.Owner,
                                isRoom = false,
                                title = shareType.titleWithCount,
                                portal = portal.orEmpty(),
                                shareList = shareList,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ShareSettingsScreenPreview() {
    val link = ExternalLink(
        access = 2,
        isLocked = false,
        isOwner = false,
        canEditAccess = false,
        sharedTo = ExternalLinkSharedTo(
            id = "1",
            title = "",
            shareLink = "",
            linkType = 2,
            internal = true,
            denyDownload = false,
            isExpired = false,
            primary = true,
            requestToken = "",
            password = "",
            expirationDate = null
        )
    )

    ManagerTheme {
        ShareSettingsScreen(
            scaffoldState = rememberScaffoldState(),
            state = ShareSettingsState.Success(
                listOf(
                    link.copy(access = 1),
                    link.copy(sharedTo = link.sharedTo.copy(expirationDate = null, id = "2")),
                    link.copy(
                        sharedTo = link.sharedTo.copy(
                            isExpired = true,
                            internal = false,
                            id = "3"
                        )
                    )
                ),
                listOf(
                    Share(
                        _access = 1,
                        isOwner = true,
                        sharedTo = SharedTo(
                            userName = "Username"
                        )
                    ),
                    Share(
                        _access = 10,
                        sharedTo = SharedTo(
                            userName = "Group name"
                        ),
                        subjectType = 2
                    ),
                    Share(
                        _access = 5,
                        sharedTo = SharedTo(
                            userName = "Username"
                        )
                    ),
                )
            ),
            isFolder = true,
            useTabletPaddings = false,
            isCreateLoading = false,
            onBack = {},
            onCreate = {},
            onShareClick = {},
            onAddUsers = {},
            onLinkClick = {}
        )
    }
}