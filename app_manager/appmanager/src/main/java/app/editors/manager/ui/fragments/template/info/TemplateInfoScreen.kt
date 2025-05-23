package app.editors.manager.ui.fragments.template.info

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.documents.core.model.login.Group
import app.documents.core.model.login.User
import app.editors.manager.R
import app.editors.manager.managers.tools.BaseEvent
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.mvp.models.ui.ResultUi
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.ui.fragments.template.settings.PublicBlock
import app.editors.manager.ui.fragments.template.settings.SelectedMembersList
import app.editors.manager.ui.views.custom.UserListBottomContent
import app.editors.manager.viewModels.main.TemplateAccessSettings
import app.editors.manager.viewModels.main.TemplateInfoState
import app.editors.manager.viewModels.main.TemplateInfoViewModel
import app.editors.manager.viewModels.main.TemplateSettingsEvent
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction

@Composable
fun TemplateInfoScreen(
    viewModel: TemplateInfoViewModel,
    showSnackbar: (String) -> Unit,
    navigateToAccessSettings: () -> Unit,
    navigateToCreated: (id: String?, type: Int?, title: String?) -> Unit,
    onBack: () -> Unit
) {
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is BaseEvent.ShowMessage -> showSnackbar(event.msg)
                    is TemplateSettingsEvent.Created -> navigateToCreated(
                        event.id,
                        state.roomType,
                        null
                    )
                }
            }
        }
    }

    TemplateInfoScreenContent(
        state = state,
        loadingStatus = loadingStatus,
        currentUser = viewModel.getCurrentUser(),
        onCreateRoom = viewModel::save,
        onBack = onBack,
        navigateToAccessSettings = navigateToAccessSettings
    )
}

@Composable
private fun TemplateInfoScreenContent(
    state: TemplateInfoState,
    loadingStatus: ResultUi<*>,
    currentUser: User,
    onCreateRoom: () -> Unit,
    navigateToAccessSettings: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            Column {
                AppTopBar(
                    title = {
                        Column {
                            Text(state.title.ifEmpty { stringResource(R.string.list_context_info) })
                            if (state.roomType != 0) {
                                Text(
                                    text = stringResource(id = RoomUtils.getRoomInfo(state.roomType).title),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    },
                    actions = {
                        if (state.editAccess) {
                            TopAppBarAction(
                                icon = R.drawable.ic_add_users,
                                onClick = navigateToAccessSettings,
                                enabled = true
                            )
                        }
                    },
                    backListener = onBack
                )
                AnimatedVisibilityVerticalFade(visible = state.isSaving) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        useTablePaddings = false
    ) {
        TemplateInfoScreenContent(
            templateState = state,
            loadingStatus = loadingStatus,
            currentUser = currentUser,
            onCreateRoom = onCreateRoom
        )
    }
}

@Composable
private fun TemplateInfoScreenContent(
    templateState: TemplateInfoState,
    loadingStatus: ResultUi<*>,
    currentUser: User,
    onCreateRoom: () -> Unit
) {
    Column {
        Crossfade(
            targetState = loadingStatus,
            modifier = Modifier.weight(1f)
        ) { state ->
            when (state) {
                ResultUi.Loading -> LoadingPlaceholder()
                is ResultUi.Success -> TemplateInfoScreenContent(
                    templateState.accessSettings,
                    currentUser
                )

                is ResultUi.Error -> {
                    PlaceholderView(
                        image = null,
                        title = stringResource(R.string.placeholder_connection),
                        subtitle = stringResource(R.string.placeholder_connection_desc)
                    )
                }
            }
        }
        UserListBottomContent(
            nextButtonTitle = R.string.dialog_create_room,
            onNext = onCreateRoom
        )
    }
}

@Composable
private fun TemplateInfoScreenContent(
    settings: TemplateAccessSettings,
    currentUser: User
) {
    Column(Modifier.padding(vertical = 8.dp)) {
        AppDescriptionItem(
            text = R.string.desc_info_template,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (settings.public) {
            AccessBlock(expandable = false) {
                PublicBlock(addDivider = false)
            }
        } else {
            AccessBlock {
                SelectedMembersList(
                    currentUser = currentUser,
                    users = settings.selectedUsers,
                    groups = settings.selectedGroups,
                    onDelete = null,
                    paddingValues = PaddingValues(vertical = 0.dp),
                    innerPadding = 8.dp
                )
            }
        }
    }
}

@Composable
private fun AccessBlock(
    expandable: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    Column(Modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.Top) {
            AppHeaderItem(
                title = R.string.setting_access_list_title,
                modifier = Modifier.weight(1f)
            )
            if (expandable) {
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_arrow_down),
                        tint = MaterialTheme.colors.colorTextTertiary,
                        contentDescription = null
                    )
                }
            }
        }
        if (expanded || !expandable) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TemplateInfoScreenContentPreview() {
    ManagerTheme {
        TemplateInfoScreenContent(
            state = TemplateInfoState(
                title = "Template",
                roomType = 1,
                editAccess = true,
                accessSettings = TemplateAccessSettings(
                    public = false,
                    selectedUsers = listOf(
                        User(
                            id = "1u",
                            displayName = "Anokhin Tollan"
                        )
                    ),
                    selectedGroups = listOf(
                        Group(
                            id = "1g",
                            name = "Programming Department"
                        )
                    ),
                )
            ),
            currentUser = User(id = "1", displayName = "John"),
            loadingStatus = ResultUi.Success(Unit),
            onBack = {},
            onCreateRoom = {},
            navigateToAccessSettings = {}
        )
    }
}