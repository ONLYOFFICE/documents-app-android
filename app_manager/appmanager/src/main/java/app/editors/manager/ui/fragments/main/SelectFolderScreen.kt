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
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.documents.core.network.manager.models.explorer.PathPart
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.ManagerUiUtils
import app.editors.manager.viewModels.main.SelectFolderViewModel
import com.google.gson.Gson
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
    val viewModel = viewModel { SelectFolderViewModel(App.getApp().coreComponent.managerRepository) }
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
    val explorer = """
        {
        "files": [
            {
                "folderId": "dropbox-337",
                "version": 1,
                "versionGroup": 1,
                "contentLength": "7.37 KB",
                "pureContentLength": 7543,
                "fileStatus": 0,
                "mute": false,
                "viewUrl": "https://testdocs1.onlyoffice.io/filehandler.ashx?action=download&fileid=dropbox-337-L9Cd0L7QstGL0Lkg0LTQvtC60YPQvC5kb2N4",
                "webUrl": "https://testdocs1.onlyoffice.io/doceditor?fileid=dropbox-337-L9Cd0L7QstGL0Lkg0LTQvtC60YPQvC5kb2N4&version=1",
                "fileType": 7,
                "fileExst": ".docx",
                "thumbnailUrl": "https://testdocs1.onlyoffice.io/filehandler.ashx?action=thumb&fileid=dropbox-337-L9Cd0L7QstGL0Lkg0LTQvtC60YPQvC5kb2N4&version=1&hash=1885173712",
                "thumbnailStatus": 1,
                "denyDownload": false,
                "denySharing": false,
                "viewAccessibility": {
                    "ImageView": false,
                    "MediaView": false,
                    "WebView": true,
                    "WebEdit": true,
                    "WebReview": true,
                    "WebCustomFilterEditing": false,
                    "WebRestrictedEditing": false,
                    "WebComment": true,
                    "CoAuhtoring": true,
                    "CanConvert": true,
                    "MustConvert": false
                },
                "id": "dropbox-337-L9Cd0L7QstGL0Lkg0LTQvtC60YPQvC5kb2N4",
                "rootFolderId": "21364",
                "canShare": false,
                "security": {
                    "Read": true,
                    "Comment": true,
                    "FillForms": true,
                    "Review": true,
                    "Edit": true,
                    "Delete": true,
                    "CustomFilter": true,
                    "Rename": true,
                    "ReadHistory": false,
                    "Lock": true,
                    "EditHistory": false,
                    "Copy": true,
                    "Move": true,
                    "Duplicate": true,
                    "SubmitToFormGallery": false,
                    "Download": true,
                    "Convert": true
                },
                "title": "Новый докум.docx",
                "access": 0,
                "shared": false,
                "created": "2022-11-14T14:26:22.0000000+03:00",
                "createdBy": {
                    "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                    "displayName": "Test Infp",
                    "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                    "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                    "hasAvatar": true
                },
                "updated": "2022-11-14T14:26:22.0000000+03:00",
                "rootFolderType": 14,
                "updatedBy": {
                    "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                    "displayName": "Test Infp",
                    "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                    "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                    "hasAvatar": true
                },
                "providerItem": true,
                "providerKey": "DropboxV2",
                "providerId": 337
            }
        ],
        "folders": [
            {
                "parentId": "dropbox-337",
                "filesCount": 0,
                "foldersCount": 0,
                "new": 0,
                "mute": false,
                "pinned": false,
                "private": false,
                "id": "dropbox-337-LzEyMw",
                "rootFolderId": "21364",
                "canShare": false,
                "security": {
                    "Read": true,
                    "Create": true,
                    "Delete": true,
                    "EditRoom": true,
                    "Rename": true,
                    "CopyTo": true,
                    "Copy": true,
                    "MoveTo": true,
                    "Move": true,
                    "Pin": true,
                    "Mute": true,
                    "EditAccess": true,
                    "Duplicate": true,
                    "Download": true,
                    "CopySharedLink": true,
                    "Reconnect": true
                },
                "title": "123",
                "access": 0,
                "shared": false,
                "created": "0001-01-01T00:00:00.0000000Z",
                "createdBy": {
                    "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                    "displayName": "Test Infp",
                    "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                    "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                    "hasAvatar": true
                },
                "updated": "0001-01-01T00:00:00.0000000Z",
                "rootFolderType": 14,
                "updatedBy": {
                    "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                    "displayName": "Test Infp",
                    "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                    "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                    "hasAvatar": true
                },
                "providerItem": true,
                "providerKey": "DropboxV2",
                "providerId": 337
            }
        ],
        "current": {
            "filesCount": 0,
            "foldersCount": 0,
            "new": 0,
            "mute": false,
            "pinned": false,
            "private": false,
            "id": "dropbox-337",
            "rootFolderId": "21364",
            "canShare": false,
            "security": {
                "Read": true,
                "Create": true,
                "Delete": true,
                "EditRoom": true,
                "Rename": true,
                "CopyTo": true,
                "Copy": true,
                "MoveTo": true,
                "Move": true,
                "Pin": true,
                "Mute": true,
                "EditAccess": true,
                "Duplicate": true,
                "Download": true,
                "CopySharedLink": true,
                "Reconnect": true
            },
            "title": "Dropbox",
            "access": 0,
            "shared": false,
            "created": "2024-04-10T13:57:11.0000000+03:00",
            "createdBy": {
                "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                "displayName": "Test Infp",
                "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                "hasAvatar": true
            },
            "updated": "2024-04-10T13:57:11.0000000+03:00",
            "rootFolderType": 14,
            "updatedBy": {
                "id": "ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38",
                "displayName": "Test Infp",
                "avatarSmall": "/storage/userPhotos/root/ab2b71b5-6c07-4bdb-a0d4-47bde65c6a38_size_32-32.png?hash=2100898975",
                "profileUrl": "https://testdocs1.onlyoffice.io/accounts/view/wertyjokas",
                "hasAvatar": true
            },
            "providerItem": true,
            "providerKey": "DropboxV2",
            "providerId": 337
        },
        "pathParts": [
            {
                "id": "dropbox-337",
                "title": "Dropbox"
            }
        ],
        "startIndex": 0,
        "count": 9,
        "total": 9,
        "new": 0
    }"""

    val state = rememberSaveable {
        Gson().fromJson(explorer, Explorer::class.java)
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