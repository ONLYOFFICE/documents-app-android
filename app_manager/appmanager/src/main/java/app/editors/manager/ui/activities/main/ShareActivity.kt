@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.documents.core.model.login.Group
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.share.models.Share
import app.documents.core.network.share.models.SharedTo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.shareApi
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.views.custom.AccessDropdownMenu
import app.editors.manager.ui.views.custom.SearchAppBar
import app.editors.manager.viewModels.main.ShareEffect
import app.editors.manager.viewModels.main.ShareState
import app.editors.manager.viewModels.main.ShareViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorButtonBackground
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize
import lib.toolkit.base.managers.utils.openSendTextActivity
import retrofit2.HttpException

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_ITEM_ID: String = "KEY_SHARE_ITEM_ID"

        @JvmStatic
        fun show(fragment: Fragment, itemId: String) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply { putExtra(KEY_SHARE_ITEM_ID, itemId) },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManagerTheme {
                val navController = rememberNavController()
                val viewModel = viewModel {
                    ShareViewModel(
                        itemId = intent.getStringExtra(KEY_SHARE_ITEM_ID).orEmpty(),
                        shareApi = shareApi,
                        managerApi = api,
                        folder = false
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

                LaunchedEffect(Unit) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ShareEffect.Error -> {
                                val text = when (val error = effect.throwable) {
                                    is HttpException -> getString(R.string.errors_client_error) + error.code()
                                    else -> getString(R.string.errors_unknown_error)
                                }
                                UiUtils.getSnackBar(this@ShareActivity)
                                    .setText(text)
                                    .show()
                            }
                            is ShareEffect.InternalLink -> {
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    effect.url,
                                    getString(R.string.share_clipboard_external_link_label)
                                )
                                UiUtils.getSnackBar(this@ShareActivity)
                                    .setText(R.string.share_clipboard_external_copied)
                                    .show()
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
                            onUserAccess = viewModel::setUserAccess,
                            accessList = ManagerUiUtils.getAccessList(state.extension),
                            onBack = ::finish,
                            onCopyExternalLink = {
                                KeyboardUtils.setDataToClipboard(
                                    context,
                                    state.externalLink.sharedTo.shareLink,
                                    getString(R.string.share_clipboard_external_link_label)
                                )
                                UiUtils.getSnackBar(this@ShareActivity)
                                    .setText(R.string.share_clipboard_external_copied)
                                    .show()
                            },
                            onSendExternalLink = {
                                openSendTextActivity(
                                    getString(R.string.share_clipboard_external_link_label),
                                    state.externalLink.sharedTo.shareLink
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class Screens {
    Main, Members
}

@Composable
private fun MainScreen(
    shareState: ShareState,
    token: String,
    portalWithScheme: String,
    accessList: List<Int>,
    onCopyInternalLink: () -> Unit,
    onCopyExternalLink: () -> Unit,
    onSendExternalLink: () -> Unit,
    onSearch: (String) -> Unit,
    onLinkAccess: (Int) -> Unit,
    onUserAccess: (String, Int) -> Unit,
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
            FloatingActionButton({

            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add_users),
                    contentDescription = null
                )
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
                        AnimatedVisibilityVerticalFade(visible = !searchState) {
                            ExternalLinkContent(
                                externalLink = shareState.externalLink,
                                accessList = accessList,
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
                        accessList = accessList,
                        onAccess = onUserAccess
                    )
                    ListContent(
                        title = R.string.rooms_info_groups_title,
                        shareList = shareState.groups,
                        portalWithScheme = portalWithScheme,
                        token = token,
                        accessList = accessList,
                        onAccess = onUserAccess
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
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            var dropdown by remember { mutableStateOf(false) }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.share_main_external_access))
                Text(
                    text = stringResource(id = R.string.share_main_external_access_info),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            }
            IconButton(onClick = { dropdown = true }, enabled = !externalLink.isLocked) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(externalLink.accessCode)),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextSecondary
                    )
                    if (!externalLink.isLocked) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_drawer_menu_header_arrow),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                    }
                    AccessDropdownMenu(
                        onDismissRequest = { dropdown = false },
                        expanded = dropdown,
                        accessList = accessList,
                        onClick = { newAccess ->
                            onAccess.invoke(newAccess)
                            dropdown = false
                        }
                    )
                }
            }
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
private fun LazyItemScope.UserItem(
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
                    .size(48.dp)
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
                    .size(48.dp)
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
                var dropdown by remember { mutableStateOf(false) }
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
                IconButton(
                    onClick = { dropdown = true },
                    modifier = Modifier.padding(end = 16.dp),
                    enabled = !share.isLocked
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(share.accessCode)),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                        if (!share.isLocked) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_drawer_menu_header_arrow),
                                contentDescription = null,
                                tint = MaterialTheme.colors.colorTextSecondary
                            )
                        }
                        AccessDropdownMenu(
                            onDismissRequest = { dropdown = false },
                            expanded = dropdown,
                            accessList = accessList,
                            onClick = { newAccess ->
                                onAccess.invoke(share.sharedTo.id, newAccess)
                                dropdown = false
                            }
                        )
                    }
                }
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
            onUserAccess = { _, _ -> },
            accessList = listOf(),
            onCopyExternalLink = {},
            onSendExternalLink = {}
        ) {}
    }
}