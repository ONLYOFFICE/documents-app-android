package app.editors.manager.ui.fragments.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.PathPart
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.viewModels.main.SelectFolderViewModel
import lib.compose.ui.enabled
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.compose.ui.views.TopAppBarAction

@Composable
fun SelectFolderScreen(
    folderId: String,
    onBack: () -> Unit,
    onAccept: (List<PathPart>) -> Unit
) {
    val viewModel = viewModel { SelectFolderViewModel(App.getApp().appComponent.managerRepository) }
    val state by viewModel.state.collectAsState()

    BackHandler {
        if (!viewModel.backToPrevious()) {
            onBack.invoke()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openFolder(folderId)
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = {
                    Column {
                        Text(text = stringResource(id = R.string.operation_title))
                        Text(
                            text = if (state.pathParts.isEmpty()) {
                                "/"
                            } else {
                                state.pathParts
                                    .toMutableList()
                                    .also { it[0] = PathPart("", "") }
                                    .joinToString("") { "${it.title}/" }
                            },
                            style = MaterialTheme.typography.caption
                        )
                    }
                },
                backListener = { if (!viewModel.backToPrevious()) onBack.invoke() },
                actions = {
                    if (!state.loading) {
                        TopAppBarAction(icon = R.drawable.drawable_ic_done) {
                            onAccept.invoke(state.pathParts)
                            onBack.invoke()
                        }
                    }
                }
            )
        }
    ) {
        if (state.loading) {
            ActivityIndicatorView(stringResource(id = R.string.placeholder_loading_files))
        } else if (state.items.isEmpty()) {
            PlaceholderView(
                image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
                title = stringResource(id = R.string.placeholder_empty),
                subtitle = ""
            )
        } else {
            MainScreen(
                items = state.items,
                pathParts = state.pathParts,
                title = state.title,
                onFolderClick = viewModel::openFolder
            )
        }
    }
}

@Composable
private fun MainScreen(
    items: List<Item>,
    pathParts: List<PathPart>,
    title: String,
    onFolderClick: (id: String) -> Unit
) {
    LazyColumn {
        items(items) {
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .enabled(it !is CloudFile)
                    .clickable(
                        enabled = it !is CloudFile,
                        onClick = { onFolderClick.invoke(it.id) }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.padding(16.dp),
                    imageVector = ImageVector.vectorResource(ManagerUiUtils.getIcon(it)),
                    contentDescription = null
                )
                Column {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = it.title)
                    }
                    AppDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectFolderScreenPreview() {

    val file = CloudFile().apply { title = "Document.docx" }
    val state = rememberSaveable {
        Explorer(
            folders = mutableListOf(
                CloudFolder().apply { title = "Folder 1" },
                CloudFolder().apply { title = "Folder 2" }
            ),
            files = mutableListOf(
                file,
                file
            )
        )
    }

    ManagerTheme {
        AppScaffold {
            MainScreen(
                state.folders + state.files,
                state.pathParts,
                state.current.title
            ) {}
        }
    }
}