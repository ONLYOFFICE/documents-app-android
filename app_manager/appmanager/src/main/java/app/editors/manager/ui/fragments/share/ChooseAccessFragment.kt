package app.editors.manager.ui.fragments.share

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.transition.TransitionManager
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.request
import app.documents.core.network.share.models.SharedTo
import app.documents.core.network.share.models.request.Invitation
import app.documents.core.network.share.models.request.RequestRoomShare
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.ChooseAccessFragmentLayoutBinding
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.mvp.models.ui.ShareUi
import app.editors.manager.ui.adapters.ShareAdapter
import app.editors.manager.ui.adapters.holders.factory.ShareHolderFactory
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.popup.SharePopup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lib.toolkit.base.ui.adapters.holder.ViewType

class ChooseAccessFragment : BaseAppFragment() {

    private val item: Item
        get() = checkNotNull(arguments?.getSerializable(TAG_ITEM, Item::class.java))

    private var viewBinding: ChooseAccessFragmentLayoutBinding? = null

    private val viewModel by viewModels<InviteUserViewModel> {
        InviteUserViewModelFactory(item)
    }

    private val adapter: ShareAdapter = ShareAdapter(ShareHolderFactory { view, position ->
        setPopup(view, position)
    })

    private fun setPopup(view: View, position: Int) {
        SharePopup(
            context = requireContext(),
            layoutId = R.layout.popup_share_menu
        ).apply {
            setContextListener(object : SharePopup.PopupContextListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onContextClick(v: View, sharePopup: SharePopup) {
                    val item = adapter.getItem(position) as ShareUi
                    sharePopup.hide()
                    when (v.id) {
                        R.id.fullAccessItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.READ_WRITE), position)
                        R.id.reviewItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.REVIEW), position)
                        R.id.viewItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.READ), position)
                        R.id.denyItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.RESTRICT), position)
                        R.id.deleteItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.NONE), position)
                        R.id.commentItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.COMMENT), position)
                        R.id.fillFormItem -> adapter.updateItem(item.copy(access = ApiContract.ShareCode.FILL_FORMS), position)
                    }
                    adapter.notifyDataSetChanged()
                }

            })
            setFullAccess(true)
            setItem(item)
            showDropAt(view, requireActivity())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = ChooseAccessFragmentLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBarTitle(getString(R.string.share_choose_access_title))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        setPanel()
        setRecyclerView()
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.setOnClickListener {
            viewModel.inviteUsers(
                emails = adapter.getItemList() as List<ShareUi>,
                message = viewBinding?.sharePanelLayout?.sharePanelMessageEdit?.text?.toString() ?: ""
            )
        }
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                updateUi(state)
            }
        }
    }

    private fun updateUi(state: InviteUserState) {
        when (state) {
            is InviteUserState.Loading -> {
                viewBinding?.progressBar?.isVisible = true
                viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = false
            }
            is InviteUserState.Success -> {
                viewBinding?.progressBar?.isVisible = false
                showSnackBar(R.string.invite_link_send_success)
                showRootFragment()
            }
            is InviteUserState.Error -> {
                viewBinding?.progressBar?.isVisible = false
                viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = true
                showSnackBar(state.message)
            }
            InviteUserState.None -> {
                viewBinding?.progressBar?.isVisible = false
                viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = true
            }
        }
    }

    private fun setPanel() {
        viewBinding?.let { binding ->
            binding.sharePanelLayout.sharePanelResetButton.isVisible = false
            binding.sharePanelLayout.sharePanelCountSelectedText.isVisible = false
            binding.sharePanelLayout.buttonPopupLayout.buttonPopupLayout.isVisible = false

            binding.sharePanelLayout.sharePanelAddButton.setText(R.string.share_invite_title)
            binding.sharePanelLayout.sharePanelMessageButton.setOnClickListener {
                TransitionManager.beginDelayedTransition(binding.sharePanelLayout.root)
                val isVisible = binding.sharePanelLayout.sharePanelMessageEditLayout.isVisible
                binding.sharePanelLayout.sharePanelMessageEditLayout.isVisible = !isVisible
            }
        }
    }


    private fun setRecyclerView() {
        val items: List<ViewType>? = arguments?.getStringArrayList(TAG_EMAILS)?.map {
            ShareUi(
                access = 1,
                sharedTo = SharedTo(email = it),
                isLocked = false,
                isOwner = false,
                isGuest = false,
                isRoom = true
            )
        }
        viewBinding?.accessRecyclerView?.adapter = adapter
        adapter.setItems(items ?: emptyList())
    }

    companion object {
        val TAG: String = ChooseAccessFragment::class.java.simpleName

        private const val TAG_EMAILS = "emails"
        private const val TAG_ITEM = "item"

        fun newInstance(item: Item, emails: List<String>): ChooseAccessFragment {
            return ChooseAccessFragment().apply {
                arguments = Bundle(2).apply {
                    putSerializable(TAG_ITEM, item)
                    putStringArrayList(TAG_EMAILS, ArrayList(emails))
                }
            }
        }
    }

}

class InviteUserViewModelFactory(private val item: Item) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InviteUserViewModel::class.java)) {
            return InviteUserViewModel(item) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class InviteUserState {
    object None : InviteUserState()
    object Loading : InviteUserState()
    object Success : InviteUserState()
    data class Error(val message: String) : InviteUserState()
}

class InviteUserViewModel(private val item: Item) : ViewModel() {

    private val shareApi = App.getApp().coreComponent.shareService

    private val _state: MutableStateFlow<InviteUserState> = MutableStateFlow(InviteUserState.None)
    val state: StateFlow<InviteUserState> = _state

    fun inviteUsers(emails: List<ShareUi>, message: String) {
        _state.value = InviteUserState.Loading
        viewModelScope.launch {
            request(
                func = {
                    shareApi.shareRoom(
                        id = item.id,
                        body = RequestRoomShare(
                            invitations = emails.map { email ->
                                Invitation(email = email.sharedTo.email, access = email.access)
                            },
                            notify = false,
                            message = message
                        )
                    )
                },
                onSuccess = { _state.value = InviteUserState.Success },
                onError = { _state.value = InviteUserState.Error(it.message ?: "Error") }
            )
        }
    }

}