package app.editors.manager.ui.fragments.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.viewModels.main.UserListEffect
import app.editors.manager.viewModels.main.UserListViewModel
import com.google.android.material.appbar.AppBarLayout
import lib.compose.ui.theme.ManagerTheme
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (requireActivity() is ShareActivity) {
            (requireActivity() as ShareActivity).findViewById<AppBarLayout>(R.id.app_bar_layout).isVisible = false
        }

        (view as ComposeView).setContent {
            ManagerTheme {
                val viewModel = viewModel {
                    UserListViewModel(
                        roomId = checkNotNull(arguments?.getSerializableExt<Item>(ShareActivity.TAG_SHARE_ITEM)).id,
                        shareService = requireContext().shareApi,
                        roomProvider = requireContext().roomProvider,
                        resourcesProvider = requireContext().appComponent.resourcesProvider,
                    )
                }
                val state by viewModel.viewState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.effect.collect {
                        when (it) {
                            UserListEffect.Success -> requireActivity().finish()
                            is UserListEffect.Error -> {
                                UiUtils.getSnackBar(requireActivity()).setText(it.message).show()
                            }
                        }
                    }
                }

                UserListScreen(
                    title = R.string.room_set_owner_title,
                    userListState = state,
                    onClick = viewModel::setOwner,
                    onSearch = viewModel::search,
                    onBack = ::onBackPressed
                )
            }
        }
    }
}