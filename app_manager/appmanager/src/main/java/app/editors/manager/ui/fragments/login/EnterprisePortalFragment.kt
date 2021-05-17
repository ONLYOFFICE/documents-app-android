package app.editors.manager.ui.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginEnterprisePortalBinding
import app.editors.manager.mvp.presenters.login.EnterprisePortalPresenter
import app.editors.manager.mvp.views.login.EnterprisePortalView
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPortalCreate
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPortalSignIn
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class EnterprisePortalFragment : BaseAppFragment(), EnterprisePortalView,
    CommonDialog.OnClickListener {

    companion object {
        val TAG: String = EnterprisePortalFragment::class.java.simpleName
        private const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        private const val TAG_DIALOG_HTTP = "TAG_DIALOG_HTTP"
        private const val KEY_EMAIL = "KEY_EMAIL"

        @JvmStatic
        fun newInstance() = EnterprisePortalFragment()
    }


    @InjectPresenter
    lateinit var presenter: EnterprisePortalPresenter

    private var viewBinding: FragmentLoginEnterprisePortalBinding? = null

    private var httpUrl = ""
    private var providers: Array<String>? = emptyArray()

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

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message ?: "")
    }

    override fun onSuccessPortal(portal: String, providers: Array<String>) {
        hideDialog()
        showPortalSignIn(requireContext(), portal, "", providers)
    }

    override fun onHttpPortal(portal: String, providers: Array<String>) {
        this.providers = providers
        httpUrl = portal
        showQuestionDialog(
            getString(R.string.dialogs_question_http_title),
            getString(R.string.dialogs_question_http_question), getString(R.string.dialogs_question_accept_yes),
            getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_HTTP
        )
    }

    override fun onPortalSyntax(message: String) {
        viewBinding?.loginEnterprisePortalLayout?.error = message
    }

    override fun onShowDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_check_portal_header_text),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_WAITING
        )
    }

    override fun onLoginPortal(portal: String) {
        viewBinding?.loginEnterprisePortalEdit?.setText(portal)
    }

    private fun init(savedInstanceState: Bundle?) {
        viewBinding?.loginEnterprisePortalEdit?.clearFocus()
        viewBinding?.loginEnterprisePortalEdit?.addTextChangedListener(FieldsWatcher())
        restoreValue(savedInstanceState)
        initListeners()
    }

    private fun restoreValue(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            viewBinding?.loginEnterprisePortalEdit?.setText(savedInstanceState.getString(KEY_EMAIL))
        }
    }

    private fun initListeners() {
        viewBinding?.loginEnterpriseCreateButton?.setOnClickListener {
            showPortalCreate(requireContext())

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
        presenter.checkPortal(viewBinding?.loginEnterprisePortalEdit?.text?.toString() ?: "")
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginEnterprisePortalLayout?.isErrorEnabled = false
        }
    }
}