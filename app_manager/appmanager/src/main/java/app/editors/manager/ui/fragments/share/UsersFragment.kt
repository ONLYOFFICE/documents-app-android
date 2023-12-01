package app.editors.manager.ui.fragments.share

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import app.documents.core.network.login.models.User
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.UserViewModelFactory
import app.editors.manager.viewModels.main.UsersViewModel
import app.editors.manager.viewModels.main.UsersViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.editors.gbase.ui.views.compose.link.clickable
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt

class UsersFragment : BaseAppFragment() {

    companion object {
        val TAG: String = UsersFragment::class.java.simpleName

        fun newInstance(item: Item?): UsersFragment {
            return UsersFragment().apply {
                arguments = Bundle(1).apply {
                    putSerializable(ShareActivity.TAG_SHARE_ITEM, item)
                }
            }
        }
    }

    private val item: CloudFolder?
        get() = arguments?.getSerializableExt(ShareActivity.TAG_SHARE_ITEM)

    private val viewModel by viewModels<UsersViewModel> {
        UserViewModelFactory(
            id = item?.id ?: "",
            shareService = requireContext().shareApi,
            roomProvider = requireContext().roomProvider,
            resourcesProvider = requireContext().appComponent.resourcesProvider,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setContent {
            val isSearchVisible = remember {
                mutableStateOf(false)
            }

            val search = remember {
                mutableStateOf("")
            }

            val scaffoldState = rememberScaffoldState()

            ManagerTheme {
                AppScaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Crossfade(targetState = isSearchVisible, label = "") {
                            when (it.value) {
                                false -> {
                                    AppTopBar(
                                        title = stringResource(id = R.string.room_set_owner_title),
                                        isClose = true,
                                        backListener = { requireActivity().finish() },
                                        actions = {
                                            Icon(
                                                imageVector = Icons.Outlined.Search,
                                                contentDescription = stringResource(id = android.R.string.search_go),
                                                modifier = Modifier.clickable(noRipple = false) {
                                                    isSearchVisible.value = true
                                                })
                                        })
                                }

                                true -> {
                                    SearchAppBar(
                                        text = search,
                                        onTextChange = { value ->
                                            search.value = value
                                            viewModel.search(value)
                                        }, onCloseClicked = {
                                            isSearchVisible.value = false
                                        })
                                }
                            }
                        }

                    }
                ) {
                    MainScreen(viewModel.usersFlow, viewModel.viewState) {
                        requireActivity().finish()
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    users: StateFlow<List<User>>,
    usersViewState: StateFlow<UsersViewState>,
    onSuccess: () -> Unit
) {
    val usersList = users.collectAsState().value

    when (val viewState = usersViewState.collectAsState().value) {
        is UsersViewState.Error -> {
            UiUtils.getSnackBar(LocalView.current).setText(viewState.message).show()
        }

        UsersViewState.Loading -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        UsersViewState.None -> {
            Spacer(modifier = Modifier.height(4.dp))

        }

        is UsersViewState.Success -> {
            onSuccess()
        }
    }

    LazyColumn {
        items(items = usersList, key = { it.id }) {
            Text(text = it.displayName)
        }
    }
}

@Composable
fun SearchAppBar(
    text: MutableState<String>,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        AppTextField(
            state = text,
            onValueChange = onTextChange,
            focusManager = focusManager,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.value.isNotEmpty()) {
                            onTextChange("")
                        } else {
                            onCloseClicked()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = android.R.string.cancel),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }, label = R.string.share_title_search, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = android.R.string.search_go),
                    modifier = Modifier.alpha(ContentAlpha.medium)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            onDone = { focusManager.clearFocus(true) }
        )
    }
}

@Preview
@Composable
private fun PreviewSearch() {
    ManagerTheme {
        SearchAppBar(text = remember {
            mutableStateOf("")
        }, onTextChange = {

        }) {

        }
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Preview
@Composable
private fun PreviewMain() {
    ManagerTheme {
        MainScreen(
            users = MutableStateFlow(
                listOf(
                    User().copy(displayName = "User", id = "id"),
                    User().copy(displayName = "User", id = "id1"),
                    User().copy(displayName = "User", id = "id2"),
                    User().copy(displayName = "User", id = "id3"),
                    User().copy(displayName = "User", id = "id4")
                )
            ),
            usersViewState = MutableStateFlow(UsersViewState.None)
        ) {

        }
    }
}