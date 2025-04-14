package app.editors.manager.ui.fragments.main.versionhistory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.mvp.models.ui.FileVersionUi
import app.editors.manager.mvp.models.ui.ResultUi
import app.editors.manager.ui.dialogs.explorer.ExplorerContextItem
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.BaseEvent
import app.editors.manager.viewModels.main.DialogState
import app.editors.manager.viewModels.main.VersionHistoryEvent
import app.editors.manager.viewModels.main.VersionHistoryState
import app.editors.manager.viewModels.main.VersionHistoryViewModel
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.toolkit.base.managers.utils.CreateDocument
import lib.toolkit.base.managers.utils.TimeUtils
import java.util.Date


@Composable
fun VersionHistoryScreen(
    viewModel: VersionHistoryViewModel,
    showDownloadFolderActivity: (Uri) -> Unit,
    goToEditComment: (FileVersionUi) -> Unit,
    onBack: () -> Unit
){
    val downloadActivityResult = rememberLauncherForActivityResult(CreateDocument()) { uri ->
        uri?.let { viewModel.startDownloadWork(uri) }
    }
    val scaffoldState = rememberScaffoldState()
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.events) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
            viewModel.events.collect { event ->
                when(event){
                    is VersionHistoryEvent.RequestDocumentCreation -> {
                        downloadActivityResult.launch(event.fileName)
                    }
                    is VersionHistoryEvent.DownloadSuccessfully -> {
                        showActionSnackbar(scaffoldState.snackbarHostState, event.msg, event.buttonText, onPerformed = {
                            event.uri?.let { showDownloadFolderActivity(it) }
                        })
                    }
                    is BaseEvent.ShowMessage -> {
                        scaffoldState.snackbarHostState.showSnackbar(event.msg)
                    }
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.version_history_title),
                backListener = onBack
            )
        },
        scaffoldState = scaffoldState,
        useTablePaddings = false
    ) {
        VersionHistoryScreenContent(
            state = state,
            setSelectedItem = viewModel::setSelectedItem,
            onContextMenuItemClick = viewModel::handleContextMenuAction,
            onConfirmDialog = viewModel::onConfirmDialog,
            onDismissDialog = viewModel::onDismissDialog,
            goToEditComment = goToEditComment
        )
    }
}

@Composable
fun VersionHistoryScreenContent(
    state: VersionHistoryState,
    setSelectedItem: (FileVersionUi) -> Unit,
    onContextMenuItemClick: (ExplorerContextItem) -> Unit,
    goToEditComment: (FileVersionUi) -> Unit,
    onConfirmDialog: (ExplorerContextItem) -> Unit,
    onDismissDialog: () -> Unit
){
    when(state.historyResult){
        ResultUi.Loading -> LoadingPlaceholder()
        is ResultUi.Success -> VersionHistoryScreenContent(
            history = state.historyResult.data,
            currentItem = state.currentItem,
            setSelectedItem = setSelectedItem,
            onContextMenuItemClick = onContextMenuItemClick,
            goToEditComment = goToEditComment
        )
        is ResultUi.Error -> {
            PlaceholderView(
                image = null,
                title = stringResource(R.string.placeholder_connection),
                subtitle = ""
            )
        }
    }

    when(state.dialogState){
        DialogState.Hidden -> {}
        is DialogState.Loading -> {
            LoadingDialog(state.dialogState.item)
        }
        is DialogState.Idle -> {
            ConfirmationDialog(
                item = state.dialogState.item,
                onConfirm = onConfirmDialog,
                onDismiss = onDismissDialog
            )
        }
    }
}

