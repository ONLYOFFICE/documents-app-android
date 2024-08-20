package app.editors.manager.ui.fragments.share

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.views.custom.SearchAppBar
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.UserListEffect
import app.editors.manager.viewModels.main.UserListState
import app.editors.manager.viewModels.main.UserListViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.launch
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTabRow
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TabRowItem
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountUtils

@Composable
fun UserListScreen(
    viewModel: UserListViewModel,
    title: Int,
    withGroups: Boolean = false,
    closeable: Boolean = true,
    disableInvited: Boolean = false,
    useTabletPaddings: Boolean = false,
    onClick: (id: String) -> Unit,
    onBack: () -> Unit,
    onSnackBar: (String) -> Unit,
    onSuccess: ((User) -> Unit)? = null,
    bottomContent: @Composable (Int, Int) -> Unit = { _, _ -> }
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserListEffect.Error -> onSnackBar.invoke(effect.message)
                is UserListEffect.Success -> onSuccess?.invoke(effect.user)
            }
        }
    }

    MainScreen(
        state = state,
        title = title,
        withGroups = withGroups,
        closeable = closeable,
        disableInvited = disableInvited,
        useTabletPaddings = useTabletPaddings,
        onClick = onClick,
        onSearch = viewModel::search,
        onBack = onBack,
        onSnackBar = onSnackBar,
        bottomContent = bottomContent
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(
    state: UserListState,
    title: Int,
    withGroups: Boolean = false,
    closeable: Boolean = true,
    disableInvited: Boolean = false,
    useTabletPaddings: Boolean = false,
    onClick: (id: String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    onSnackBar: (String) -> Unit,
    bottomContent: @Composable (count: Int, access: Int) -> Unit = { _, _ -> },
) {
    val searchState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val token = remember { context.accountOnline?.accountName?.let { AccountUtils.getToken(context, it) }.orEmpty() }
    val portal = remember { context.accountOnline?.portal?.urlWithScheme }
    val tabs = remember {
        listOfNotNull(
            TabRowItem(
                context.getString(app.editors.manager.R.string.share_goal_user)
            ),
            TabRowItem(
                context.getString(app.editors.manager.R.string.share_goal_group)
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = tabs::size)

    AppScaffold(
        useTablePaddings = useTabletPaddings,
        topBar = {
            Column {
                AnimatedContent(
                    targetState = searchState.value,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "appbar_content"
                ) { search ->
                    if (search) {
                        SearchAppBar(
                            onClose = { searchState.value = false; onSearch.invoke("") },
                            onTextChange = onSearch::invoke
                        )
                    } else {
                        AppTopBar(
                            title = stringResource(id = title),
                            isClose = closeable,
                            backListener = onBack,
                            actions = {
                                TopAppBarAction(
                                    icon = app.editors.manager.R.drawable.ic_toolbar_search,
                                    onClick = { searchState.value = true }
                                )
                            }
                        )
                    }
                }
                if (withGroups) {
                    AppTabRow(
                        pagerState = pagerState,
                        tabs = tabs,
                        onTabClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(it) }
                        }
                    )
                }
            }
        }
    ) {
        if (state.loading) {
            LoadingPlaceholder()
        } else {
            Column {
                if (state.requestLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    VerticalSpacer(height = 4.dp)
                }

                HorizontalPager(modifier = Modifier.weight(1f), state = pagerState) {
                    when (it) {
                        0 -> {
                            val users = state.users
                            if (users.isNotEmpty()) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    users.groupBy { user -> user.displayNameFromHtml.first().uppercaseChar() }
                                        .toSortedMap()
                                        .forEach { (letter, users) ->
                                            itemsIndexed(items = users.orEmpty()) { index, user ->
                                                UserItem(
                                                    withLetter = true,
                                                    letter = letter?.toString().takeIf { index == 0 },
                                                    name = user.displayNameFromHtml,
                                                    subtitle = user.groups.joinToString { group -> group.name },
                                                    shared = disableInvited && user.shared,
                                                    selected = state.selected.contains(user.id),
                                                    onClick = { onClick.invoke(user.id) },
                                                    avatar = GlideUtils.getCorrectLoad(
                                                        user.avatarMedium,
                                                        token,
                                                        portal
                                                    )
                                                )
                                            }
                                        }
                                }
                            } else {
                                PlaceholderView(withGroups, searchState.value)
                            }
                        }
                        1 -> {
                            val groups = state.groups
                            if (groups.isNotEmpty()) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(items = groups) { group ->
                                        UserItem(
                                            withLetter = false,
                                            name = group.name,
                                            shared = group.shared,
                                            letter = null,
                                            subtitle = null,
                                            avatar = null,
                                            selected = state.selected.contains(group.id),
                                            onClick = { onClick.invoke(group.id) }
                                        )
                                    }
                                }
                            } else {
                                PlaceholderView(withGroups, searchState.value)
                            }
                        }
                    }
                }
                bottomContent(state.selected.size, state.access ?: 0)
            }
        }
    }
}

