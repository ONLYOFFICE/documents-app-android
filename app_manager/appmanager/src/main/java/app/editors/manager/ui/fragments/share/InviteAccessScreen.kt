package app.editors.manager.ui.fragments.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.documents.core.model.cloud.Access
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.editors.manager.R
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.ui.views.custom.AccessIconButton
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.InviteAccessEffect
import app.editors.manager.viewModels.main.InviteAccessState
import app.editors.manager.viewModels.main.InviteAccessViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.VerticalSpacer
import lib.toolkit.base.managers.utils.StringUtils
import retrofit2.HttpException

@Composable
fun InviteAccessScreen(
    accessList: List<Access>,
    viewModel: InviteAccessViewModel,
    description: String? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onSnackBar: (String) -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InviteAccessEffect.Success -> onSuccess.invoke()
                is InviteAccessEffect.Error -> {
                    val text = when (val exception = effect.exception) {
                        is HttpException -> context.getString(R.string.errors_client_error) + exception.code()
                        else -> context.getString(R.string.errors_unknown_error)
                    }
                    onSnackBar(text)
                }
            }
        }
    }

    MainScreen(
        state = state,
        description = description,
        accessList = accessList,
        onSetAccess = viewModel::setAccess,
        onBack = onBack,
        onSetAllAccess = viewModel::setAllAccess,
        onNext = viewModel::invite
    )
}

@Composable
private fun MainScreen(
    state: InviteAccessState,
    description: String? = null,
    accessList: List<Access>,
    onSetAccess: (String, Access) -> Unit,
    onSetAllAccess: (Access) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = R.string.share_choose_access_title,
                backListener = onBack
            )
        },
        useTablePaddings = false
    ) {
        Column {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    if (state.loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        VerticalSpacer(height = 4.dp)
                    }
                }
                item {
                    description?.let { text ->
                        AppDescriptionItem(
                            modifier = Modifier.padding(top = 8.dp),
                            text = text
                        )
                    }
                }
                if (state.emails.isNotEmpty()) {
                    item {
                        AppHeaderItem(title = R.string.invite_new_members)
                    }
                    items(state.emails.toList()) { email ->
                        InviteItem(
                            title = email,
                            subtitle = null,
                            avatar = null,
                            access = state.idAccessList[email] ?: Access.None,
                            accessList = accessList,
                            onSetAccess = onSetAccess,
                            value = email
                        )
                    }
                } else {
                    val users = state.users
                    if (users.isNotEmpty()) {
                        item {
                            AppHeaderItem(title = R.string.invite_new_members)
                        }
                        items(users) { user ->
                            InviteItem(
                                title = user.displayName,
                                subtitle = stringResource(RoomUtils.getUserRole(user))
                                        + user.email?.let { " | $it" },
                                avatar = user.avatarMedium,
                                access = state.idAccessList[user.id] ?: Access.None,
                                accessList = accessList,
                                onSetAccess = onSetAccess,
                                value = user.id
                            )
                        }
                    }

                    val groups = state.groups
                    if (groups.isNotEmpty()) {
                        item {
                            AppHeaderItem(title = R.string.invite_new_groups)
                        }
                        items(groups) { group ->
                            InviteItem(
                                title = group.name,
                                subtitle = null,
                                avatar = "",
                                access = state.idAccessList[group.id] ?: Access.None,
                                accessList = accessList.minus(
                                    arrayOf(
                                        Access.RoomManager,
                                        Access.ContentCreator
                                    ).toSet(),
                                ),
                                onSetAccess = onSetAccess,
                                value = group.id
                            )
                        }
                    }
                }
            }
            UserListBottomContent(
                nextButtonTitle = R.string.share_invite_title,
                accessList = accessList.run {
                    takeIf { state.groups.isEmpty() } ?: minus(
                        listOf(
                            Access.RoomManager,
                            Access.ContentCreator
                        ).toSet()
                    )
                },
                count = null,
                access = state.access,
                onDelete = null,
                onAccess = onSetAllAccess,
                onNext = onNext
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun <T> InviteItem(
    title: String,
    subtitle: String?,
    avatar: String?,
    access: Access,
    accessList: List<Access>,
    value: T,
    onSetAccess: (T, Access) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatar != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
                    .clip(CircleShape)
                    .size(40.dp)
            ) {
                if (avatar.isNotEmpty()) {
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
                            .background(colorResource(id = lib.toolkit.base.R.color.colorIconBackground))
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = StringUtils.getAvatarName(title),
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.colorTextSecondary
                        )
                    }
                }
            }
        } else {
            Image(
                modifier = Modifier.padding(end = 16.dp),
                painter = painterResource(R.drawable.ic_account_placeholder),
                contentDescription = null
            )
        }
        Column {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.body1,
                        maxLines = 1
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.colorTextSecondary
                        )
                    }
                }
                AccessIconButton(
                    access = access,
                    enabled = true,
                    accessList = accessList,
                    onAccess = { access -> onSetAccess.invoke(value, access) }
                )
            }
            AppDivider()
        }
    }
}

@Preview
@Composable
private fun InviteAccessScreenEmailPreview() {
    ManagerTheme {
        val emails = Array(5) { "email@email $it" }
        MainScreen(
            state = InviteAccessState(
                access = Access.Read,
                emails = emails.toList(),
                idAccessList = emails.associateWith { Access.Read }
            ),
            accessList = listOf(),
            onSetAccess = { _, _ -> },
            onBack = {},
            onSetAllAccess = {},
            onNext = {}
        )
    }
}


@Preview
@Composable
private fun InviteAccessScreenUsersPreview() {
    ManagerTheme {
        val users = Array(5) {
            User(
                displayName = "user $it",
                avatarMedium = "qwe",
                email = "email@email $it",
            )
        }.toList()
        val groups = Array(5) { Group(name = "group $it") }.toList()
        val accessList = users.map(User::id)
            .plus(groups.map(Group::id))
            .associateWith { Access.Read }

        MainScreen(
            InviteAccessState(
                access = Access.Read,
                users = users,
                groups = groups,
                idAccessList = accessList
            ),
            description = "Guests, Users and Groups cannot be assigned as Room managers. Only Room and DocSpace admins are suitable for the specified role.",
            accessList = listOf(),
            onSetAccess = { _, _ -> },
            onBack = {},
            onSetAllAccess = {},
            onNext = {}
        )
    }
}