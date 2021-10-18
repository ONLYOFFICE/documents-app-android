package app.editors.manager.ui.fragments.login

import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.mvp.views.login.PasswordRecoveryView
import moxy.presenter.InjectPresenter
import app.editors.manager.mvp.presenters.login.PasswordRecoveryPresenter
import app.editors.manager.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import app.editors.manager.databinding.FragmentLoginPasswordRecoveryBinding
import app.editors.manager.ui.views.edits.BaseWatcher

class PasswordRecoveryFragment : BaseAppFragment(), PasswordRecoveryView {

    companion object {
        var TAG: String = PasswordRecoveryFragment::class.java.simpleName

        const val KEY_EMAIL = "KEY_EMAIL"
        const val KEY_PERSONAL = "KEY_PERSONAL"

        fun newInstance(email: String?, isPersonal: Boolean?): PasswordRecoveryFragment {
            return PasswordRecoveryFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_EMAIL, email)
                    putBoolean(KEY_PERSONAL, isPersonal!!)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: PasswordRecoveryPresenter

    private var viewBinding: FragmentLoginPasswordRecoveryBinding? = null

    private var isPasswordRecovered = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginPasswordRecoveryBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        viewBinding?.apply {
            loginPasswordRecoveryEmailEdit.apply {
                setText(arguments?.getString(KEY_EMAIL))
                addTextChangedListener(FieldsWatcher())
                setOnEditorActionListener { _, actionId, _ ->
                    actionKeyPress(actionId)
                }
            }
            loginPasswordRecoveryButton.apply {
                if (arguments?.getString(KEY_EMAIL)?.isEmpty() == true) {
                    this.isEnabled = false
                }
                setOnClickListener {
                    onRecoverButtonClick()
                }
            }
        }
        setActionBarTitle(context?.getString(R.string.login_password_recovery_toolbar_title))
    }

    private fun onRecoverButtonClick() {
        if (!isPasswordRecovered) {
            arguments?.getBoolean(
                KEY_PERSONAL
            )?.let {
                presenter.recoverPassword(
                    viewBinding?.loginPasswordRecoveryEmailEdit?.text.toString().trim { it <= ' ' },
                    it
                )
            }
        } else {
            activity?.onBackPressed()
        }
    }

    override fun onPasswordRecoverySuccess(email: String) {
        isPasswordRecovered = true
        viewBinding?.apply {
            loginPasswordRecoveryEmailLayout.visibility = View.INVISIBLE
            loginPasswordRecoveryHint.text = getString(R.string.login_password_recovery_success_hint, email)
            loginPasswordRecoveryImage.visibility = View.VISIBLE
            loginPasswordRecoveryButton.apply {
                visibility = View.VISIBLE
                text = context?.getString(R.string.login_password_recovery_button_text)
            }
        }
    }

    override fun onEmailError() {
        hideDialog()
        viewBinding?.loginPasswordRecoveryEmailLayout?.error =
            context?.getString(R.string.errors_email_syntax_error)
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it)?.show() }
    }

    fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onRecoverButtonClick()
            hideKeyboard()
            return true
        }
        return false
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginPasswordRecoveryEmailLayout?.isErrorEnabled = false
            val email = viewBinding?.loginPasswordRecoveryEmailEdit?.text.toString()
            viewBinding?.loginPasswordRecoveryButton?.isEnabled = "" != email
        }
    }
}