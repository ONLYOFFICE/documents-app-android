package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentLoginPersonalPortalBinding
import app.editors.manager.mvp.presenters.login.PersonalLoginPresenter
import app.editors.manager.mvp.views.login.CommonSignInView
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPersonalSignUp
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPhone
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showSms
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.login.AuthPageFragment.Companion.newInstance
import app.editors.manager.ui.fragments.login.AuthPagerFragment.Companion.KEY_FOURTH_FRAGMENT
import app.editors.manager.ui.views.custom.SocialViews
import app.editors.manager.ui.views.custom.SocialViews.OnSocialNetworkCallbacks
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.RemoteUrlViewModel
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class PersonalPortalFragment : BaseAppFragment(), CommonSignInView, OnSocialNetworkCallbacks {

    companion object {
        val TAG: String = PersonalPortalFragment::class.java.simpleName

        private const val TAG_DIALOG_WAITING = "TAG_DIALOG_WAITING"
        private const val TAG_DIALOG_LOGIN_FACEBOOK = "TAG_DIALOG_LOGIN_FACEBOOK"
        private const val KEY_EMAIL = "KEY_EMAIL"
        private const val KEY_PASSWORD = "KEY_PASSWORD"

        fun newInstance(): PersonalPortalFragment {
            return PersonalPortalFragment()
        }
    }

    @InjectPresenter
    lateinit var personalSignInPresenter: PersonalLoginPresenter

    private val urlsViewModel: RemoteUrlViewModel by viewModels()

    private var viewBinding: FragmentLoginPersonalPortalBinding? = null

    private var portalsActivity: PortalsActivity? = null
    private var fieldsWatcher: FieldsWatcher? = null
    private var socialViews: SocialViews? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
        context.appComponent.inject(urlsViewModel)
        portalsActivity = try {
            context as PortalsActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                PersonalPortalFragment::class.java.simpleName + " - must implement - " +
                        PortalsActivity::class.java.simpleName
            )
        }
    }

    override fun onBackPressed(): Boolean {
        hideKeyboard()
        return super.onBackPressed()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginPersonalPortalBinding.inflate(inflater)
        restoreValues(savedInstanceState)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        portalsActivity?.setOnActivityResult(this)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        socialViews?.onDestroyView()
        portalsActivity?.setOnActivityResult(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_EMAIL, viewBinding?.loginPersonalPortalEmailEdit?.text.toString())
        outState.putString(KEY_PASSWORD, viewBinding?.loginPersonalPortalPasswordEdit?.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SocialViews.GOOGLE_PERMISSION) {
            personalSignInPresenter.retrySignInWithGoogle()
        } else {
            socialViews?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_LOGIN_FACEBOOK -> socialViews?.onFacebookContinue()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_WAITING -> personalSignInPresenter.cancelRequest()
                TAG_DIALOG_LOGIN_FACEBOOK -> socialViews?.onFacebookLogout()
            }
        }
    }

    private fun onSignInClick() {
        hideKeyboard(viewBinding?.loginPersonalPortalEmailEdit)
        val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
        val password = viewBinding?.loginPersonalPortalPasswordEdit?.text.toString()
        personalSignInPresenter.signInPersonal(email, password)
    }

    private fun signUpClick() {
        showPersonalSignUp(requireContext())
    }

    private fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick()
            return true
        }
        return false
    }

    override fun onSuccessLogin() {
        hideDialog()
        context?.let { MainActivity.show(it) }
        requireActivity().finish()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: String) {
        hideDialog()
        if (phoneNoise != null && phoneNoise.isNotEmpty()) {
            context?.let { showSms(it, request) }
        } else {
            context?.let { showPhone(it, request) }
        }
    }

    override fun onTwoFactorAuthTfa(secretKey: String?, request: String) {
        hideDialog()
        if (secretKey != null) {
            AuthAppActivity.show(requireActivity(), request, secretKey)
        } else {
            showFragment(newInstance(KEY_FOURTH_FRAGMENT, request, ""), AuthPageFragment.TAG, false)
        }
    }

    override fun onGooglePermission(intent: Intent) {
        requireActivity().startActivityForResult(intent, SocialViews.GOOGLE_PERMISSION)
    }

    override fun onEmailNameError(message: String) {
        hideDialog()
        viewBinding?.loginPersonalPortalEmailLayout?.error = message
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
            TAG_DIALOG_WAITING
        )
        personalSignInPresenter.signInPersonalWithTwitter(token)
    }

    override fun onTwitterFailed() {
        hideDialog()
        showSnackBar(R.string.socials_twitter_failed_auth)
    }

    override fun onFacebookSuccess(token: String) {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_WAITING
        )
        personalSignInPresenter.signInPersonalWithFacebook(token)
    }

    override fun onFacebookLogin(message: String) {
        showQuestionDialog(
            getString(R.string.dialogs_question_facebook_title),
            getString(R.string.dialogs_question_facebook_question) + message,
            getString(R.string.dialogs_question_accept_yes), getString(R.string.dialogs_question_accept_no),
            TAG_DIALOG_LOGIN_FACEBOOK
        )
    }

    override fun onFacebookCancel() {
        showSnackBar(R.string.socials_facebook_cancel_auth)
    }

    override fun onFacebookFailed() {
        showSnackBar(R.string.socials_facebook_failed_auth)
    }

    override fun onGoogleSuccess(account: Account) {
        showWaitingDialog(
            getString(R.string.dialogs_wait_title),
            getString(R.string.dialogs_common_cancel_button),
            TAG_DIALOG_WAITING
        )
        personalSignInPresenter.signInPersonalWithGoogle(account)
    }

    override fun onGoogleFailed() {
        showSnackBar(R.string.socials_google_failed_auth)
    }

    override fun onGoogleCancelled() {
        showSnackBar(R.string.socials_google_cancel_auth)
    }

    private fun init(savedInstanceState: Bundle?) {
        fieldsWatcher = FieldsWatcher()
        initListeners()
        socialViews = SocialViews(
            requireActivity(),
            viewBinding?.socialNetworkLayout?.socialNetworkLayout,
            getString(R.string.facebook_app_id)
        )
        socialViews?.setOnSocialNetworkCallbacks(this)
        viewBinding?.loginPersonalPortalEmailEdit?.clearFocus()
        viewBinding?.loginPersonalSigninButton?.isEnabled = false
        restoreValues(savedInstanceState)
        urlsViewModel.remoteUrls.observe(viewLifecycleOwner) { text ->
            text?.let {
                viewBinding?.termsTextView?.movementMethod = LinkMovementMethod.getInstance()
                viewBinding?.termsTextView?.text = text
            }
        }
        viewBinding?.termsCheckbox?.setOnCheckedChangeListener { _, isChecked ->
            viewBinding?.loginPersonalSigninButton?.isEnabled = isChecked
        }
    }

    private fun initListeners() {
        viewBinding?.loginPersonalSigninButton?.setOnClickListener {
            onSignInClick()
        }

        viewBinding?.loginPersonalSignupButton?.setOnClickListener {
            signUpClick()
        }
        viewBinding?.loginEnterpriseForgotPwdButton?.setOnClickListener {
            context?.let { SignInActivity.showPasswordRecovery(it, viewBinding?.loginPersonalPortalEmailEdit?.text.toString()) }
        }
        viewBinding?.loginPersonalPortalEmailEdit?.addTextChangedListener(fieldsWatcher)

        viewBinding?.loginPersonalPortalPasswordEdit?.addTextChangedListener(fieldsWatcher)

        viewBinding?.loginPersonalPortalPasswordEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }
    }

    private fun restoreValues(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            viewBinding?.loginPersonalPortalEmailEdit?.setText(savedInstanceState.getString(KEY_EMAIL))
            viewBinding?.loginPersonalPortalPasswordEdit?.setText(savedInstanceState.getString(KEY_PASSWORD))
        }
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginPersonalPortalEmailLayout?.isErrorEnabled = false
            val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
            val password = viewBinding?.loginPersonalPortalPasswordEdit?.text.toString()
            viewBinding?.loginPersonalSigninButton?.isEnabled = "" != email && "" != password
        }
    }
}