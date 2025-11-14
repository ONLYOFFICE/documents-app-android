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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.manager.models.explorer.AccessTarget
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.ShareEntity
import app.documents.core.network.share.models.ShareType
import app.documents.core.network.share.models.SharedTo
import app.documents.core.providers.RoomProvider
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.managers.utils.titleWithCount
import app.editors.manager.managers.utils.toUi
import app.editors.manager.ui.fragments.share.InviteAccessScreen
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.ui.fragments.share.link.ChangeUserAccessScreen
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.AppCircularProgress
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.KeyboardUtils
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen {

    @Serializable
    data object Main : Screen()

    @Serializable
    data class LinkSettings(
        val link: String?,
        val expirationDate: String?
    ) : Screen()

    @Serializable
    data object AddUser : Screen()

    @Serializable
    data class AddUserAccess(
        val users: String?,
        val groups: String?,
        val access: Int
    ) : Screen()

    @Serializable
    data class ChangeAccess(
        val id: String,
        val access: Int,
        val removable: Boolean,
        val isOwnerOrAdmin: Boolean
    ) : Screen()
}

@Composable
fun ShareDocSpaceScreen(
    roomProvider: RoomProvider,
    shareData: ShareData,
    useTabletPadding: Boolean,
    onSendLink: (String) -> Unit,
    onClose: () -> Unit,
) {
    val navController = rememberNavController()

    val viewModel = viewModel {
        ShareSettingsViewModel(
            roomProvider = roomProvider,
            shareData = shareData
        )
    }

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.hasRoute(Screen.Main::class)) {
                viewModel.fetchData()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ShareSettingsEffect.Access -> {
                    navController.navigate(Screen.Main) {
                        popUpTo<Screen.Main> {
                            inclusive = true
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    BackHandler(onBack = onClose)

    NavHost(
        modifier = Modifier.background(MaterialTheme.colors.background),
        navController = navController,
        startDestination = Screen.Main
    ) {
        composable<Screen.Main> {
            MainScreen(
                viewModel = viewModel,
                useTabletPaddings = useTabletPadding,
                onShareLink = onSendLink,
                onBack = onClose,
                onAddUsers = { navController.navigate(Screen.AddUser) },
                descriptionText = if (!shareData.isFolder) {
                    stringResource(R.string.rooms_share_shared_desc)
                } else {
                    stringResource(R.string.rooms_share_shared_folder_desc)
                },
                onLinkClick = { link ->
                    val json =
                        URLEncoder.encode(Json.encodeToString(link), Charsets.UTF_8.toString())
                    navController.navigate(Screen.LinkSettings(json, link.sharedTo.expirationDate))
                },
                onChangeAccess = { share ->
                    navController.navigate(
                        Screen.ChangeAccess(
                            id = share.sharedTo.id,
                            access = share.access.code,
                            removable = true,
                            isOwnerOrAdmin = share.isOwnerOrAdmin
                        )
                    )
                }
            )
        }
        composable<Screen.LinkSettings> { backstackEntry ->
            val data = backstackEntry.toRoute<Screen.LinkSettings>()
            val json = URLDecoder.decode(data.link, Charsets.UTF_8.toString())
            SharedLinkSettingsScreen(
                viewModel = viewModel {
                    SharedLinkSettingsViewModel(
                        externalLink = Json.decodeFromString<ExternalLink>(json),
                        expired = data.expirationDate,
                        roomProvider = roomProvider,
                        shareData = shareData
                    )
                },
                accessList = shareData.getAccessList(AccessTarget.ExternalLink),
                useTabletPadding = useTabletPadding,
                onBack = navController::popBackStack,
            )
        }
        composable<Screen.AddUser> {
            val userListViewModel = viewModel {
                ShareUserListViewModel(
                    roomProvider = roomProvider,
                    shareData = shareData,
                    currentAccess = shareData
                        .getAccessList(AccessTarget.User)
                        .last { access -> access.access !is Access.Restrict }
                        .access
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
                    access = access.toUi(true),
                    accessList = shareData.getAccessList(AccessTarget.User),
                    onAccess = userListViewModel::setAccess,
                    onDelete = userListViewModel::onDelete
                ) {
                    val users = userListViewModel.getSelectedUsers().takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) }
                    val groups = userListViewModel.getSelectedGroups().takeIf { it.isNotEmpty() }?.let { Json.encodeToString(it) }
                    navController.navigate(
                        Screen.AddUserAccess(
                            users?.let { URLEncoder.encode(it, Charsets.UTF_8.toString()) },
                            groups?.let { URLEncoder.encode(it, Charsets.UTF_8.toString()) },
                            access.code
                        )
                    )
                }
            }
        }
        composable<Screen.AddUserAccess> { backstackEntry ->
            val data = backstackEntry.toRoute<Screen.AddUserAccess>()
            val decodedUsers = data.users?.let { URLDecoder.decode(it, Charsets.UTF_8.toString()) }
            val decodedGroups = data.groups?.let { URLDecoder.decode(it, Charsets.UTF_8.toString()) }
            val shareAccessViewModel = viewModel {
                ShareAccessViewModel(
                    roomProvider = roomProvider,
                    shareData = shareData,
                    access = Access.get(data.access),
                    users = decodedUsers?.let { Json.decodeFromString<List<User>>(it) }.orEmpty(),
                    groups = decodedGroups?.let { Json.decodeFromString<List<Group>>(it) }.orEmpty(),
                    emails = emptyList(),
                )
            }
            InviteAccessScreen(
                accessList = shareData.getAccessList(AccessTarget.User),
                viewModel = shareAccessViewModel,
                onBack = navController::popBackStack,
                onSuccess = {
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.Main) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Screen.ChangeAccess> { backStackEntry ->
            val data = backStackEntry.toRoute<Screen.ChangeAccess>()

            ChangeUserAccessScreen(
                accessList = shareData.getAccessList(AccessTarget.User, true),
                onBack = navController::popBackStackWhenResumed,
                currentAccess = Access.get(data.access),
                onChangeAccess = { newAccess ->
                    viewModel.setUserAccess(
                        data.id,
                        newAccess
                    )
                },
            )
        }
    }
}

@Composable
private fun MainScreen(
    viewModel: ShareSettingsViewModel,
    descriptionText: String,
    useTabletPaddings: Boolean,
    onShareLink: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onChangeAccess: (ShareEntity) -> Unit,
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

                else -> Unit
            }
        }
    }

    ShareSettingsScreen(
        scaffoldState = scaffoldState,
        state = state,
        descriptionText = descriptionText,
        onBack = onBack,
        useTabletPaddings = useTabletPaddings,
        isCreateLoading = isCreateLoading,
        onCreate = viewModel::create,
        onShareClick = onShareLink,
        onLinkClick = onLinkClick,
        onAddUsers = onAddUsers,
        onChangeAccess = onChangeAccess
    )
}

@Composable
private fun ShareSettingsScreen(
    scaffoldState: ScaffoldState,
    state: ShareSettingsState,
    isCreateLoading: Boolean,
    useTabletPaddings: Boolean,
    descriptionText: String,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onChangeAccess: (ShareEntity) -> Unit,
    onBack: () -> Unit
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
                            text = descriptionText
                        )
                    }
                    item {
                        val groupedShareList = state.members.groupBy { it.itemAccessType }
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
                                onClick = onChangeAccess
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
            descriptionText = stringResource(R.string.rooms_share_shared_folder_desc),
            useTabletPaddings = false,
            isCreateLoading = false,
            onBack = {},
            onCreate = {},
            onShareClick = {},
            onAddUsers = {},
            onLinkClick = {},
            onChangeAccess = {}
        )
    }
}