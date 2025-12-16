package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.managers.utils.SocialSignIn
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
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
import com.google.android.gms.auth.GoogleAuthUtil
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

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

    @InjectPresenter
    lateinit var presenter: EnterpriseLoginPresenter

    private var viewBinding: FragmentLoginEnterpriseSigninBinding? = null

    private var signInActivity: SignInActivity? = null
    private var socialViews: SocialViews? = null
    private var fieldsWatcher: FieldsWatcher? = null

    private val portal: String?
        get() = arguments?.getString(SignInActivity.KEY_PORTAL)

    private val login: String?
        get() = arguments?.getString(SignInActivity.KEY_LOGIN)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
        signInActivity = try {
            context as SignInActivity
        } catch (_: ClassCastException) {
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
        signInActivity?.setOnActivityResult(this)
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
        socialViews?.onDestroyView()
        signInActivity?.setOnActivityResult(null)
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
            setOnEditorActionListener { _, actionId, _ ->
                actionKeyPress(actionId)
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
            presenter.signInWithProvider(null, ApiContract.Social.GOOGLE)
        } else {
            socialViews?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK -> socialViews?.onFacebookContinue()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag != null) {
            when (tag) {
                EnterpriseLoginPresenter.TAG_DIALOG_WAITING -> presenter.cancelRequest()
                EnterpriseLoginPresenter.TAG_DIALOG_LOGIN_FACEBOOK -> socialViews?.onFacebookLogout()
            }
        }
    }

    private fun signInButtonClick() {
        val email = viewBinding?.loginEnterprisePortalEmailEdit?.text.toString()
        val password = viewBinding?.loginEnterprisePortalPasswordEdit?.text.toString()
        presenter.signInPortal(email.trim { it <= ' ' }, password, CloudPortal(url = portal.orEmpty()))
    }

    private fun onSignOnButtonClick() {
        showFragment(
            SSOLoginFragment.newInstance(App.getApp().loginComponent.currentPortal?.settings?.ssoUrl),
            SSOLoginFragment.TAG,
            true
        )
    }

    private fun onForgotPwdClick() {
        showFragment(
            PasswordRecoveryFragment.newInstance(
                viewBinding?.loginEnterprisePortalEmailEdit?.text.toString(),
                false,
                presenter.useLdap
            ), PasswordRecoveryFragment.TAG, false
        )
    }


    private fun actionKeyPress(actionId: Int): Boolean {
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
        MainActivity.show(requireContext(), requireActivity().intent.data)
        requireActivity().finish()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: String) {
        hideDialog()
        if (!phoneNoise.isNullOrEmpty()) {
            showFragment(
                EnterpriseSmsFragment.newInstance(false, request),
                EnterpriseSmsFragment.TAG,
                false
            )
        } else {
            showFragment(EnterprisePhoneFragment.newInstance(request), EnterprisePhoneFragment.TAG, false)
        }
    }

    override fun onTwoFactorAuthTfa(secretKey: String?, request: String) {
        hideDialog()
        if (!secretKey.isNullOrEmpty()) {
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

    override fun onWaitingDialog() {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            EnterpriseLoginPresenter.TAG_DIALOG_WAITING
        )
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    override fun onTwitterSuccess(token: String) {
        onWaitingDialog()
        presenter.signInWithProvider(token, ApiContract.Social.TWITTER)
    }

    override fun onTwitterFailed() {
        hideDialog()
        showSnackBar(R.string.socials_twitter_failed_auth)
    }

    override fun onFacebookSuccess(token: String) {
        onWaitingDialog()
        presenter.signInWithProvider(token, ApiContract.Social.FACEBOOK)
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
        onWaitingDialog()
        val scope = requireContext().getString(R.string.google_scope)
        val accessToken = GoogleAuthUtil.getToken(requireContext(), account, scope)
        presenter.signInWithProvider(accessToken, ApiContract.Social.GOOGLE)
    }

    override fun onGoogleFailed() {
        hideDialog()
        showSnackBar(R.string.socials_google_failed_auth)
    }

    override fun onGoogleCancelled() {
        hideDialog()
        showSnackBar(R.string.socials_google_cancel_auth)
    }

    private fun init(savedInstanceState: Bundle?) {
        initViews()
        initListeners()
        args
        intent
        restoreViews(savedInstanceState)
    }

    override fun onSocialClick(social: String) {
        presenter.signInPortalSocial(social)
    }

    override fun onSocialAuth(socialSignIn: SocialSignIn?) {
        socialSignIn?.let { social ->
            val url = social.getUrl()
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), url.toUri())
        }
    }

    fun handleSocialOAuthCode(code: String?) {
        if (!code.isNullOrEmpty()) {
            onWaitingDialog()
            presenter.signInWithProvider(
                accessToken = null,
                codeOauth = code,
                provider = presenter.socialSignIn?.providerKey.orEmpty(),
            )
        } else {
            showSnackBar(R.string.socials_failed_auth)
        }
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
        val facebookId = if (context?.accountOnline?.portalUrl?.endsWith(".info") == true)
            BuildConfig.FACEBOOK_APP_ID_INFO else
            BuildConfig.FACEBOOK_APP_ID

        socialViews = SocialViews(requireActivity(), viewBinding?.socialNetworkLayout?.socialNetworkLayout, facebookId)
        socialViews?.setOnSocialNetworkCallbacks(this)
        fieldsWatcher = FieldsWatcher()
        viewBinding?.loginEnterprisePortalEmailEdit?.addTextChangedListener(fieldsWatcher)
        viewBinding?.loginEnterprisePortalPasswordEdit?.addTextChangedListener(fieldsWatcher)
        viewBinding?.loginEnterpriseSigninButton?.isEnabled = false

        val cloudPortal = presenter.currentPortal
        if (cloudPortal != null && cloudPortal.settings.ldap) {
            viewBinding?.ldapCheckbox?.isVisible = true
            viewBinding?.ldapCheckbox?.setOnCheckedChangeListener { _, isChecked ->
                presenter.useLdap = isChecked
                viewBinding?.loginEnterprisePortalEmailLayout?.error = null
                if (isChecked) {
                    viewBinding?.loginEnterprisePortalEmailLayout?.setHint(R.string.profile_username_title)
                } else {
                    viewBinding?.loginEnterprisePortalEmailLayout?.setHint(R.string.login_enterprise_email_hint)
                }
            }
        }

        presenter.checkSocialProvider(portal.orEmpty()) { socialViews?.setProviders(it) }
    }

    private val intent: Unit
        get() {
            val intent = activity?.intent
            if (intent != null) {
                //                if (intent.hasExtra(SignInActivity.TAG_PORTAL_SIGN_IN_EMAIL) && mPreferenceTool!!.login != null) {
                //                    viewBinding?.loginEnterprisePortalEmailEdit!!.setText(mPreferenceTool!!.login)
                //                }
            }
        }

    private fun restoreViews(savedInstanceState: Bundle?) {
        val ssoUrl = App.getApp().loginComponent.currentPortal?.settings?.ssoUrl
        if (!ssoUrl.isNullOrEmpty()) {
            viewBinding?.loginEnterpriseSignonButton?.apply {
                visibility = View.VISIBLE
                setSSOButtonText()
                isEnabled = true
            }
        }
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
        val ssoLabel = App.getApp().loginComponent.currentPortal?.settings?.ssoLabel
        viewBinding?.loginEnterpriseSignonButton?.text = if (!ssoLabel.isNullOrEmpty()) getString(
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