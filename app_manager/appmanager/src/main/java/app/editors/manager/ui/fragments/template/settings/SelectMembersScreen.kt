package app.editors.manager.ui.fragments.template.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.ui.fragments.template.rememberAccountContext
import app.editors.manager.ui.fragments.share.MemberAvatar
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.TemplateUserListViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar

@Composable
fun SelectMembersScreen(
    viewModel: TemplateUserListViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    UserListScreen(
        title = R.string.setting_select_members_title,
        closeable = false,
        useTabletPaddings = false,
        viewModel = viewModel,
        onClick = viewModel::toggleSelect,
        onBack = onBack,
        onSnackBar = {}
    ) { size, _ ->
        UserListBottomContent(
            nextButtonTitle = lib.toolkit.base.R.string.common_next,
            count = size,
            onDelete = viewModel::onDelete,
            onNext = onNext
        )
    }
}

@Composable
fun ConfirmationScreen(
    viewModel: TemplateUserListViewModel,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    ConfirmationScreenContent(
        currentUser = viewModel.getCurrentUser(),
        users = settings.selectedUsers,
        groups = settings.selectedGroups,
        onDelete = viewModel::toggleSelect,
        onNext = onConfirm,
        onBack = onBack
    )
}

@Composable
private fun ConfirmationScreenContent(
    currentUser: User,
    users: List<User>,
    groups: List<Group>,
    onDelete: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.setting_select_members_title),
                backListener = onBack,
            )
        },
        useTablePaddings = false
    ) {
        Column {
            SelectedMembersList(
                currentUser = currentUser,
                users = users,
                groups = groups,
                onDelete = onDelete,
                modifier = Modifier.weight(1f)
            )
            UserListBottomContent(
                nextButtonTitle = R.string.share_invite_title,
                onNext = onNext
            )
        }

    }
}

@Composable
fun SelectedMembersList(
    currentUser: User,
    users: List<User>,
    groups: List<Group>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(vertical = 8.dp),
    innerPadding: Dp = 4.dp,
    onDelete: ((String) -> Unit)? = null
) {
    val accountContext = rememberAccountContext()
    val allUsers = listOf(currentUser) + users

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(innerPadding),
        modifier = modifier.padding(paddingValues)
    ) {
        items(allUsers, key = User::id) { user ->
            val isCurrentUser = user.id == currentUser.id
            AccessMemberItem(
                id = user.id,
                name = user.displayNameFromHtml,
                avatar = GlideUtils.getCorrectLoad(
                    user.avatarMedium,
                    accountContext.token,
                    accountContext.portal.urlWithScheme
                ),
                isCurrentUser = isCurrentUser,
                onDelete = if (!isCurrentUser) onDelete else null
            )
        }

        items(groups, key = Group::id) { group ->
            AccessMemberItem(
                id = group.id,
                name = group.name,
                avatar = null,
                isCurrentUser = false,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun LazyItemScope.AccessMemberItem(
    id: String,
    name: String,
    avatar: Any?,
    isCurrentUser: Boolean,
    onDelete: ((String) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .animateItem(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp)
                    .clip(CircleShape)
                    .size(40.dp)
            ) {
                MemberAvatar(name, avatar)
            }
            MemberTitle(
                name = name,
                isCurrentUser = isCurrentUser,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
            onDelete?.let {
                IconButton(onClick = { onDelete(id) }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_access_panel_close),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextSecondary,
                    )
                }
            }
        }
        AppDivider(startIndent = 16.dp + 40.dp + 16.dp)
    }
}

@Composable
fun MemberTitle(
    name: String,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, false)
        )
        if (isCurrentUser) {
            Text(
                text = stringResource(R.string.access_members_me_subtitle),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.colorTextSecondary,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationScreenPreview() {
    ManagerTheme {
        ConfirmationScreenContent(
            onBack = {},
            users = listOf(
                User(
                    id = "1u",
                    displayName = "Anokhin Tollan"
                )
            ),
            groups = listOf(
                Group(
                    id = "1g",
                    name = "Programming Department"
                )
            ),
            currentUser = User(
                id = "0u",
                displayName = "Anokhin Sergey"
            ),
            onDelete = {},
            onNext = {}
        )
    }
}