package app.editors.manager.ui.fragments.share

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.UserListState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.R

@Composable
fun UserListScreen(
    title: Int,
    userListState: UserListState,
    closeable: Boolean = true,
    onClick: (String) -> Unit, // user id
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    bottomContent: @Composable () -> Unit = {}
) {
    val searchState = remember { mutableStateOf(false) }

    AppScaffold(
        useTablePaddings = false,
        topBar = {
            AnimatedVisibility(
                visible = !searchState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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
            AnimatedVisibility(visible = searchState.value, enter = fadeIn(), exit = fadeOut()) {
                SearchAppBar(
                    onCloseClick = { searchState.value = false },
                    onTextChange = onSearch::invoke
                )
            }
        }
    ) {
        if (userListState.loading) {
            LoadingPlaceholder()
        } else {
            Column {
                if (userListState.requestLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    VerticalSpacer(height = 4.dp)
                }

                val users = userListState.users
                if (users.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        val groupUser = users.groupBy { it.displayName.first().uppercaseChar() }.toSortedMap()
                        groupUser.forEach { (letter, users) ->
                            itemsIndexed(items = users.orEmpty()) { index, user ->
                                UserItem(
                                    letter = letter?.toString().takeIf { index == 0 },
                                    user = user,
                                    selected = userListState.selected.contains(user.id),
                                    onClick = { onClick.invoke(user.id) },
                                )
                            }
                        }
                    }
                    bottomContent()
                } else {
                    PlaceholderView(
                        image = R.drawable.placeholder_not_found,
                        title = stringResource(id = app.editors.manager.R.string.room_search_not_found),
                        subtitle = stringResource(id = app.editors.manager.R.string.room_search_not_found_desc)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.UserItem(letter: String?, user: User, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .animateItemPlacement()
            .clickable(onClick = onClick, enabled = !user.shared),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Box(
            modifier = Modifier
                .enabled(!user.shared)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
                .clip(CircleShape)
                .size(40.dp)
        ) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                model = user.avatarMedium,
                contentDescription = null,
                loading = placeholder(app.editors.manager.R.drawable.ic_account_placeholder),
                failure = placeholder(app.editors.manager.R.drawable.ic_account_placeholder)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = .8f))
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
                .enabled(!user.shared)
                .weight(1f)
                .padding(start = 12.dp, end = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = user.email.orEmpty(),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
        if (user.shared) {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(id = app.editors.manager.R.string.invite_already_invited),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
    }
}

@Composable
fun SearchAppBar(
    onTextChange: (String) -> Unit,
    onCloseClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val searchValueState = remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        AppTextField(
            state = searchValueState,
            onValueChange = {
                searchValueState.value = it
                onTextChange.invoke(it)
            },
            focusManager = focusManager,
            label = app.editors.manager.R.string.share_title_search,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onDone = { focusManager.clearFocus(true) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = android.R.string.search_go),
                    modifier = Modifier.alpha(ContentAlpha.medium)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (searchValueState.value.isNotEmpty()) {
                            onTextChange("")
                        } else {
                            onCloseClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = android.R.string.cancel),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewMainWithBottom() {
    ManagerTheme {
        val selected = remember { mutableStateListOf("id5") }
        UserListScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            userListState = UserListState(
                loading = false,
                requestLoading = true,
                selected = selected,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email"),
                    User().copy(displayName = "User", id = "id1", email = "email"),
                    User().copy(displayName = "Mike", id = "id2", email = "email"),
                    User().copy(displayName = "mike", id = "id3", email = "email"),
                    User().copy(displayName = "User", id = "id4", email = "email"),
                    User().copy(displayName = "123", id = "id5", email = "email"),
                    User().copy(displayName = "5mike", id = "id6", email = "email", shared = true)
                )
            ), false,
            { user -> if (selected.contains(user)) selected.remove(user) else selected.add(user) },
            {},
            {}
        ) {
            UserListBottomContent(count = selected.size,
                access = 4,
                accessList = RoomUtils.getAccessOptions(ApiContract.RoomType.CUSTOM_ROOM, false),
                {}, {}, {})
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
        UserListScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            userListState = UserListState(
                loading = false,
                requestLoading = false,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email"),
                    User().copy(displayName = "User", id = "id1", email = "email"),
                    User().copy(displayName = "Mike", id = "id2", email = "email"),
                    User().copy(displayName = "mike", id = "id3", email = "email"),
                    User().copy(displayName = "User", id = "id4", email = "email"),
                    User().copy(displayName = "123", id = "id5", email = "email"),
                    User().copy(displayName = "5mike", id = "id6", email = "email", shared = true)
                ),
                selected = listOf("id5")
            ), false, {}, {}, {}, {}
        )
    }
}

@Preview
@Composable
private fun PreviewEmptyMain() {
    ManagerTheme {
        UserListScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            userListState = UserListState(
                loading = false,
                requestLoading = false,
                users = emptyList()
            ), false, {}, {}, {}, {}
        )
    }
}