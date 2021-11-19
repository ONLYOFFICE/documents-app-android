package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentLoginEnterprisePortalBinding
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.EnterprisePortalState
import app.editors.manager.viewModels.login.EnterprisePortalViewModel
import app.editors.manager.viewModels.login.RemoteUrlViewModel
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

class EnterprisePortalFragment : BaseAppFragment(),
    CommonDialog.OnClickListener {

    companion object {
        val TAG: String = EnterprisePortalFragment::class.java.simpleName
        private const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        private const val TAG_DIALOG_HTTP = "TAG_DIALOG_HTTP"
        private const val KEY_EMAIL = "KEY_EMAIL"

        @JvmStatic
        fun newInstance() = EnterprisePortalFragment()
    }

    private val viewModel: EnterprisePortalViewModel by viewModels()
    private val urlsViewModel: RemoteUrlViewModel by viewModels()

    private var viewBinding: FragmentLoginEnterprisePortalBinding? = null

    private var httpUrl = ""
    private var providers: Array<String>? = emptyArray()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(viewModel)
        requireContext().appComponent.inject(urlsViewModel)
    }

    override fun onBackPressed(): Boolean {
        hideKeyboard()
        return super.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentLoginEnterprisePortalBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            if (TAG_DIALOG_HTTP == tag) {
                if (httpUrl.isNotEmpty()) {
                    onSuccessPortal(httpUrl, providers ?: emptyArray())
                }
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        tag?.let { dialogTag ->
            if (dialogTag == TAG_DIALOG_WAITING) {
                viewModel.cancel()
            }
        }
    }

    private fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    private fun onSuccessPortal(portal: String, providers: Array<String>) {
        hideDialog()
        SignInActivity.showPortalSignIn(requireContext(), portal, "", providers)
    }

    private fun onHttpPortal(portal: String, providers: Array<String>) {
        this.providers = providers
        httpUrl = portal
        showQuestionDialog(
            getString(R.string.dialogs_question_http_title),
            getString(R.string.dialogs_question_http_question), getString(R.string.dialogs_question_accept_yes),
            getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_HTTP
        )
    }

    private fun onPortalSyntax(message: String) {
        viewBinding?.loginEnterprisePortalLayout?.error = message
    }

    private fun onShowDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_check_portal_header_text),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_WAITING
        )
    }

    private fun onLoginPortal(portal: String) {
        viewBinding?.loginEnterprisePortalEdit?.setText(portal)
    }

    private fun init(savedInstanceState: Bundle?) {
        viewBinding?.loginEnterprisePortalEdit?.clearFocus()
        viewBinding?.loginEnterprisePortalEdit?.addTextChangedListener(FieldsWatcher())
        restoreValue(savedInstanceState)
        initListeners()

        viewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            onError(error.message)
        }
        viewModel.portalStateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is EnterprisePortalState.Progress -> {
                    onShowDialog()
                }
                is EnterprisePortalState.Success -> {
                    if (state.isHttp) {
                        onHttpPortal(state.portal, state.providers)
                    } else {
                        onSuccessPortal(state.portal, state.providers)
                    }
                }
                is EnterprisePortalState.Error -> {
                    if (state.message == getString(R.string.login_enterprise_edit_error_hint)) {
                        onPortalSyntax(state.message)
                    } else {
                        onError(state.message)
                    }
                }
            }
        }
        urlsViewModel.remoteUrls.observe(viewLifecycleOwner) { text: Spanned? ->
            text?.let {
                viewBinding?.termsTextView?.movementMethod = LinkMovementMethod.getInstance()
                viewBinding?.termsTextView?.text = text
            }
        }
    }

    private fun restoreValue(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            viewBinding?.loginEnterprisePortalEdit?.setText(savedInstanceState.getString(KEY_EMAIL))
        }
    }

    private fun initListeners() {
        viewBinding?.loginEnterpriseCreateButton?.setOnClickListener {
            SignInActivity.showPortalCreate(requireContext())
        }
        viewBinding?.loginEnterpriseNextButton?.setOnClickListener {
            nextClick()
        }

        viewBinding?.loginEnterprisePortalEdit?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                nextClick()
            }
            false
        }
    }

    private fun nextClick() {
        hideKeyboard(viewBinding?.loginEnterprisePortalEdit)
        viewModel.checkPortal(viewBinding?.loginEnterprisePortalEdit?.text?.trim().toString())
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginEnterprisePortalLayout?.isErrorEnabled = false
        }
    }
}