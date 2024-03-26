package app.editors.manager.ui.fragments.share

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import app.documents.core.model.login.User
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
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

    private val item: CloudFolder
        get() = checkNotNull(arguments?.getSerializableExt(ShareActivity.TAG_SHARE_ITEM))

    private val viewModel by viewModels<UsersViewModel> {
        UserViewModelFactory(
            id = item,
            shareService = requireContext().shareApi,
            roomProvider = requireContext().roomProvider,
            resourcesProvider = requireContext().appComponent.resourcesProvider,
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (requireActivity() is ShareActivity) {
            (requireActivity() as ShareActivity).findViewById<AppBarLayout>(R.id.app_bar_layout).isVisible = false
        }

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
                        AnimatedVisibility(
                            visible = !isSearchVisible.value,
                            enter = fadeIn() + slideInHorizontally(),
                            exit = fadeOut() + slideOutHorizontally()
                        ) {
                            AppTopBar(
                                title = stringResource(id = R.string.room_set_owner_title),
                                isClose = true,
                                backListener = { requireActivity().finish() },
                                actions = {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = stringResource(id = android.R.string.search_go),
                                        modifier = Modifier.clickable {
                                            isSearchVisible.value = true
                                        })
                                })
                        }
                        AnimatedVisibility(visible = isSearchVisible.value, enter = fadeIn(), exit = fadeOut()) {
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
                ) {
                    MainScreen(viewModel.usersFlow, viewModel.viewState, {
                        viewModel.setOwner(it.id)
                    }) {
                        requireActivity().finish()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreen(
    usersFlow: StateFlow<List<User>>,
    usersViewState: StateFlow<UsersViewState>,
    click: (user: User) -> Unit,
    onSuccess: () -> Unit
) {
    val usersList = usersFlow.collectAsState().value
    val listState = rememberLazyListState()
    val groupUser = usersList.groupBy { it.displayName.first().uppercaseChar() }.toSortedMap()

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

    if (usersList.isNotEmpty()) {
        LazyColumn(state = listState) {
            groupUser.keys.forEach { key ->
                //TODO Maybe use sticky header??? Or delete header???
//                stickyHeader(key = key) {
//                        Text(
//                            text = key.toString(),
//                            style = MaterialTheme.typography.h5,
//                            color = MaterialTheme.colors.primary,
//                            modifier = Modifier.padding(8.dp)
//                        )
//                    Spacer(modifier = Modifier.height((-64).dp))
//                }
                itemsIndexed(groupUser[key] ?: emptyList(), key = { _, user -> user.id }) { index, user ->
                    Row(modifier = Modifier
                        .animateItemPlacement()
                        .clickable { click.invoke(user) }
                    ) {
                        if (index == 0) {
                            Text(
                                text = key.toString(),
                                style = MaterialTheme.typography.h5,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(48.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                        UserItem(user = user)
                    }

                }
            }
//            itemsIndexed(items = usersList, key = { _, user -> user.id }) { _, user ->
//                UserItem(
//                    user = user,
//                    click
//                )
//            }
        }
    } else {
        PlaceholderView(
            image = lib.toolkit.base.R.drawable.placeholder_not_found,
            title = stringResource(id = R.string.room_search_not_found),
            subtitle = stringResource(id = R.string.room_search_not_found_desc)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserItem(user: User) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(vertical = 8.dp)
    ) {
        GlideImage(
            model = user.avatarMedium, contentDescription = null,
            loading = placeholder(R.drawable.ic_empty_image),
            failure = placeholder(R.drawable.ic_empty_image),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .size(48.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
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
@Preview(showSystemUi = true)
@Composable
private fun PreviewMain() {
    ManagerTheme {
        MainScreen(
            usersFlow = MutableStateFlow(
                listOf(
                    User().copy(displayName = "user", id = "id", email = "email"),
                    User().copy(displayName = "User", id = "id1", email = "email"),
                    User().copy(displayName = "Mike", id = "id2", email = "email"),
                    User().copy(displayName = "mike", id = "id3", email = "email"),
                    User().copy(displayName = "User", id = "id4", email = "email"),
                    User().copy(displayName = "123", id = "id5", email = "email"),
                    User().copy(displayName = "5mike", id = "id6", email = "email")
                )
            ),
            usersViewState = MutableStateFlow(UsersViewState.None), {

            }
        ) {

        }
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Preview
@Composable
private fun PreviewEmptyMain() {
    ManagerTheme {
        MainScreen(
            usersFlow = MutableStateFlow(
                emptyList()
            ),
            usersViewState = MutableStateFlow(UsersViewState.None), {

            }
        ) {

        }
    }
}