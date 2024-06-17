@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.SharedTo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.fragments.share.InviteAccessScreen
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.ui.views.custom.AccessIconButton
import app.editors.manager.ui.views.custom.SearchAppBar
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.InviteAccessViewModel
import app.editors.manager.viewModels.main.ShareEffect
import app.editors.manager.viewModels.main.ShareState
import app.editors.manager.viewModels.main.ShareViewModel
import app.editors.manager.viewModels.main.UserListViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorButtonBackground
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.utils.popBackStackWhenResumed
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.managers.utils.getJsonString
import lib.toolkit.base.managers.utils.openSendTextActivity
import retrofit2.HttpException
import java.net.URLEncoder

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"
        private const val KEY_SHARE_IS_FOLDER: String = "KEY_SHARE_IS_FOLDER"

        @JvmStatic
        fun show(fragment: Fragment, itemId: String, isFolder: Boolean) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply {
                    putExtra(KEY_SHARE_ITEM_ID, itemId)
                    putExtra(KEY_SHARE_IS_FOLDER, isFolder)
                },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagerTheme {
                val navController = rememberNavController()
                val isFolder = remember { intent.getBooleanExtra(KEY_SHARE_IS_FOLDER, false) }
                val itemId = remember { intent.getStringExtra(KEY_SHARE_ITEM_ID).orEmpty() }
                val viewModel = viewModel {
                    ShareViewModel(
                        itemId = itemId,
                        shareApi = shareApi,
                        managerApi = api,
                        folder = isFolder
                    )
                }
                val state by viewModel.state.collectAsState()
                val context = LocalContext.current
                val token = remember {
                    AccountUtils
                        .getToken(context, context.accountOnline?.accountName.orEmpty())
                        .orEmpty()
                }
                val portal = remember {
                    context.accountOnline?.portal?.urlWithScheme
                        .orEmpty()
                }

                val accessListWithOutRestricted = state.accessList.filter {
                    it != ApiContract.ShareCode.RESTRICT && it != ApiContract.ShareCode.NONE
                }

                fun onSnackBar(text: String) {
                    UiUtils.getSnackBar(this@ShareActivity)
                        .setText(text)
                        .show()
                }

                LaunchedEffect(Unit) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ShareEffect.Error -> {
                                val text = when (val error = effect.throwable) {
                                    is HttpException -> getString(R.string.errors_client_error) + error.code()
                                    else -> getString(R.string.errors_unknown_error)
                                }
                                onSnackBar(text)
                            }
                            is ShareEffect.InternalLink -> {
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    if (effect.withPortal) {
                                        effect.url
                                    } else {
                                        context.accountOnline?.portal?.urlWithScheme + effect.url
                                    },
                                    "Internal link"
                                )
                                onSnackBar(getString(R.string.share_clipboard_internal_copied))
                            }
                        }
                    }
                }

                NavHost(navController, startDestination = Screens.Main.name) {
                    composable(Screens.Main.name) {
                        MainScreen(
                            shareState = state,
                            token = token,
                            portalWithScheme = portal,
                            onCopyInternalLink = viewModel::copyInternalLink,
                            onSearch = viewModel::search,
                            onLinkAccess = viewModel::setExternalLinkAccess,
                            onMemberAccess = viewModel::setMemberAccess,
                            onBack = ::finish,
                            onCopyExternalLink = {
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    state.externalLink.sharedTo.shareLink,
                                    getString(R.string.share_clipboard_external_link_label)
                                )
                                onSnackBar(getString(R.string.share_clipboard_external_copied))
                            },
                            onSendExternalLink = {
                                openSendTextActivity(
                                    getString(R.string.share_clipboard_external_link_label),
                                    state.externalLink.sharedTo.shareLink
                                )
                            },
                            onAddUsers = {
                                navController.navigate(Screens.AddUsers.name)
                            }
                        )
                    }
                    composable(Screens.AddUsers.name) {
                        val userListViewModel = viewModel {
                            UserListViewModel(
                                access = accessListWithOutRestricted.last(),
                                resourcesProvider = ResourcesProvider(context),
                                shareService = shareApi,
                                invitedIds = state.users.map { it.sharedTo.id } + state.groups.map { it.sharedTo.id },
                            )
                        }
                        UserListScreen(
                            viewModel = userListViewModel,
                            title = R.string.share_invite_user,
                            onClick = userListViewModel::toggleSelect,
                            closeable = false,
                            withGroups = true,
                            disableInvited = true,
                            onBack = navController::popBackStackWhenResumed,
                            onSnackBar = {
                                UiUtils.getSnackBar(this@ShareActivity)
                                    .setText(it)
                                    .show()
                            },
                            bottomContent = { count, access ->
                                UserListBottomContent(
                                    nextButtonTitle = lib.toolkit.base.R.string.common_next,
                                    count = count,
                                    access = accessListWithOutRestricted.last(),
                                    accessList = accessListWithOutRestricted,
                                    onAccess = userListViewModel::setAccess,
                                    onDelete = userListViewModel::onDelete
                                ) {
                                    val users =
                                        Json.encodeToString(userListViewModel.getSelectedUsers().ifEmpty { null })
                                    val groups =
                                        Json.encodeToString(userListViewModel.getSelectedGroups().ifEmpty { null })
                                    navController.navigate(
                                        "${Screens.InviteAccess.name}?" +
                                                "users=${URLEncoder.encode(users, Charsets.UTF_8.toString())}&" +
                                                "groups=${URLEncoder.encode(groups, Charsets.UTF_8.toString())}&" +
                                                "access=$access"
                                    )
                                }
                            }
                        )
                    }
                    composable(
                        route = "${Screens.InviteAccess.name}?" +
                                "users={users}&" +
                                "groups={groups}&" +
                                "access={access}",
                        arguments = listOf(
                            navArgument("users") { type = NavType.StringType; nullable = true },
                            navArgument("groups") { type = NavType.StringType; nullable = true },
                            navArgument("access") { type = NavType.IntType; defaultValue = 2 }
                        )
                    ) {
                        val inviteAccessViewModel = viewModel {
                            InviteAccessViewModel(
                                access = it.arguments?.getInt("access") ?: 2,
                                users = it.arguments?.getJsonString<List<User>>("users", true).orEmpty(),
                                groups = it.arguments?.getJsonString<List<Group>>("groups", true).orEmpty(),
                                isFolder = isFolder,
                                shareService = shareApi,
                                itemId = itemId
                            )
                        }
                        InviteAccessScreen(
                            accessList = accessListWithOutRestricted,
                            viewModel = inviteAccessViewModel,
                            onBack = navController::popBackStackWhenResumed,
                            onSnackBar = ::onSnackBar,
                            onSuccess = {
                                viewModel.fetchShareList()
                                onSnackBar(getString(R.string.invite_link_send_success))
                                navController.navigate(Screens.Main.name) {
                                    popUpTo(Screens.Main.name) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class Screens {
    Main, AddUsers, InviteAccess
}

@Composable
private fun MainScreen(
    shareState: ShareState,
    token: String,
    portalWithScheme: String,
    onCopyInternalLink: () -> Unit,
    onCopyExternalLink: () -> Unit,
    onSendExternalLink: () -> Unit,
    onSearch: (String) -> Unit,
    onLinkAccess: (Int) -> Unit,
    onMemberAccess: (String, Int, Boolean) -> Unit,
    onAddUsers: () -> Unit,
    onBack: () -> Unit
) {
    var searchState by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchState) {
        listState.animateScrollToItem(0)
    }

    AppScaffold(
        topBar = {
            AnimatedContent(
                targetState = searchState,
                label = "search_bar",
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) { state ->
                if (state) {
                    SearchAppBar(onTextChange = onSearch) {
                        onSearch.invoke("")
                        searchState = false
                    }
                } else {
                    AppTopBar(
                        title = R.string.share_title_main,
                        isClose = true,
                        backListener = onBack,
                        actions = {
                            TopAppBarAction(
                                icon = R.drawable.ic_list_context_external_link,
                                onClick = onCopyInternalLink
                            )
                            TopAppBarAction(icon = R.drawable.ic_toolbar_search) {
                                searchState = true
                            }
                        }
                    )
                }
            }
        },
        fab = {
            AnimatedVisibility(
                visible = !searchState,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(onClick = onAddUsers) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_add_users),
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        if (shareState.loading) {
            ActivityIndicatorView()
        } else {
            if (searchState && shareState.users.isEmpty() && shareState.groups.isEmpty()) {
                PlaceholderView(
                    image = lib.toolkit.base.R.drawable.placeholder_not_found,
                    title = stringResource(id = R.string.room_search_not_found),
                    subtitle = ""
                )
            } else {
                LazyColumn(state = listState) {
                    item {
                        AnimatedVisibilityVerticalFade(visible = shareState.requestLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    item {
                        AnimatedVisibilityVerticalFade(visible = !searchState && !shareState.folder) {
                            ExternalLinkContent(
                                externalLink = shareState.externalLink,
                                accessList = shareState.accessList.filter { it != ApiContract.ShareCode.NONE },
                                onAccess = onLinkAccess,
                                onCopy = onCopyExternalLink,
                                onSend = onSendExternalLink
                            )
                        }
                    }
                    ListContent(
                        title = R.string.rooms_info_users_title,
                        shareList = shareState.users,
                        portalWithScheme = portalWithScheme,
                        token = token,
                        accessList = shareState.accessList,
                        onAccess = { id, access -> onMemberAccess.invoke(id, access, false) }
                    )
                    ListContent(
                        title = R.string.rooms_info_groups_title,
                        shareList = shareState.groups,
                        portalWithScheme = portalWithScheme,
                        token = token,
                        accessList = shareState.accessList,
                        onAccess = { id, access -> onMemberAccess.invoke(id, access, true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkContent(
    externalLink: Share,
    accessList: List<Int>,
    onAccess: (Int) -> Unit,
    onCopy: () -> Unit,
    onSend: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .padding(start = 16.dp)
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.share_main_external_access))
                Text(
                    text = stringResource(id = R.string.share_main_external_access_info),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            }
            AccessIconButton(
                access = externalLink.accessCode,
                enabled = !externalLink.isLocked,
                accessList = accessList,
                onAccess = onAccess::invoke
            )
        }
        AnimatedVisibilityVerticalFade(visible = externalLink.accessCode != ApiContract.ShareCode.RESTRICT) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTextButton(
                    title = stringResource(id = R.string.share_main_external_copy_button)
                        .capitalize(),
                    onClick = onCopy
                )
                AppTextButton(
                    title = stringResource(id = R.string.operation_share_send_link)
                        .capitalize(),
                    onClick = onSend
                )
            }
        }
    }
}

private fun LazyListScope.ListContent(
    title: Int,
    portalWithScheme: String,
    token: String,
    shareList: List<Share>,
    accessList: List<Int>,
    onAccess: (String, Int) -> Unit
) {
    if (shareList.isNotEmpty()) {
        item {
            AppHeaderItem(
                title = stringResource(
                    id = title,
                    shareList.size
                )
            )
            VerticalSpacer(height = 8.dp)
        }
        items(shareList) { share ->
            UserItem(
                share = share,
                portalWithScheme = portalWithScheme,
                token = token,
                accessList = accessList,
                onAccess = onAccess
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserItem(
    share: Share,
    portalWithScheme: String,
    token: String,
    accessList: List<Int>,
    onAccess: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (share.sharedTo.avatarMedium.isNotEmpty()) {
            GlideImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                model = GlideUtils.getCorrectLoad(
                    token = token,
                    url = (if (share.sharedTo.avatarMedium.startsWith("http")) "" else portalWithScheme) +
                            share.sharedTo.avatarMedium
                ),
                contentDescription = null
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.colorButtonBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_list_item_share_group_icon),
                    tint = MaterialTheme.colors.colorTextSecondary,
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val title = when {
                        share.sharedTo.displayNameHtml.isNotEmpty() -> share.sharedTo.displayNameHtml
                        share.sharedTo.name.isNotEmpty() -> share.sharedTo.name
                        else -> ""
                    }
                    val subtitle = if (share.sharedTo.groups.isNotEmpty()) {
                        share.sharedTo.groups.joinToString { it.name }
                    } else ""

                    Text(
                        text = title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.body1
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.colorTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                AccessIconButton(
                    access = share.accessCode,
                    enabled = !share.isLocked,
                    accessList = accessList,
                    onAccess = { access -> onAccess.invoke(share.sharedTo.id, access) }
                )
            }
            Divider()
        }
    }
}

@Preview
@Composable
private fun ShareScreenPreview() {
    ManagerTheme {
        MainScreen(
            token = "",
            portalWithScheme = "",
            shareState = ShareState(
                externalLink = Share("0"),
                users = listOf(
                    Share(
                        access = "1",
                        sharedTo = SharedTo(
                            displayName = "User name",
                            groups = listOf(
                                Group(name = "1. Group name"),
                                Group(name = "2. Group name"),
                                Group(name = "3. Group name"),
                                Group(name = "4. Group name")
                            )
                        )
                    ),
                    Share(
                        access = "1",
                        isLocked = true,
                        sharedTo = SharedTo(
                            displayName = "User name",
                            groups = listOf(
                                Group(name = "1. Group name"),
                                Group(name = "2. Group name"),
                                Group(name = "3. Group name"),
                                Group(name = "4. Group name")
                            )
                        )
                    )
                ),
                groups = listOf(
                    Share(
                        access = "1",
                        sharedTo = SharedTo(name = "1. Group name")
                    )
                )
            ),
            onCopyInternalLink = {},
            onSearch = {},
            onLinkAccess = {},
            onMemberAccess = { _, _, _ -> },
            onCopyExternalLink = {},
            onSendExternalLink = {},
            onAddUsers = {}
        ) {}
    }
}