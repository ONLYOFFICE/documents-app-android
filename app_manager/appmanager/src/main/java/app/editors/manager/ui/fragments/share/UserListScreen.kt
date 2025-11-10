package app.editors.manager.ui.fragments.share

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.model.login.Group
import app.documents.core.model.login.Member
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.getTypeTitle
import app.editors.manager.managers.utils.toUi
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.views.custom.SearchAppBar
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.UserListEffect
import app.editors.manager.viewModels.main.UserListMode
import app.editors.manager.viewModels.main.UserListState
import app.editors.manager.viewModels.main.UserListViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.launch
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTabRow
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TabRowItem
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.R as R2

private data class Header(val title: Int) : Member {

    override val id: String
        get() = title.toString()

    override val shared: Boolean
        get() = false
}

private sealed class PagerTab(title: Int): TabRowItem(title) {
    class Users(isDocSpace: Boolean) : PagerTab(
        if (isDocSpace) {
            R.string.rooms_user_type_members
        } else {
            R.string.share_goal_user
        }
    )

    data object Groups : PagerTab(R.string.share_goal_group)
    data object Guests : PagerTab(R.string.share_goal_guest)
}

@Composable
fun UserListScreen(
    viewModel: UserListViewModel,
    title: Int,
    closeable: Boolean = true,
    useTabletPaddings: Boolean = false,
    onClick: (Member) -> Unit,
    onBack: () -> Unit,
    onSuccess: ((User) -> Unit)? = null,
    topBarActions: @Composable () -> Unit = {},
    bottomContent: @Composable (Int, Access) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val state by viewModel.viewState.collectAsState()
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserListEffect.Error -> {
                    val message = effect.errorCode?.let { code ->
                        context.getString(R.string.errors_client_error) + code
                    } ?: context.getString(R.string.errors_unknown_error)
                    scaffoldState.snackbarHostState.showSnackbar(message)
                }
                is UserListEffect.Success -> onSuccess?.invoke(effect.user)
                else -> Unit
            }
        }
    }

    MainScreen(
        scaffoldState = scaffoldState,
        state = state,
        title = title,
        closeable = closeable,
        useTabletPaddings = useTabletPaddings,
        onClick = onClick,
        onSearch = viewModel::search,
        onBack = onBack,
        bottomContent = bottomContent,
        topBarActions = topBarActions
    )
}

@Composable
private fun MainScreen(
    scaffoldState: ScaffoldState,
    state: UserListState,
    title: Int,
    closeable: Boolean = true,
    useTabletPaddings: Boolean = false,
    onClick: (Member) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    topBarActions: @Composable () -> Unit = {},
    bottomContent: @Composable (count: Int, access: Access) -> Unit = { _, _ -> },
) {
    val searchState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current
    val token = remember {
        if (!view.isInEditMode) {
            context.accountOnline?.accountName?.let { AccountUtils.getToken(context, it) }.orEmpty()
        } else {
            ""
        }
    }
    val portal = remember {
        if (!view.isInEditMode) {
            context.accountOnline?.portal
        } else {
            CloudPortal(provider = PortalProvider.Cloud.DocSpace)
        }
    }
    val tabs = remember {
        listOfNotNull(
            PagerTab.Users(portal.isDocSpace),
            PagerTab.Groups.takeIf {
                when (state.mode) {
                    is UserListMode.Invite,
                    is UserListMode.TemplateAccess,
                    is UserListMode.Share -> true

                    else -> false
                }
            },
            PagerTab.Guests.takeIf {
                portal.isDocSpace &&
                        when (state.mode) {
                            is UserListMode.Invite,
                            is UserListMode.StartFilling,
                            is UserListMode.Share -> true

                            else -> false
                        }
            }
        )
    }
    val pagerState = rememberPagerState(pageCount = tabs::size)

    AppScaffold(
        scaffoldState = scaffoldState,
        useTablePaddings = useTabletPaddings,
        topBar = {
            AppBar(
                title = stringResource(title),
                closeable = closeable,
                searchState = searchState,
                pagerState = pagerState,
                tabs = tabs,
                onSearch = onSearch,
                onBack = onBack,
                topBarActions = topBarActions
            )
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
                if (searchState.value) {
                    SearchListScreen(
                        modifier = Modifier.weight(1f),
                        state = state,
                        token = token,
                        portal = portal?.urlWithScheme,
                        isDocSpace = portal.isDocSpace,
                        provider = portal?.provider,
                        onClick = onClick
                    )
                } else {
                    PagerScreen(
                        modifier = Modifier.weight(1f),
                        pagerState = pagerState,
                        state = state,
                        tabs = tabs,
                        onClick = onClick,
                        portal = portal?.urlWithScheme,
                        token = token,
                        isDocSpace = portal.isDocSpace,
                        provider = portal?.provider
                    )
                }
                bottomContent(state.selected.size, state.access ?: Access.None)
            }
        }
    }
}

