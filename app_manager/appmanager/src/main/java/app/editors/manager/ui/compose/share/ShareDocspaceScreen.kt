package app.editors.manager.ui.compose.share

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import app.documents.core.network.common.contracts.ApiContract
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
import app.editors.manager.ui.fragments.share.link.ExternalLinkItem
import app.editors.manager.ui.fragments.share.link.ExternalLinkSettingsScreen
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.share.link.ShareUsersList
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.link.ShareSettingsEffect
import app.editors.manager.viewModels.link.ShareSettingsState
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import app.editors.manager.viewModels.main.ShareAccessViewModel
import app.editors.manager.viewModels.main.ShareUserListViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
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
    onClose: (Boolean?) -> Unit,
    onShowSnackbar: (String) -> Unit,
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

    BackHandler(onBack = { onClose(viewModel.hasLinks) })

    NavHost(
        modifier = Modifier.background(MaterialTheme.colors.background),
        navController = navController,
        startDestination = Screen.Main
    ) {
        composable<Screen.Main> {
            MainScreen(
                viewModel = viewModel,
                shareData = shareData,
                useTabletPaddings = useTabletPadding,
                onShareLink = onSendLink,
                onBack = { onClose(viewModel.hasLinks) },
                onAddUsers = { navController.navigate(Screen.AddUser) },
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
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        composable<Screen.LinkSettings> { backstackEntry ->
            val data = backstackEntry.toRoute<Screen.LinkSettings>()
            val json = URLDecoder.decode(data.link, Charsets.UTF_8.toString())
            ExternalLinkSettingsScreen(
                link = Json.decodeFromString<ExternalLink>(json),
                shareData = shareData,
                onBackListener = navController::popBackStackWhenResumed
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
                onBack = navController::popBackStackWhenResumed,
            ) { size, access ->
                UserListBottomContent(
                    nextButtonTitle = lib.toolkit.base.R.string.common_next,
                    count = size,
                    access = access.toUi(true),
                    accessList = shareData.getAccessList(AccessTarget.User),
                    onAccess = userListViewModel::setAccess,
                    onDelete = userListViewModel::onDelete
                ) {
                    val users = userListViewModel.getSelectedUsers().takeIf { it.isNotEmpty() }
                        ?.let { Json.encodeToString(it) }
                    val groups = userListViewModel.getSelectedGroups().takeIf { it.isNotEmpty() }
                        ?.let { Json.encodeToString(it) }
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
            val decodedGroups =
                data.groups?.let { URLDecoder.decode(it, Charsets.UTF_8.toString()) }
            val shareAccessViewModel = viewModel {
                ShareAccessViewModel(
                    roomProvider = roomProvider,
                    shareData = shareData,
                    access = Access.get(data.access),
                    users = decodedUsers?.let { Json.decodeFromString<List<User>>(it) }.orEmpty(),
                    groups = decodedGroups?.let {
                        Json.decodeFromString<List<Group>>(it)
                    }.orEmpty(),
                    emails = emptyList(),
                )
            }
            InviteAccessScreen(
                accessList = shareData.getAccessList(AccessTarget.User),
                viewModel = shareAccessViewModel,
                onBack = navController::popBackStackWhenResumed,
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
    shareData: ShareData,
    useTabletPaddings: Boolean,
    onShareLink: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onChangeAccess: (ShareEntity) -> Unit,
    onBack: () -> Unit,
    onShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current
    var isCreateLoading by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ShareSettingsEffect.Copy -> {
                    KeyboardUtils.setDataToClipboard(context, effect.link)
                    onShowSnackbar(context.getString(R.string.rooms_info_create_link_complete))
                }

                is ShareSettingsEffect.Error -> {
                    onShowSnackbar(
                        effect.code?.let { code ->
                            if (code == R.string.rooms_info_create_maximum_exceed) {
                                context.getString(R.string.rooms_info_create_maximum_exceed)
                            } else {
                                context.getString(R.string.errors_client_error) + code
                            }
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
        state = state,
        shareData = shareData,
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
    state: ShareSettingsState,
    shareData: ShareData,
    isCreateLoading: Boolean,
    useTabletPaddings: Boolean,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
    onAddUsers: () -> Unit,
    onChangeAccess: (ShareEntity) -> Unit,
    onBack: () -> Unit
) {
    val linksDescription = remember {
        when {
            shareData.roomType in listOf(
                ApiContract.RoomType.COLLABORATION_ROOM,
                ApiContract.RoomType.VIRTUAL_ROOM
            ) -> null

            shareData.isRoom && shareData.roomType == ApiContract.RoomType.FILL_FORMS_ROOM -> R.string.rooms_info_fill_form_desc
            shareData.isRoom -> R.string.rooms_info_access_desc
            shareData.roomType == null -> null
            shareData.fileExt.isNullOrEmpty() -> R.string.rooms_info_folder_desc
            else -> R.string.rooms_info_file_desc
        }
    }

    val bottomDescription = remember {
        if (!shareData.isFolder) {
            R.string.rooms_share_shared_desc
        } else {
            R.string.rooms_share_shared_folder_desc
        }
    }

    AppScaffold(
        useTablePaddings = useTabletPaddings,
        topBar = {
            AppTopBar(
                title = R.string.share_title_main,
                backListener = onBack,
                actions = {
                    if (shareData.shouldShowUsers) {
                        TopAppBarAction(
                            icon = R.drawable.ic_add_users,
                            onClick = onAddUsers
                        )
                    }
                }
            )
        }
    ) {
        when (state) {
            is ShareSettingsState.Loading -> LoadingPlaceholder()
            is ShareSettingsState.Success -> {
                LazyColumn {
                    linksBlock(
                        descriptionText = linksDescription,
                        bottomDescription = bottomDescription,
                        roomType = shareData.roomType ?: -1,
                        canAddLinks = (state.links.size < ShareData.MAX_SHARED_LINKS) && !isCreateLoading,
                        isCreateLoading = isCreateLoading,
                        links = state.links,
                        onCreate = onCreate,
                        onShareClick = onShareClick,
                        onLinkClick = onLinkClick
                    )

                    if (shareData.shouldShowUsers) {
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
}

private fun LazyListScope.linksBlock(
    descriptionText: Int?,
    bottomDescription: Int,
    roomType: Int,
    isCreateLoading: Boolean,
    canAddLinks: Boolean,
    links: List<ExternalLink>,
    onCreate: () -> Unit,
    onShareClick: (String) -> Unit,
    onLinkClick: (ExternalLink) -> Unit,
) {
    descriptionText?.let { desc ->
        item {
            AppDescriptionItem(
                modifier = Modifier.padding(top = 8.dp),
                text = desc
            )
        }
    }

    item {
        Row {
            if (roomType != ApiContract.RoomType.FILL_FORMS_ROOM && links.isNotEmpty()) {
                AppHeaderItem(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.rooms_share_shared_links_count, links.size)
                )
                IconButton(onClick = onCreate, enabled = canAddLinks) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_default_add),
                        tint = if (canAddLinks) MaterialTheme.colors.primary else MaterialTheme.colors.colorTextTertiary,
                        contentDescription = null
                    )
                }
            } else {
                AppHeaderItem(title = R.string.rooms_share_shared_links)
            }
        }
    }

    if (links.isNotEmpty()) {
        items(links, key = { it.sharedTo.id }) { link ->
            ExternalLinkItem(
                linkTitle = link.sharedTo.title,
                access = link.access,
                hasPassword = !link.sharedTo.password.isNullOrEmpty(),
                expiring = !link.sharedTo.expirationDate.isNullOrEmpty(),
                internal = link.sharedTo.internal == true,
                isExpired = link.sharedTo.isExpired,
                onShareClick = { onShareClick.invoke(link.sharedTo.shareLink) },
                onClick = { onLinkClick(link) },
                modifier = Modifier.animateItem()
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
                    .animateItem()
                    .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AppCircularProgress()
            }
        }
    }

    if (descriptionText == null) {
        item {
            AppDescriptionItem(
                modifier = Modifier.padding(top = 8.dp),
                text = bottomDescription
            )
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
            title = "Shared link",
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
            shareData = ShareData(roomType = ApiContract.RoomType.FILL_FORMS_ROOM, fileExt = "docx"),
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