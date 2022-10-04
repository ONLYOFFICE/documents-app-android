package app.editors.manager.ui.fragments.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.documents.core.network.models.share.request.RequestInviteLink
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.getShareApi
import app.editors.manager.databinding.ShareInviteFragmentLayoutBinding
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.ui.activities.main.ShareActivity
import com.google.android.material.chip.Chip
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lib.toolkit.base.ui.fragments.base.BaseFragment


class ShareInviteFragment : BaseFragment() {


    private var viewBinding: ShareInviteFragmentLayoutBinding? = null

    private val item: Item
        get() = arguments?.getSerializable("item") as Item

    private val viewModel by viewModels<InviteUserViewModel> {
        InviteUserViewModelFactory(item)
    }

    private var shareActivity: ShareActivity? = null

    private val currentTags: ArrayList<String> by lazy { arrayListOf() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareActivity = try {
            context as ShareActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                ShareInviteFragment::class.java.simpleName + " - must implement - " +
                        ShareActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ShareInviteFragmentLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBarTitle(getString(R.string.share_invite_user))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        loadTagsUi()
        initListeners()
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
                viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = true
            }
            is InviteUserState.Error -> {
                showSnackBar(state.message)
            }
            else -> {}
        }
    }

    private fun initListeners() {
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.setOnClickListener {
            if (getEmails().isEmpty()) {
//                viewBinding?.emailCompleteTextView?.error = getString(R.string.share_invite_error_empty)
                return@setOnClickListener
            }

            viewModel.inviteUsers(getEmails())

        }
        viewBinding?.sharePanelLayout?.inviteResetButton?.setOnClickListener {
            removeTags()
        }
    }

    private fun removeTags() {
        viewBinding?.chipGroup?.removeAllViews()
        currentTags.clear()
        updateCounter()
    }

    private fun getEmails(): List<String> {
        val emails = arrayListOf<String>()
        viewBinding?.chipGroup?.childCount?.let {
            for (i in 0 until it) {
                val chip = viewBinding?.chipGroup?.getChildAt(i) as Chip
                emails.add(chip.text.toString())
            }
        }
        return emails
    }

    private fun loadTagsUi() {
        val autoCompleteTextView = checkNotNull(viewBinding?.emailCompleteTextView)

        // done keyboard button is pressed
        autoCompleteTextView.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val name = textView.text.toString()
                textView.text = null
                addTag(name)
                return@setOnEditorActionListener true
            }
            false
        }

        // space or comma is detected
        autoCompleteTextView.addTextChangedListener {
            if (it != null && it.isEmpty()) {
                return@addTextChangedListener
            }

            if (it?.last() == ',' || it?.last() == ' ') {
                val name = it.substring(0, it.length - 1)
                addTag(name)

                viewBinding?.emailCompleteTextView?.text = null
            }
        }

        // initialize
        for (tag in currentTags) {
            addChipToGroup(tag, currentTags)
        }
    }

    private fun addTag(name: String) {
        if (name.isNotEmpty() && !currentTags.contains(name)) {
            addChipToGroup(name, currentTags)
            currentTags.add(name)
        }
    }

    private fun addChipToGroup(name: String, items: MutableList<String>) {
        val chip = Chip(context).apply {
            text = name
            isCheckable = false
            isCloseIconVisible = true
        }
        viewBinding?.chipGroup?.addView(chip)

        chip.setOnCloseIconClickListener {
            viewBinding?.chipGroup?.removeView(chip)
            items.remove(name)
            currentTags.remove(name)
            updateCounter()
        }
        updateCounter()
    }

    private fun updateCounter() {
        val count = viewBinding?.chipGroup?.childCount ?: 0
        viewBinding?.sharePanelLayout?.sharePanelCountSelectedText?.text = "$count"
    }

    companion object {

        val TAG: String = ShareInviteFragment::class.java.simpleName

        fun newInstance(item: Item) =
            ShareInviteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("item", item)
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

    private val shareApi = App.getApp().getShareApi()

    private var disposable: Disposable? = null

    private val _state: MutableStateFlow<InviteUserState> = MutableStateFlow(InviteUserState.None)
    val state: StateFlow<InviteUserState> = _state

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }

    fun inviteUsers(emails: List<String>) {
        _state.value = InviteUserState.Loading
        disposable = shareApi.sendInviteLink(item.id, RequestInviteLink(emails, 1, 1))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _state.value = InviteUserState.Success
            }, {
                _state.value = InviteUserState.Error(it.message ?: "Error")
            })
    }

}

//public enum EmployeeType {
//    All = 0,
//    User = 1,
//    Visitor = 2
//}