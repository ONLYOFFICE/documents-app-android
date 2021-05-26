package app.editors.manager.ui.fragments.login

import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.mvp.views.login.PasswordRecoveryView
import moxy.presenter.InjectPresenter
import app.editors.manager.mvp.presenters.login.PasswordRecoveryPresenter
import butterknife.Unbinder
import butterknife.BindView
import app.editors.manager.R
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnEditorAction
import android.view.inputmethod.EditorInfo
import app.editors.manager.ui.views.edits.BaseWatcher

class PasswordRecoveryFragment : BaseAppFragment(), PasswordRecoveryView {

    companion object {
        var TAG = PasswordRecoveryFragment::class.java.simpleName
        const val KEY_EMAIL = "KEY_EMAIL"
        const val KEY_PERSONAL = "KEY_PERSONAL"
        fun newInstance(email: String?, isPersonal: Boolean?): PasswordRecoveryFragment {
            return PasswordRecoveryFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_EMAIL, email)
                    putBoolean(KEY_PERSONAL, isPersonal!!)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: PasswordRecoveryPresenter

    protected var mUnbinder: Unbinder? = null
    private var isPasswordRecovered = false

    @JvmField
    @BindView(R.id.login_password_recovery_hint)
    var loginPasswordRecoveryHint: TextView? = null

    @JvmField
    @BindView(R.id.login_password_recovery_button)
    var recoverButton: AppCompatButton? = null

    @JvmField
    @BindView(R.id.login_password_recovery_email_layout)
    var passwordRecoveryEmailLayout: TextInputLayout? = null

    @JvmField
    @BindView(R.id.login_password_recovery_email_edit)
    var passwordRecoveryEmailEdit: AppCompatEditText? = null

    @JvmField
    @BindView(R.id.login_password_recovery_image)
    var passwordRecoveryImageView: AppCompatImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_login_password_recovery, container, false)
        mUnbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnbinder?.unbind()
    }

    private fun init() {
        passwordRecoveryEmailEdit?.setText(arguments?.getString(KEY_EMAIL))
        if (arguments?.getString(KEY_EMAIL)?.isEmpty() == true) {
            recoverButton?.isEnabled = false
        }
        passwordRecoveryEmailEdit?.addTextChangedListener(FieldsWatcher())
        setActionBarTitle(context?.getString(R.string.login_password_recovery_toolbar_title))
    }

    @OnClick(R.id.login_password_recovery_button)
    fun onRecoverButtonClick() {
        if (!isPasswordRecovered) {
            arguments?.getBoolean(
                KEY_PERSONAL
            )?.let {
                presenter.recoverPassword(
                    passwordRecoveryEmailEdit?.text.toString().trim { it <= ' ' },
                    it
                )
            }
        } else {
            activity?.onBackPressed()
        }
    }

    override fun onPasswordRecoverySuccess(email: String) {
        passwordRecoveryEmailLayout?.visibility = View.INVISIBLE
        loginPasswordRecoveryHint?.text =
            context!!.getString(R.string.login_password_recovery_success_hint, email)
        isPasswordRecovered = true
        recoverButton?.visibility = View.VISIBLE
        passwordRecoveryImageView?.visibility = View.VISIBLE
        recoverButton?.text = context?.getString(R.string.login_password_recovery_button_text)
    }

    override fun onEmailError() {
        hideDialog()
        passwordRecoveryEmailLayout?.error =
            context?.getString(R.string.errors_email_syntax_error)
    }

    override fun onError(message: String?) {
        message?.let { showSnackBar(it)?.show() }
    }

    @OnEditorAction(R.id.login_password_recovery_email_edit)
    fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            onRecoverButtonClick()
            hideKeyboard()
            return true
        }
        return false
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            passwordRecoveryEmailLayout?.isErrorEnabled = false
            val email = passwordRecoveryEmailEdit?.text.toString()
            recoverButton?.isEnabled = "" != email
        }
    }
}