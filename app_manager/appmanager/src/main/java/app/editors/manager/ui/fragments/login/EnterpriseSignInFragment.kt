package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentLoginEnterpriseSigninBinding
import app.editors.manager.mvp.presenters.login.EnterpriseLoginPresenter
import app.editors.manager.mvp.views.login.CommonSignInView
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.SocialViews
import app.editors.manager.ui.views.custom.SocialViews.OnSocialNetworkCallbacks
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class EnterpriseSignInFragment : BaseAppFragment(), CommonSignInView, CommonDialog.OnClickListener,
    OnSocialNetworkCallbacks {

    companion object {
        val TAG: String = EnterpriseSignInFragment::class.java.simpleName

        private const val TAG_FOCUS_EMAIL = "TAG_FOCUS_EMAIL"
        private const val TAG_FOCUS_PWD = "TAG_FOCUS_PWD"

        fun newInstance(portal: String?, login: String?, providers: Array<String>?): EnterpriseSignInFragment {
            return EnterpriseSignInFragment().apply {
                arguments = Bundle().apply {
                    putString(SignInActivity.KEY_PORTAL, portal)
                    putString(SignInActivity.KEY_LOGIN, login)
                    putStringArray(SignInActivity.KEY_PROVIDERS, providers)
                }
            }
        }
    }

    @Inject
    lateinit var networkSettings: NetworkSettings

    @InjectPresenter
    lateinit var presenter: EnterpriseLoginPresenter

    private var viewBinding: FragmentLoginEnterpriseSigninBinding? = null

    private var mSignInActivity: SignInActivity? = null
    private var mSocialViews: SocialViews? = null
    private var mFieldsWatcher: FieldsWatcher? = null

    private val portal: String?
        get() = arguments?.getString(SignInActivity.KEY_PORTAL)
    private val login: String?
        get() = arguments?.getString(SignInActivity.KEY_LOGIN)
    private val providers: Array<String>
        get() = arguments?.getStringArray(SignInActivity.KEY_PROVIDERS) ?: emptyArray()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
        mSignInActivity = try {
            context as SignInActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                EnterpriseSignInFragment::class.java.simpleName + " - must implement - " +
                        EnterpriseSignInFragment::class.java.simpleName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginEnterpriseSigninBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSignInActivity!!.setOnActivityResult(this)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding?.loginEnterprisePortalEmailEdit?.hasFocus()?.let {
            outState.putBoolean(
                TAG_FOCUS_EMAIL,
                it
            )
        }

        viewBinding?.loginEnterprisePortalPasswordEdit?.hasFocus()?.let {
            outState.putBoolean(
                TAG_FOCUS_PWD,
                it
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mSocialViews!!.onDestroyView()
        mSignInActivity!!.setOnActivityResult(null)
        presenter.cancelRequest()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        viewBinding?.loginEnterpriseSigninButton?.setOnClickListener {
            signInButtonClick()
        }

        viewBinding?.loginEnterpriseSignonButton?.setOnClickListener {
            onSignOnButtonClick()
        }

        viewBinding?.loginEnterpriseForgotPwdButton?.setOnClickListener {
            onForgotPwdClick()
        }

        viewBinding?.loginEnterprisePortalPasswordEdit?.apply {
            setOnEditorActionListener { v, actionId, event ->
                actionKeyPress(v, actionId, event)
            }
            setOnTouchListener { _, _ ->
                onEmailTouchListener()
            }
        }
        viewBinding?.loginEnterprisePortalEmailEdit?.setOnTouchListener { _, _ ->
            onEmailTouchListener()
        }
    }

    override fun onBackPressed(): Boolean {
        hideKeyboard(viewBinding?.loginEnterprisePortalEmailEdit)
        hideKeyboard(viewBinding?.loginEnterprisePortalPasswordEdit)
        return super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SocialViews.GOOGLE_PERMISSION) {
            presenter.retrySignInWithGoogle()
        } else {
            mSocialViews!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK -> mSocialViews!!.onFacebookContinue()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag != null) {
            when (tag) {
                EnterpriseLoginPresenter.TAG_DIALOG_WAITING -> presenter.cancelRequest()
                EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK -> mSocialViews!!.onFacebookLogout()
            }
        }
    }

    private fun signInButtonClick() {
        val email = viewBinding?.loginEnterprisePortalEmailEdit?.text.toString()
        val password = viewBinding?.loginEnterprisePortalPasswordEdit?.text.toString()
        presenter.signInPortal(email.trim { it <= ' ' }, password, portal!!)
    }


    private fun onSignOnButtonClick() {
        //showFragment(EnterpriseSmsFragment.newInstance(false, null), EnterpriseSmsFragment.TAG, false);
        showFragment(
            SSOLoginFragment.newInstance(networkSettings.ssoUrl),
            SSOLoginFragment.TAG,
            true
        )
    }

    private fun onForgotPwdClick() {
        showUrlInBrowser(networkSettings.getScheme() + networkSettings.getPortal())
    }


    private fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            signInButtonClick()
            return true
        }
        return false
    }


    private fun onEmailTouchListener(): Boolean {
        viewBinding?.loginEnterprisePortalEmailEdit?.isFocusableInTouchMode = true
        viewBinding?.loginEnterprisePortalPasswordEdit?.isFocusableInTouchMode = true
        return false
    }

    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: String) {
        hideDialog()
        if (phoneNoise != null && phoneNoise.isNotEmpty()) {
            showFragment(EnterprisePhoneFragment.newInstance(request), EnterprisePhoneFragment.TAG, false)
        } else {
            showFragment(
                EnterpriseSmsFragment.newInstance(false, request),
                EnterpriseSmsFragment.TAG,
                false
            )
        }
    }

    override fun onTwoFactorAuthTfa(secretKey: String?, request: String) {
        hideDialog()
        if (secretKey != null && secretKey.isNotEmpty()) {
            AuthAppActivity.show(requireActivity(), request, secretKey)
        } else {
            showFragment(
                AuthPageFragment.newInstance(AuthPagerFragment.KEY_FOURTH_FRAGMENT, request, ""),
                AuthPageFragment.TAG,
                true
            )
        }
    }

    override fun onGooglePermission(intent: Intent) {
        requireActivity().startActivityForResult(intent, SocialViews.GOOGLE_PERMISSION)
    }

    override fun onEmailNameError(message: String) {
        hideDialog()
        viewBinding?.loginEnterprisePortalEmailLayout?.error = message
    }

    override fun onWaitingDialog(message: String, tag: String) {
        showWaitingDialog(message, getString(R.string.dialogs_common_cancel_button), tag)
    }

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message!!)
    }

    override fun onTwitterSuccess(token: String) {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            EnterpriseLoginPresenter.TAG_DIALOG_WAITING
        )
        presenter.signInWithTwitter(token)
    }

    override fun onTwitterFailed() {
        hideDialog()
        showSnackBar(R.string.socials_twitter_failed_auth)
    }

    override fun onFacebookSuccess(token: String) {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            EnterpriseLoginPresenter.TAG_DIALOG_WAITING
        )
        presenter.signInWithFacebook(token)
    }

    override fun onFacebookLogin(message: String) {
        showQuestionDialog(
            getString(R.string.dialogs_question_facebook_title),
            getString(R.string.dialogs_question_facebook_question) + message,
            getString(R.string.dialogs_question_accept_yes),
            getString(R.string.dialogs_question_accept_no),
            EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK
        )
    }

    override fun onFacebookCancel() {
        hideDialog()
        showSnackBar(R.string.socials_facebook_cancel_auth)
    }

    override fun onFacebookFailed() {
        hideDialog()
        showSnackBar(R.string.socials_facebook_failed_auth)
    }

    override fun onGoogleSuccess(account: Account) {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            EnterpriseLoginPresenter.TAG_DIALOG_WAITING
        )
        presenter.signInWithGoogle(account)
    }

    override fun onGoogleFailed() {
        hideDialog()
        showSnackBar(R.string.socials_google_failed_auth)
    }

    private fun init(savedInstanceState: Bundle?) {
        initViews()
        initListeners()
        args
        intent
        restoreViews(savedInstanceState)
    }

    private val args: Unit
        get() {
            val portal = portal
            if (portal != null && portal.isNotEmpty()) {
                setActionBarTitle(portal)
            }
            val login = login
            if (login != null && login.isNotEmpty()) {
                viewBinding?.loginEnterprisePortalEmailEdit?.setText(login)
            }
        }

    private fun initViews() {
        val facebookId =
            if (networkSettings.isPortalInfo) getString(R.string.facebook_app_id_info) else getString(
                R.string.facebook_app_id
            )
        mSocialViews = SocialViews(activity, viewBinding?.socialNetworkLayout?.socialNetworkLayout, facebookId)
        mSocialViews!!.setOnSocialNetworkCallbacks(this)
        mFieldsWatcher = FieldsWatcher()
        viewBinding?.loginEnterprisePortalEmailEdit!!.addTextChangedListener(mFieldsWatcher)
        viewBinding?.loginEnterprisePortalPasswordEdit!!.addTextChangedListener(mFieldsWatcher)
        viewBinding?.loginEnterpriseSigninButton?.isEnabled = false
        viewBinding?.loginEnterpriseSignonButton?.isEnabled = false

        if (providers.isNotEmpty()) {
            showGoogleLogin(providers.contains("google"))
            showFacebookLogin(providers.contains("facebook"))
        }
    }

    private val intent: Unit
        get() {
            val intent = activity!!.intent
            if (intent != null) {
//                if (intent.hasExtra(SignInActivity.TAG_PORTAL_SIGN_IN_EMAIL) && mPreferenceTool!!.login != null) {
//                    viewBinding?.loginEnterprisePortalEmailEdit!!.setText(mPreferenceTool!!.login)
//                }
            }
        }

    private fun restoreViews(savedInstanceState: Bundle?) {
        val ssoUrl = networkSettings.ssoUrl
        if (ssoUrl.isNotEmpty()) {
            viewBinding?.loginEnterpriseSignonButton?.apply {
                visibility = View.VISIBLE
                setSSOButtonText()
                isEnabled = true
            }
        }

//        final String login = mPreferenceTool.getLogin();
//        if (login != null) {
//            mLoginPersonalPortalEmailEdit.setText(login);
//        }
//
////        final String password = mPreferenceTool.getPassword();
////        if (password != null) {
////            mLoginPersonalPortalPasswordEdit.setText(password);
////        }
        if (savedInstanceState == null) {
            viewBinding?.loginEnterprisePortalEmailEdit?.isFocusable = false
            viewBinding?.loginEnterprisePortalPasswordEdit?.isFocusable = false
        } else {
            if (savedInstanceState.getBoolean(TAG_FOCUS_EMAIL)) {
                showKeyboard(viewBinding?.loginEnterprisePortalEmailEdit)
            } else if (savedInstanceState.getBoolean(TAG_FOCUS_PWD)) {
                showKeyboard(viewBinding?.loginEnterprisePortalPasswordEdit)
            }
        }
    }

    private fun setSSOButtonText() {
        val ssoLabel = networkSettings.ssoLabel
        viewBinding?.loginEnterpriseSignonButton?.text = if (ssoLabel.isNotEmpty()) getString(
            R.string.login_enterprise_single_sign_button_login,
            ssoLabel
        )
        else {
            getString(
                R.string.login_enterprise_single_sign_button_login,
                getString(R.string.login_enterprise_single_sign_button_login_default)
            )
        }
    }

    private fun showGoogleLogin(isShow: Boolean) {
        mSocialViews?.showGoogleLogin(isShow)
    }

    private fun showFacebookLogin(isShow: Boolean) {
        mSocialViews?.showFacebookLogin(isShow)
    }

    /*
     * Text input watcher
     * */
    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginEnterprisePortalEmailLayout?.isErrorEnabled = false
            val email = viewBinding?.loginEnterprisePortalEmailEdit?.text.toString()
            val password = viewBinding?.loginEnterprisePortalPasswordEdit?.text.toString()
            viewBinding?.loginEnterpriseSigninButton?.isEnabled = "" != email && "" != password
        }
    }
}