@Composable
private fun PlaceholderView(withGroups: Boolean, search: Boolean) {
    val title: Int
    val subtitle: Int

    if (search) {
        title = app.editors.manager.R.string.placeholder_no_search_result
        subtitle = app.editors.manager.R.string.placeholder_no_search_result_desc
    } else if (withGroups) {
        title = app.editors.manager.R.string.placeholder_no_members_found
        subtitle = app.editors.manager.R.string.placeholder_no_members_found_desc
    } else {
        title = app.editors.manager.R.string.placeholder_no_users_found
        subtitle = app.editors.manager.R.string.placeholder_no_users_found_desc
    }

    PlaceholderView(
        image = R.drawable.placeholder_no_search_result,
        title = stringResource(id = title),
        subtitle = stringResource(id = subtitle)
    )
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.UserItem(
    name: String,
    selected: Boolean,
    shared: Boolean,
    letter: String?,
    withLetter: Boolean,
    avatar: Any?,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .animateItemPlacement()
            .clickable(onClick = onClick, enabled = !shared),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (withLetter) {
            Text(
                text = letter.orEmpty(),
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .width(40.dp)
                    .alpha(letter?.let { 1f } ?: 0f)
                    .padding(start = 16.dp)
            )
        }
        Box(
            modifier = Modifier
                .enabled(!shared)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
                .clip(CircleShape)
                .size(40.dp)
        ) {
            if (avatar != null) {
                GlideImage(
                    modifier = Modifier.fillMaxSize(),
                    model = avatar,
                    contentDescription = null,
                    loading = placeholder(app.editors.manager.R.drawable.ic_account_placeholder),
                    failure = placeholder(app.editors.manager.R.drawable.ic_account_placeholder)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorIconBackground))
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lib.toolkit.base.managers.utils.StringUtils.getAvatarName(name),
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.colorTextSecondary
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = .7f))
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .enabled(!shared)
                .weight(1f)
                .padding(start = 12.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
                maxLines = 1
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                    color = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
        if (shared) {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(id = app.editors.manager.R.string.invite_already_invited),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewMainWithBottom() {
    ManagerTheme {
        val selected = remember { mutableStateListOf("id5") }
        MainScreen(
            title = app.editors.manager.R.string.invite_choose_from_list,
            state = UserListState(
                loading = false,
                requestLoading = false,
                selected = selected,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email"),
                    User().copy(displayName = "User", id = "id1", email = "email"),
                    User().copy(displayName = "Mike", id = "id2", email = "email"),
                    User().copy(displayName = "mike", id = "id3", email = "email"),
                    User().copy(displayName = "User", id = "id4", email = "email"),
                    User().copy(displayName = "123", id = "id5", email = "email"),
                    User().copy(displayName = "5mike", id = "id6", email = "email", shared = true)
                ),
                groups = listOf(
                    Group("", "group 1")
                )
            ),
            disableInvited = true,
            withGroups = true,
            closeable = false,
            onClick = { user -> if (selected.contains(user)) selected.remove(user) else selected.add(user) },
            onSearch = {},
            onSnackBar = {},
            onBack = {}
        ) { _, _ ->
            UserListBottomContent(
                nextButtonTitle = app.editors.manager.R.string.share_invite_title,
                count = selected.size,
                access = 4,
                accessList = RoomUtils.getAccessOptions(ApiContract.RoomType.CUSTOM_ROOM, false),
                {}, {}) {}
        }
    }
}

@Preview
@Composable
private fun PreviewSearch() {
    ManagerTheme {
        SearchAppBar({}, {})
    }
}

@Preview
@Composable
private fun PreviewMain() {
    ManagerTheme {
        MainScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            state = UserListState(
                loading = false,
                requestLoading = false,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email"),
                    User().copy(displayName = "User", id = "id1", email = "email"),
                    User().copy(displayName = "Mike", id = "id2", email = "email"),
                    User().copy(displayName = "mike", id = "id3", email = "email"),
                    User().copy(displayName = "User", id = "id4", email = "email"),
                    User().copy(displayName = "123", id = "id5", email = "email"),
                    User().copy(displayName = "5mike", id = "id6", email = "email")
                ),
                groups = listOf(
                    Group("", "group 1")
                )
            ),
            withGroups = false,
            closeable = false,
            onClick = { },
            onSearch = {},
            onSnackBar = {},
            onBack = {}
        ) { _, _ -> }
    }
}

@Preview
@Composable
private fun PreviewEmptyMain() {
    ManagerTheme {
        MainScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            state = UserListState(
                loading = false,
                requestLoading = false,
                users = emptyList()
            ),
            withGroups = false,
            closeable = false,
            onClick = { },
            onSearch = {},
            onSnackBar = {},
            onBack = {}
        )
    }
}