@Composable
private fun SearchListScreen(
    modifier: Modifier,
    state: UserListState,
    onClick: (Member) -> Unit,
    token: String,
    portal: String?,
    provider: PortalProvider?,
    isDocSpace: Boolean
) {
    val members = mutableListOf<Member>()

    if (state.users.isNotEmpty()) {
        members.add(Header(R.string.share_add_common_header_users))
        members.addAll(state.users)
    }

    if (state.groups.isNotEmpty()) {
        members.add(Header(R.string.share_add_common_header_groups))
        members.addAll(state.groups)
    }

    if (state.guests.isNotEmpty()) {
        members.add(Header(R.string.share_goal_guest))
        members.addAll(state.guests)
    }

    LazyColumn(modifier = modifier) {
        items(members, key = Member::id) { member ->
            when (member) {
                is Header -> {
                    AppHeaderItem(member.title)
                }

                is User -> {
                    UserItem(
                        user = member,
                        letter = null,
                        withLetter = false,
                        selected = state.selected.contains(member.id),
                        mode = state.mode,
                        token = token,
                        portal = portal,
                        isDocSpace = isDocSpace,
                        provider = provider,
                        onClick = onClick
                    )
                }

                is Group -> {
                    GroupItem(
                        group = member,
                        mode = state.mode,
                        selected = state.selected.contains(member.id),
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBar(
    title: String,
    closeable: Boolean,
    searchState: MutableState<Boolean>,
    pagerState: PagerState,
    tabs: List<TabRowItem>,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    topBarActions: @Composable () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
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
                    title = title,
                    isClose = closeable,
                    backListener = onBack,
                    actions = {
                        TopAppBarAction(
                            icon = R.drawable.ic_toolbar_search,
                            onClick = { searchState.value = true }
                        )
                        topBarActions()
                    }
                )
            }
        }
        if (!searchState.value && tabs.size > 1) {
            AppTabRow(
                pagerState = pagerState,
                tabs = tabs,
                onTabClick = { coroutineScope.launch { pagerState.animateScrollToPage(it) } }
            )
        }
    }
}

@Composable
private fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    state: UserListState,
    tabs: List<PagerTab>,
    onClick: (Member) -> Unit,
    token: String,
    portal: String?,
    provider: PortalProvider?,
    isDocSpace: Boolean
) {
    HorizontalPager(modifier = modifier, state = pagerState) {
        when (tabs[it]) {
            is PagerTab.Users -> {
                if (state.users.isNotEmpty()) {
                    UsersScreen(
                        users = state.users,
                        mode = state.mode,
                        selectedIdList = state.selected,
                        token = token,
                        portal = portal,
                        isDocSpace = isDocSpace,
                        provider = provider,
                        onClick = onClick
                    )
                } else {
                    PlaceholderView(
                        image = R.drawable.placeholder_no_users,
                        title = stringResource(id = R.string.placeholder_no_members_found),
                        subtitle = stringResource(id = R.string.placeholder_no_members_found_desc)
                    )
                }
            }

            PagerTab.Groups -> {
                if (state.groups.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items = state.groups, key = Group::id) { group ->
                            GroupItem(
                                group = group,
                                mode = state.mode,
                                selected = state.selected.contains(group.id),
                                onClick = onClick
                            )
                        }
                    }
                } else {
                    PlaceholderView(
                        image = R.drawable.placeholder_no_users,
                        title = stringResource(id = R.string.placeholder_no_members_found),
                        subtitle = stringResource(id = R.string.placeholder_no_members_found_desc)
                    )
                }
            }

            PagerTab.Guests -> {
                if (state.guests.isNotEmpty()) {
                    UsersScreen(
                        users = state.guests,
                        mode = state.mode,
                        selectedIdList = state.selected,
                        token = token,
                        portal = portal,
                        isDocSpace = isDocSpace,
                        provider = provider,
                        onClick = onClick
                    )
                } else {
                    PlaceholderView(
                        image = R.drawable.placeholder_no_users,
                        title = stringResource(id = R.string.placeholder_no_members_found),
                        subtitle = stringResource(id = R.string.placeholder_no_members_found_desc)
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.UserItem(
    user: User,
    selected: Boolean,
    letter: String?,
    withLetter: Boolean,
    mode: UserListMode,
    token: String,
    portal: String?,
    isDocSpace: Boolean,
    provider: PortalProvider?,
    onClick: (Member) -> Unit,
) {
    val subtitle = if (isDocSpace) {
        stringResource(user.getTypeTitle(provider)) + user.email?.let { " | $it" }
    } else {
        user.groups.joinToString { group -> group.name }
    }

    val shared = when (mode) {
        UserListMode.TemplateAccess -> {
            val currentUserId = LocalContext.current.accountOnline?.id.orEmpty()
            currentUserId == user.id
        }

        UserListMode.StartFilling -> false

        else -> user.shared
    }

    ListItem(
        withLetter = withLetter,
        letter = letter,
        name = user.displayNameFromHtml,
        subtitle = subtitle,
        shared = shared,
        selected = selected,
        onClick = { onClick.invoke(user) },
        avatar = GlideUtils.getCorrectLoad(
            user.avatarMedium,
            token,
            portal
        )
    )
}

@Composable
private fun LazyItemScope.GroupItem(
    group: Group,
    mode: UserListMode,
    selected: Boolean,
    onClick: (Member) -> Unit
) {
    val shared = remember { if (mode == UserListMode.TemplateAccess) false else group.shared }
    val isEveryone = group.name == "Everyone"
    Column {
        ListItem(
            withLetter = false,
            name = if (isEveryone) stringResource(R.string.share_goal_everyone) else group.name,
            shared = shared,
            letter = null,
            subtitle = null,
            avatar = if (isEveryone) R.drawable.drawable_list_share_image_item_group_placeholder else null,
            selected = selected,
            onClick = { onClick.invoke(group) }
        )
        if (isEveryone) {
            Divider()
        }
    }
}

@Composable
private fun UsersScreen(
    users: List<User>,
    mode: UserListMode,
    selectedIdList: List<String>,
    token: String,
    portal: String?,
    isDocSpace: Boolean,
    provider: PortalProvider?,
    onClick: (Member) -> Unit,
) {
    Column {
        if (mode == UserListMode.TemplateAccess) {
            BoldedResourceText(
                textResId = R.string.setting_access_public_desc,
                boldPartsRes = listOf(
                    R.string.setting_access_public_bold_docspace,
                    R.string.setting_access_public_bold_room
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        bottom = 12.dp,
                        end = 16.dp
                    )
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            users.groupBy { user -> user.displayNameFromHtml.first().uppercaseChar() }
                .toSortedMap()
                .forEach { (letter, users) ->
                    itemsIndexed(
                        items = users.orEmpty(),
                        key = { _, item -> item.id }) { index, user ->
                        UserItem(
                            user = user,
                            letter = letter?.toString().takeIf { index == 0 },
                            withLetter = true,
                            selected = selectedIdList.contains(user.id),
                            mode = mode,
                            token = token,
                            portal = portal,
                            isDocSpace = isDocSpace,
                            provider = provider,
                            onClick = onClick
                        )
                    }
                }
        }
    }
}

@Composable
private fun LazyItemScope.ListItem(
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
            .animateItem()
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
            MemberAvatar(name, avatar)
            this@Row.AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = .7f))
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R2.drawable.ic_done),
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
                    color = MaterialTheme.colors.colorTextSecondary,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (shared) {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(R.string.invite_already_invited),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MemberAvatar(
    name: String,
    avatar: Any?,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        if (avatar != null) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                model = avatar,
                contentDescription = null,
                loading = placeholder(R.drawable.ic_account_placeholder),
                failure = placeholder(R.drawable.ic_account_placeholder)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(id = R2.color.colorIconBackground))
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
    }
}

@Composable
fun BoldedResourceText(
    @StringRes textResId: Int,
    boldPartsRes: List<Int>,
    modifier: Modifier = Modifier
) {
    val fullText = stringResource(id = textResId)
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        for (boldId in boldPartsRes) {
            val boldPart = stringResource(boldId)
            val startIndex = fullText.indexOf(boldPart, lastIndex)
            if (startIndex >= 0) {
                append(fullText.substring(lastIndex, startIndex))
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldPart)
                }
                lastIndex = startIndex + boldPart.length
            }
        }

        if (lastIndex < fullText.length) {
            append(fullText.substring(lastIndex))
        }
    }
    Text(
        style = MaterialTheme.typography.body2,
        color = MaterialTheme.colors.colorTextSecondary,
        text = annotatedString,
        modifier = modifier
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewMainWithBottom() {
    ManagerTheme {
        val selected = remember { mutableStateListOf("id5") }
        MainScreen(
            scaffoldState = rememberScaffoldState(),
            title = R.string.invite_choose_from_list,
            state = UserListState(
                mode = UserListMode.StartFilling,
                loading = false,
                requestLoading = false,
                selected = selected,
                users = listOf(
                    User().copy(
                        displayName = "user",
                        id = "id",
                        email = "email@emailemail.com",
                        isAdmin = true,
                        shared = true
                    ),
                    User().copy(displayName = "User", id = "id1", email = "email@email.com"),
                    User().copy(displayName = "Mike", id = "id2", email = "email@email.com"),
                    User().copy(displayName = "mike", id = "id3", email = "email@email.com"),
                    User().copy(
                        displayName = "User",
                        id = "id4",
                        email = "email@email.com",
                        isAdmin = true
                    ),
                    User().copy(
                        displayName = "123",
                        id = "id5",
                        email = "email@email.com",
                        isAdmin = true
                    ),
                    User().copy(
                        displayName = "5mike",
                        id = "id6",
                        email = "email@email.com",
                        shared = true
                    )
                ),
                groups = listOf(
                    Group("", "group 1")
                )
            ),
            closeable = false,
            onClick = { user ->
                if (selected.contains(user.id)) selected.remove(user.id) else selected.add(
                    user.id
                )
            },
            onSearch = {},
            onBack = {}
        ) { _, _ ->
            UserListBottomContent(
                nextButtonTitle = R.string.share_invite_title,
                count = selected.size,
                access = Access.Editor.toUi(),
                accessList = RoomUtils.getAccessOptions(ApiContract.RoomType.CUSTOM_ROOM, false)
                    .map { it.toUi() },
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
            scaffoldState = rememberScaffoldState(),
            title = R.string.room_set_owner_title,
            state = UserListState(
                mode = UserListMode.ChangeOwner,
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
                ),
                guests = listOf(
                    User().copy(displayName = "Guest", id = "id7", email = "email"),
                    User().copy(displayName = "Guest2", id = "id8", email = "email"),
                )
            ),
            closeable = false,
            onClick = { },
            onSearch = {},
            onBack = {}
        ) { _, _ -> }
    }
}

@Preview
@Composable
private fun PreviewEmptyMain() {
    ManagerTheme {
        MainScreen(
            scaffoldState = rememberScaffoldState(),
            title = R.string.room_set_owner_title,
            state = UserListState(
                mode = UserListMode.ChangeOwner,
                loading = false,
                requestLoading = false,
                users = emptyList()
            ),
            closeable = false,
            onClick = { },
            onSearch = {},
            onBack = {}
        )
    }
}