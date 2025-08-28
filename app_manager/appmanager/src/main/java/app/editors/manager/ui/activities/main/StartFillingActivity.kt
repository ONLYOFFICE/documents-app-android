package app.editors.manager.ui.activities.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.documents.core.model.login.User
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.roomProvider
import app.editors.manager.managers.tools.BaseEvent
import app.editors.manager.managers.utils.GlideAvatarImage
import app.editors.manager.ui.fragments.main.FillingStatusMode
import app.editors.manager.ui.fragments.main.FillingStatusRoute
import app.editors.manager.ui.fragments.share.InviteUsersScreen
import app.editors.manager.ui.fragments.share.UserListScreen
import app.editors.manager.viewModels.main.FillingStatusViewModel
import app.editors.manager.viewModels.main.RoomUserListViewModel
import app.editors.manager.viewModels.main.StartFillingEvent
import app.editors.manager.viewModels.main.StartFillingState
import app.editors.manager.viewModels.main.StartFillingViewModel
import app.editors.manager.viewModels.main.UserListMode
import kotlinx.serialization.Serializable
import lib.compose.ui.theme.BaseAppTheme
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.compose.ui.theme.colorTextPrimary
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.NestedColumn
import lib.compose.ui.views.TopAppBarAction
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.FormRole
import lib.toolkit.base.managers.utils.FormRoleList
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.capitalize

private sealed class Screen {

    @Serializable
    data object Main : Screen()

    @Serializable
    data object InviteToRoom : Screen()

    @Serializable
    data object FillingStatus : Screen()

    @Serializable
    data class UserList(val index: Int) : Screen()
}

class StartFillingActivity : ComponentActivity() {

    private val formRoles: List<Pair<FormRole, User?>> by lazy {
        val formRoles = FormRoleList
            .fromJson(intent.getStringExtra(EditorsContract.EXTRA_FORM_ROLES))

        formRoles.map { it to null }
    }

    private val roomId: String by lazy {
        intent.getStringExtra(EditorsContract.EXTRA_ROOM_ID).orEmpty()
    }

    private val formId: String by lazy {
        intent.getStringExtra(EditorsContract.EXTRA_ITEM_ID).orEmpty()
    }

