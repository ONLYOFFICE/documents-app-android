package app.editors.manager.ui.fragments.share.link

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.share.models.ExternalLink
import app.documents.core.network.share.models.ExternalLinkSharedTo
import app.editors.manager.R
import app.editors.manager.app.roomProvider
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.viewModels.link.ShareSettingsEffect
import app.editors.manager.viewModels.link.ShareSettingsState
import app.editors.manager.viewModels.link.ShareSettingsViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.putArgs

class ShareSettingsFragment : BaseDialogFragment() {

    companion object {

        private const val KEY_FILE_ID = "KEY_FILE_ID"
        private val TAG = ShareSettingsFragment::class.simpleName

        private fun newInstance(): ShareSettingsFragment = ShareSettingsFragment()

        fun show(activity: FragmentActivity, fileId: String?) {
            newInstance()
                .putArgs(KEY_FILE_ID to fileId)
                .show(activity.supportFragmentManager, TAG)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ComponentDialog(
            requireContext(),
            if (!UiUtils.isTablet(requireContext())) R.style.FullScreenDialog else 0
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent {
            ManagerTheme {
                val viewModel = viewModel {
                    ShareSettingsViewModel(
                        requireContext().roomProvider,
                        arguments?.getString(KEY_FILE_ID).orEmpty()
                    )
                }
                val isCreateLoading = remember { mutableStateOf(false) }
                val state by viewModel.state.collectAsState()
                val settingsEffect by viewModel.effect.collectAsState(null)
                val snackBar = remember { UiUtils.getSnackBar(requireView()) }

                when (val effect = settingsEffect) {
                    is ShareSettingsEffect.Copy -> {
                        KeyboardUtils.setDataToClipboard(requireContext(), effect.link)
                        snackBar.setText(R.string.rooms_info_create_link_complete).show()
                    }
                    is ShareSettingsEffect.Error -> {
                        snackBar
                            .setText(
                                effect.code?.let { code ->
                                    getString(R.string.errors_client_error) + code
                                } ?: getString(R.string.errors_unknown_error)
                            )
                            .show()
                    }
                    is ShareSettingsEffect.OnCreate -> {
                        isCreateLoading.value = effect.loading
                    }
                    else -> Unit
                }

                ShareSettingsScreen(
                    state = state,
                    onBack = ::dismiss,
                    isCreateLoading = isCreateLoading,
                    onCreate = viewModel::create
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShareSettingsScreen(
    isCreateLoading: State<Boolean>,
    state: ShareSettingsState,
    onCreate: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(title = R.string.share_title_main, backListener = onBack)
        }
    ) {
        when (state) {
            is ShareSettingsState.Loading -> LoadingPlaceholder()
            is ShareSettingsState.Success -> {
                LazyColumn {
                    item {
                        Row {
                            AppHeaderItem(
                                modifier = Modifier.weight(1f),
                                title = stringResource(id = R.string.rooms_share_shared_links)
                            )
                            if (state.links.isNotEmpty()) {
                                IconButton(onClick = onCreate) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_action_button_docs_add),
                                        tint = MaterialTheme.colors.primary,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                    if (state.links.isNotEmpty()) {
                        items(state.links, key = { it.sharedTo.id }) { link ->
                            SharedLinkItem(
                                modifier = Modifier.animateItemPlacement(),
                                access = link.access,
                                internal = link.sharedTo.internal == true,
                                expirationDate = link.sharedTo.expirationDate,
                                isExpired = link.sharedTo.isExpired,
                                onShareClick = { },
                                onClick = {}
                            )
                        }
                    } else if (!isCreateLoading.value) {
                        item {
                            AppTextButton(
                                modifier = Modifier.padding(start = 8.dp),
                                title = R.string.rooms_info_create_link,
                                onClick = onCreate
                            )
                        }
                    }
                    if (isCreateLoading.value) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_onehalf_line_height))
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    item {
                        AppDescriptionItem(text = R.string.rooms_share_shared_desc)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ShareSettingsScreenPreview() {
    val link = ExternalLink(
        access = 2,
        isLocked = false,
        isOwner = false,
        canEditAccess = false,
        sharedTo = ExternalLinkSharedTo(
            id = "",
            title = "",
            shareLink = "",
            linkType = 2,
            internal = true,
            denyDownload = false,
            isExpired = false,
            primary = true,
            requestToken = "",
            password = "",
            expirationDate = "2024-4-05T22:00:00.0000000+03:00"
        )
    )

    ManagerTheme {
        ShareSettingsScreen(
            state = ShareSettingsState.Success(
                listOf(
                    link.copy(access = 1),
                    link.copy(sharedTo = link.sharedTo.copy(expirationDate = null)),
                    link.copy(sharedTo = link.sharedTo.copy(isExpired = true, internal = false))
                )
            ),
            isCreateLoading = remember { mutableStateOf(true) },
            onBack = {},
            onCreate = {}
        )
    }
}