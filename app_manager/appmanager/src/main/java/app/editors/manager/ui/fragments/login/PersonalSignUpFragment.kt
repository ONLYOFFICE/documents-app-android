package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginPersonalSignupBinding
import app.editors.manager.mvp.presenters.login.PersonalSignUpPresenter
import app.editors.manager.mvp.views.login.PersonalRegisterView
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.managers.utils.StringUtils.isEmailValid
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class PersonalSignUpFragment : BaseAppFragment(), PersonalRegisterView {

    companion object {
        val TAG: String = PersonalSignUpFragment::class.java.simpleName

        private const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        private const val TAG_DIALOG_INFO = "TAG_DIALOG_INFO"

        fun newInstance(): PersonalSignUpFragment {
            return PersonalSignUpFragment()
        }
    }

    @InjectPresenter
    lateinit var personalSignUpPresenter: PersonalSignUpPresenter

    private var fieldsWatcher: FieldsWatcher? = null

    private var viewBinding: FragmentLoginPersonalSignupBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginPersonalSignupBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_INFO -> activity!!.finish()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_WAITING -> personalSignUpPresenter.cancelRequest()
                TAG_DIALOG_INFO -> hideDialog()
            }
        }
    }

    private fun onSignUpClick() {
        hideKeyboard(viewBinding?.loginPersonalPortalEmailEdit)
        val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
        if (isEmailValid(email)) {
            showWaitingDialog(
                getString(R.string.dialogs_sign_in_register_portal),
                getString(R.string.dialogs_common_cancel_button),
                TAG_DIALOG_WAITING
            )
            personalSignUpPresenter.registerPortal(email)
        } else {
            setMessage(R.string.errors_email_syntax_error, true)
        }
    }

    private fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignUpClick()
            return true
        }
        return false
    }

    override fun onError(message: String?) {
        hideDialog()
        setMessage(message, true)
    }

    @SuppressLint("StringFormatInvalid")
    override fun onRegisterPortal() {
        showQuestionDialog(
            getString(R.string.dialogs_question_personal_confirm_link_title),
            getString(
                R.string.login_personal_signup_dialog_info,
                viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
            ),
            getString(R.string.dialogs_question_personal_confirm_accept),
            getString(R.string.dialogs_question_personal_confirm_cancel),
            TAG_DIALOG_INFO
        )
    }

    private fun init() {
        fieldsWatcher = FieldsWatcher()
        initListeners()
        setActionBarTitle(getString(R.string.login_personal_signup_title))
        showKeyboard(viewBinding?.loginPersonalPortalEmailEdit)
        viewBinding?.loginPersonalSignupButton?.isEnabled = false
        setMessage(R.string.login_personal_signup_edit_info, false)
    }

    private fun initListeners() {
        viewBinding?.loginPersonalSignupButton?.setOnClickListener {
            onSignUpClick()
        }

        viewBinding?.loginPersonalPortalEmailEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }

        viewBinding?.loginPersonalPortalEmailEdit?.addTextChangedListener(fieldsWatcher)
    }

    private fun setMessage(message: String?, isError: Boolean) {
        viewBinding?.loginPersonalPortalEmailLayout?.apply {
            setErrorTextAppearance(if (isError) R.style.TextInputErrorRed else R.style.TextInputErrorGrey)
            error = message
        }
    }

    private fun setMessage(@StringRes resId: Int, isError: Boolean) {
        setMessage(getString(resId), isError)
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginPersonalPortalEmailLayout?.apply {
                setErrorTextAppearance(R.style.TextInputErrorGrey)
                error = getString(R.string.login_personal_signup_edit_info)
            }
            val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
            viewBinding?.loginPersonalSignupButton?.isEnabled = "" != email
        }
    }
}