    private val isComplete: Boolean by lazy {
        intent.getBooleanExtra(EditorsContract.EXTRA_START_FILLING_COMPLETE, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseAppTheme(
                primaryColor = getThemeColor()
            ) {
                CompositionLocalProvider(LocalUseTabletPadding provides true) {
                    val navController = rememberNavController()
                    val viewModel = viewModel {
                        StartFillingViewModel(
                            roomProvider = roomProvider,
                            roomId = roomId,
                            formId = formId,
                            formRoles = formRoles,
                            resourcesProvider = appComponent.resourcesProvider,
                        )
                    }
                    val state = viewModel.state.collectAsState()
                    val lifecycleOwner = LocalLifecycleOwner.current

                    LaunchedEffect(viewModel.events) {
                        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            viewModel.events.collect { event ->
                                when (event) {
                                    is BaseEvent.ShowMessage -> {
                                        UiUtils.getSnackBar(this@StartFillingActivity)
                                            .setText(event.msg)
                                            .show()
                                    }

                                    is StartFillingEvent.Success -> {
                                        setResult(RESULT_OK)
                                        finish()
                                    }
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = if (isComplete) Screen.FillingStatus else Screen.Main
                    ) {
                        composable<Screen.Main> {
                            StartFillingScreen(
                                state = state.value,
                                onStart = viewModel::startFilling,
                                onClose = ::finish,
                                onDeleteClick = viewModel::deleteUser,
                                onAddClick = { index ->
                                    navController.navigate(Screen.UserList(index))
                                }
                            )
                        }
                        composable<Screen.UserList> { backStackEntry ->
                            val index = backStackEntry.toRoute<Screen.UserList>().index
                            val userListViewModel = viewModel {
                                RoomUserListViewModel(
                                    roomId = roomId,
                                    mode = UserListMode.StartFilling,
                                    roomProvider = roomProvider,
                                    resourcesProvider = appComponent.resourcesProvider
                                )
                            }

                            LaunchedEffect(Unit) {
                                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                    userListViewModel.refreshMembers()
                                }
                            }

                            UserListScreen(
                                viewModel = userListViewModel,
                                title = R.string.setting_select_members_title,
                                closeable = false,
                                onBack = navController::popBackStack,
                                useTabletPaddings = true,
                                onClick = { user ->
                                    if (user is User) {
                                        viewModel.setUser(index, user)
                                        navController.popBackStack()
                                    }
                                },
                                onSnackBar = ::onSnackBar,
                                topBarActions = {
                                    TopAppBarAction(
                                        icon = R.drawable.ic_add_users
                                    ) {
                                        navController.navigate(Screen.InviteToRoom)
                                    }
                                }
                            )
                        }
                        composable<Screen.InviteToRoom> {
                            InviteUsersScreen(
                                roomType = -1,
                                roomId = roomId,
                                roomProvider = roomProvider,
                                fromList = true,
                                onSnackBar = ::onSnackBar,
                                onBack = navController::popBackStack,
                            )
                        }
                        composable<Screen.FillingStatus> {
                            val fillingStatusViewModel = viewModel<FillingStatusViewModel> {
                                FillingStatusViewModel(
                                    formId = formId,
                                    cloudFileProvider = cloudFileProvider
                                )
                            }

                            BackHandler(onBack = ::finish)

                            FillingStatusRoute(
                                fillingStatusMode = FillingStatusMode.StartFilling,
                                viewModel = fillingStatusViewModel,
                                onBack = ::finish,
                                onFillClick = {
                                    setResult(EditorsContract.RESULT_FILL_FORM)
                                    finish()
                                },
                                onSnackBar = {
                                    UiUtils.getSnackBar(this@StartFillingActivity)
                                        .setText(it)
                                        .show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onSnackBar(text: String) {
        UiUtils.getSnackBar(this@StartFillingActivity)
            .setText(text)
            .show()
    }

    private fun getThemeColor(): Color {
        return intent?.getIntExtra(EditorsContract.EXTRA_THEME_COLOR, -1)?.let { Color(it) }
            ?: Color(getColor(lib.toolkit.base.R.color.colorPrimary))
    }
}

@Composable
private fun StartFillingScreen(
    modifier: Modifier = Modifier,
    state: StartFillingState,
    onAddClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = R.string.start_filling_title,
                backListener = onClose,
                isClose = true
            )
        }
    ) {
        Column {
            AnimatedVisibility(state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            NestedColumn(
                modifier = Modifier.weight(1f)
            ) {
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.start_filling_desc
                )
                AppHeaderItem(
                    title = R.string.start_filling_header
                )
                state.rolesWithUsers.forEachIndexed { index, (role, user) ->
                    RoleItem(
                        enabled = !state.isLoading,
                        index = index + 1,
                        roleName = role.name,
                        roleColor = Color(role.color),
                        user = user,
                        onAddClick = { onAddClick(index) },
                        onDeleteClick = { onDeleteClick(index) }
                    )
                }
            }
            Column {
                AppDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppTextButton(
                        title = stringResource(R.string.dialogs_common_cancel_button).capitalize(),
                        onClick = onClose
                    )
                    AppTextButton(
                        enabled = state.rolesWithUsers.none { it.second == null } && !state.isLoading,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = R.string.start_filling_start,
                        onClick = onStart
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleItem(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    index: Int,
    roleName: String,
    roleColor: Color,
    user: User?,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = index.toString())
        }
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (user != null) {
                GlideAvatarImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp),
                    url = user.avatarMedium
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                        .background(roleColor)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_add),
                        contentDescription = null,
                        tint = MaterialTheme.colors.colorTextSecondary
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            if (user != null) {
                Text(
                    text = user.displayNameFromHtml
                )
            }
            Text(
                text = roleName,
                fontSize = if (user == null) 16.sp else 14.sp,
                color = if (user == null)
                    MaterialTheme.colors.colorTextPrimary else
                    MaterialTheme.colors.colorTextSecondary
            )
        }
        if (user != null) {
            IconButton(
                enabled = enabled,
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_close),
                    contentDescription = null,
                    tint = MaterialTheme.colors.colorTextSecondary
                )
            }
        }
    }
}

@Preview
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun StartFillingScreenPreview() {
    BaseAppTheme(
        primaryColor = colorResource(lib.toolkit.base.R.color.colorPdfTint)
    ) {
        StartFillingScreen(
            state = StartFillingState(
                rolesWithUsers = List(15) {
                    FormRole("Role name", Color.Green.toArgb(), 0) to if (it == 0) {
                        User(displayName = "Username")
                    } else {
                        null
                    }
                },
                isLoading = false
            ),
            onClose = {},
            onStart = {},
            onAddClick = {},
            onDeleteClick = {}
        )
    }
}