package app.editors.manager.ui.fragments.share

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.User
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.views.custom.AccessDropdownMenu
import app.editors.manager.viewModels.main.UserListState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.R

@Composable
fun UserListScreen(
    title: Int,
    usersViewState: UserListState,
    onClick: (User) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    bottomContent: @Composable () -> Unit = {}
) {
    val searchState = remember { mutableStateOf(false) }

    if (usersViewState.loading) {
        LoadingPlaceholder()
    } else {
        AppScaffold(
            topBar = {
                AnimatedVisibility(
                    visible = !searchState.value,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    AppTopBar(
                        title = stringResource(id = title),
                        isClose = true,
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
            Column {
                val users = usersViewState.users
                if (users.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        val groupUser = users.groupBy { it.first.displayName.first().uppercaseChar() }.toSortedMap()
                        groupUser.forEach { (letter, users) ->
                            itemsIndexed(items = users.orEmpty()) { index, user ->
                                UserItem(
                                    letter = letter?.toString().takeIf { index == 0 },
                                    user = user,
                                    onClick = { onClick.invoke(user.first) }
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
private fun LazyItemScope.UserItem(letter: String?, user: Pair<User, Boolean>, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .animateItemPlacement()
            .clickable(onClick = onClick),
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
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
                .clip(CircleShape)
                .size(40.dp)
        ) {
            GlideImage(
                modifier = Modifier.fillMaxSize(),
                model = user.first.avatarMedium,
                contentDescription = null,
                loading = placeholder(app.editors.manager.R.drawable.ic_account_placeholder),
                failure = placeholder(app.editors.manager.R.drawable.ic_account_placeholder)
            )
            if (user.second) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colors.colorTextSecondary)
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
                .fillMaxWidth()
                .padding(start = 12.dp, end = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = user.first.displayName,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = user.first.email.orEmpty(),
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
            .height(56.dp),
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        AppTextField(
            state = searchValueState,
            onValueChange = onTextChange,
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

@Preview
@Composable
private fun PreviewMainWithBottom() {
    ManagerTheme {
        UserListScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            usersViewState = UserListState(
                loading = false,
                requestLoading = false,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email") to false,
                    User().copy(displayName = "User", id = "id1", email = "email") to false,
                    User().copy(displayName = "Mike", id = "id2", email = "email") to false,
                    User().copy(displayName = "mike", id = "id3", email = "email") to false,
                    User().copy(displayName = "User", id = "id4", email = "email") to false,
                    User().copy(displayName = "123", id = "id5", email = "email") to true,
                    User().copy(displayName = "5mike", id = "id6", email = "email") to false
                )
            ), {}, {}, {}, {}
        ) {
            AppDivider()
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextSecondary
                    )
                }
                Text(text = "2", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center)
                var dropdown by remember { mutableStateOf(false) }
                IconButton(onClick = { dropdown = true }) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(ManagerUiUtils.getAccessIcon(4)),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                        Icon(
                            imageVector = ImageVector.vectorResource(app.editors.manager.R.drawable.ic_drawer_menu_header_arrow),
                            contentDescription = null,
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                        AccessDropdownMenu(
                            onDismissRequest = { dropdown = false },
                            expanded = dropdown,
                            accessList = RoomUtils.getAccessOptions(ApiContract.RoomType.CUSTOM_ROOM, false),
                            onClick = { newAccess -> dropdown = false }
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                AppTextButton(
                    title = R.string.common_next
                ) {

                }
            }
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
            usersViewState = UserListState(
                loading = false,
                requestLoading = false,
                users = listOf(
                    User().copy(displayName = "user", id = "id", email = "email") to false,
                    User().copy(displayName = "User", id = "id1", email = "email") to false,
                    User().copy(displayName = "Mike", id = "id2", email = "email") to false,
                    User().copy(displayName = "mike", id = "id3", email = "email") to false,
                    User().copy(displayName = "User", id = "id4", email = "email") to false,
                    User().copy(displayName = "123", id = "id5", email = "email") to true,
                    User().copy(displayName = "5mike", id = "id6", email = "email") to false
                )
            ), {}, {}, {}, {}
        )
    }
}

@Preview
@Composable
private fun PreviewEmptyMain() {
    ManagerTheme {
        UserListScreen(
            title = app.editors.manager.R.string.room_set_owner_title,
            usersViewState = UserListState(
                loading = false,
                requestLoading = false,
                users = emptyList()
            ), {}, {}, {}, {}
        )
    }
}