@Composable
fun VersionHistoryScreenContent(
    history: Map<String, List<FileVersionUi>>,
    currentItem: FileVersionUi?,
    setSelectedItem: (FileVersionUi) -> Unit,
    onContextMenuItemClick: (ExplorerContextItem) -> Unit,
    goToEditComment: (FileVersionUi) -> Unit
){
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    sheetState.currentValue

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            currentItem?.let {
                BottomSheetContextMenu(
                    currentVersionItem = currentItem,
                    onContextMenuItemClick = { item ->
                        scope.launch { sheetState.hide() }
                        onContextMenuItemClick(item)
                    },
                    goToEditComment = {
                        scope.launch { sheetState.hide() }
                        goToEditComment(currentItem)
                    }
                )
            }
        },
        sheetShape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        scrimColor = if (!isSystemInDarkTheme()) {
            ModalBottomSheetDefaults.scrimColor
        } else {
            MaterialTheme.colors.background.copy(alpha = 0.60f)
        }
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            history.entries.forEachIndexed { indexGroup, (versionGroup, versions) ->
                item {
                    val title = if (indexGroup == 0) stringResource(R.string.version_group_current_header, versionGroup)
                    else stringResource(R.string.version_group_header, versionGroup)
                    AppHeaderItem(title)
                    versions.forEachIndexed { indexVersion, version ->
                        VersionItem(
                            item = version,
                            onItemClick = { item ->
                                setSelectedItem(item)
                                onContextMenuItemClick(ExplorerContextItem.Open)
                            },
                            onContextMenuClick = { item ->
                                setSelectedItem(if (indexGroup == 0 && indexVersion == 0) item.copy(isCurrentVersion = true) else item)
                                scope.launch { sheetState.show() }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VersionItem(
    item: FileVersionUi,
    modifier: Modifier = Modifier,
    onItemClick: ((FileVersionUi) -> Unit)? = null,
    onContextMenuClick: ((FileVersionUi) -> Unit)? = null
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .clickable { onItemClick?.invoke(item) }
    ) {
        Image(
            painter = painterResource(ManagerUiUtils.getFileThumbnail(item.fileExst, isGrid = onContextMenuClick == null)),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(start = 12.dp)
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                    Text(
                        text = TimeUtils.formatVersionDate(item.date),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    Text(
                        text = item.initiatorDisplayName,
                        style = MaterialTheme.typography.caption
                    )

                    Text(
                        text = item.comment,
                        style = MaterialTheme.typography.caption
                    )
                }
                onContextMenuClick?.let {
                    IconButton(onClick = { onContextMenuClick(item) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_list_context_button),
                            contentDescription = stringResource(R.string.context_menu_desc),
                            tint = MaterialTheme.colors.colorTextSecondary
                        )
                    }
                }
            }
            AppDivider()
        }
    }
}

suspend fun showActionSnackbar(
    snackbarHostState: SnackbarHostState,
    msg: String,
    actionLabel: String,
    duration: SnackbarDuration = SnackbarDuration.Long,
    onPerformed: () -> Unit,
    onDismiss: (() -> Unit)? = null,
){
    val result = snackbarHostState.showSnackbar(
        message = msg,
        actionLabel = actionLabel,
        duration = duration
    )
    when (result) {
        SnackbarResult.ActionPerformed -> onPerformed()
        SnackbarResult.Dismissed -> onDismiss?.invoke()
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
@Composable
fun VersionHistoryScreenContentPreview(){
    val item = FileVersionUi(
        version = 1,
        versionGroup = "1",
        fileId = "",
        date = Date(12312312),
        initiatorDisplayName = "John Krasinski",
        comment = "Edited",
        fileExst = "docx",
        viewUrl = "",
        title = "",
        isCurrentVersion = true,
        editAccess = true,
        file = CloudFile()
    )
    ManagerTheme {
        VersionHistoryScreenContent(
            goToEditComment = {},
            onConfirmDialog = {},
            onDismissDialog = {},
            setSelectedItem = {},
            onContextMenuItemClick =  {},
            state = VersionHistoryState(
                historyResult = ResultUi.Success(mapOf(("1" to listOf(item, item))))
            )
        )
    }
}
