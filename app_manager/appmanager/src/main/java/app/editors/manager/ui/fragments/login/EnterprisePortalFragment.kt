package app.editors.manager.ui.fragments.login

import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginEnterprisePortalBinding
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.EnterprisePortalState
import app.editors.manager.viewModels.login.EnterprisePortalViewModel
import app.editors.manager.viewModels.login.RemoteUrlViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
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
                viewModel.cancel()
                onSuccessPortal()
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

    private fun onSuccessPortal() {
        hideDialog()
        SignInActivity.showPortalSignIn(requireContext(), "", "", emptyArray())
    }

    private fun onHttpPortal() {
        showQuestionDialog(
            getString(R.string.dialogs_question_http_title),
            getString(R.string.dialogs_question_http_question),
            getString(R.string.dialogs_question_accept_yes),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_HTTP
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
                        onHttpPortal()
                    } else {
                        onSuccessPortal()
                    }
                }
                is EnterprisePortalState.Error -> {
                    if (state.message == R.string.login_enterprise_edit_error_hint) {
                        onPortalSyntax(getString(state.message))
                    } else if (state.message != null) {
                        onError(getString(state.message))
                    }
                }
            }
        }
        urlsViewModel.remoteUrls.observe(viewLifecycleOwner) { text: Spanned? ->
            text?.let {
                viewBinding?.terms?.termsTextView?.movementMethod = LinkMovementMethod.getInstance()
                viewBinding?.terms?.termsTextView?.text = text
            }
        }
        viewBinding?.terms?.termsCheckbox?.setOnCheckedChangeListener { _, isChecked ->
            viewBinding?.loginEnterpriseNextButton?.isEnabled =
                isChecked && viewBinding?.loginEnterprisePortalEdit?.text?.isNotEmpty() == true
        }

        lifecycleScope.launch {
            viewModel.portals.collect { portals ->
                if (portals.isNotEmpty()) {
                    viewBinding?.loginEnterprisePortalLayout?.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
                    viewBinding?.loginEnterprisePortalEdit?.setSimpleItems(portals)
                }
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
        hideKeyboard(requireContext(), view?.windowToken)
        viewModel.checkPortal(viewBinding?.loginEnterprisePortalEdit?.text?.trim().toString())
    }

    private inner class FieldsWatcher : BaseWatcher() {
        @Suppress("KotlinConstantConditions")
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginEnterprisePortalLayout?.isErrorEnabled = false
            if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                viewBinding?.loginEnterpriseNextButton?.isEnabled =
                    s.isNotEmpty() && viewBinding?.terms?.termsCheckbox?.isChecked == true
            } else {
                viewBinding?.loginEnterpriseNextButton?.isEnabled = s.isNotEmpty()
            }
        }
    }
}