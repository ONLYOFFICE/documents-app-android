package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentLoginPersonalSignupBinding
import app.editors.manager.mvp.presenters.login.PersonalSignUpPresenter
import app.editors.manager.mvp.views.login.PersonalRegisterView
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.RemoteUrlViewModel
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

    private val urlsViewModel: RemoteUrlViewModel by viewModels()

    private var fieldsWatcher: FieldsWatcher? = null

    private var viewBinding: FragmentLoginPersonalSignupBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.appComponent.inject(urlsViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
                TAG_DIALOG_INFO -> requireActivity().finish()
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

    override fun onWaitingDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_sign_in_register_portal),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_WAITING
        )
    }

    override fun onMessage(message: Int) {
        setMessage(message = getString(message), true)
    }

    private fun onSignUpClick() {
        hideKeyboard(viewBinding?.loginPersonalPortalEmailEdit)
        val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
        personalSignUpPresenter.checkMail(email = email)
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
                viewBinding?.loginPersonalPortalEmailEdit?.text.toString().replace(PersonalSignUpPresenter.TAG_INFO, "")
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
        urlsViewModel.remoteUrls.observe(viewLifecycleOwner) { text: Spanned? ->
            text?.let {
                viewBinding?.terms?.termsTextView?.movementMethod = LinkMovementMethod.getInstance()
                viewBinding?.terms?.termsTextView?.text = text
            }
        }
    }

    private fun initListeners() {
        viewBinding?.loginPersonalSignupButton?.setOnClickListener {
            onSignUpClick()
        }

        viewBinding?.loginPersonalPortalEmailEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }

        viewBinding?.terms?.termsCheckbox?.setOnCheckedChangeListener { _, isChecked ->
            viewBinding?.loginPersonalSignupButton?.isEnabled = isChecked && viewBinding?.loginPersonalPortalEmailEdit?.text?.isNotEmpty() == true
        }

        viewBinding?.loginPersonalPortalEmailEdit?.addTextChangedListener(fieldsWatcher)
    }

    private fun setMessage(message: String?, isError: Boolean) {
        viewBinding?.loginPersonalPortalEmailLayout?.apply {
            setErrorTextAppearance(if (isError) lib.toolkit.base.R.style.TextInputErrorRed else lib.toolkit.base.R.style.TextInputErrorGrey)
            error = message
        }
    }

    private fun setMessage(@StringRes resId: Int, isError: Boolean) {
        setMessage(getString(resId), isError)
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            setMessage(R.string.login_personal_signup_edit_info, false)
            val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
            viewBinding?.loginPersonalSignupButton?.isEnabled = "" != email && viewBinding?.terms?.termsCheckbox?.isChecked == true
        }
    }
}