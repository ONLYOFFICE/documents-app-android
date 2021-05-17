package app.editors.manager.ui.fragments.login

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentLoginPersonalPortalBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.presenters.login.PersonalLoginPresenter
import app.editors.manager.mvp.views.login.CommonSignInView
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.activities.login.PortalsActivity
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPersonalSignUp
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showPhone
import app.editors.manager.ui.activities.login.SignInActivity.Companion.showSms
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.fragments.login.AuthPageFragment.Companion.newInstance
import app.editors.manager.ui.fragments.login.AuthPagerFragment.KEY_FOURTH_FRAGMENT
import app.editors.manager.ui.views.custom.SocialViews
import app.editors.manager.ui.views.custom.SocialViews.OnSocialNetworkCallbacks
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter
import javax.inject.Inject

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

    @JvmField
    @Inject
    var mPreferenceTool: PreferenceTool? = null

    @InjectPresenter
    lateinit var mPersonalSignInPresenter: PersonalLoginPresenter

    private var viewBinding: FragmentLoginPersonalPortalBinding? = null

    private var mPortalsActivity: PortalsActivity? = null
    private var mFieldsWatcher: FieldsWatcher? = null
    private var mSocialViews: SocialViews? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
        mPortalsActivity = try {
            context as PortalsActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(PersonalPortalFragment::class.java.simpleName + " - must implement - " +
                    PortalsActivity::class.java.simpleName)
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
        mPortalsActivity!!.setOnActivityResult(this)
        init(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mSocialViews!!.onDestroyView()
        mPortalsActivity!!.setOnActivityResult(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_EMAIL, viewBinding?.loginPersonalPortalEmailEdit?.text.toString())
        outState.putString(KEY_PASSWORD, viewBinding?.loginPersonalPortalPasswordEdit?.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SocialViews.GOOGLE_PERMISSION) {
            mPersonalSignInPresenter.retrySignInWithGoogle()
        } else {
            mSocialViews!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAcceptClick(dialogs: Dialogs?, value: String?, tag: String?) {
        super.onAcceptClick(dialogs, value, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_LOGIN_FACEBOOK -> mSocialViews!!.onFacebookContinue()
            }
        }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        if (tag != null) {
            when (tag) {
                TAG_DIALOG_WAITING -> mPersonalSignInPresenter.cancelRequest()
                TAG_DIALOG_LOGIN_FACEBOOK -> mSocialViews!!.onFacebookLogout()
            }
        }
    }

    private fun onSignInClick() {
        hideKeyboard(viewBinding?.loginPersonalPortalEmailEdit)
        val email = viewBinding?.loginPersonalPortalEmailEdit?.text.toString()
        val password = viewBinding?.loginPersonalPortalPasswordEdit?.text.toString()
        mPersonalSignInPresenter.signInPersonal(email, password)
    }

    private fun signUpClick() {
        showPersonalSignUp(context!!)
    }

    private fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick()
            return true
        }
        return false
    }

    override fun onSuccessLogin() {
        hideDialog()
        context?.let { MainActivity.show(it) }
        activity!!.finish()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: String) {
        hideDialog()
        if (phoneNoise != null) {
            context?.let { showPhone(it, request) };
        } else {
            context?.let { showSms(it, request) };
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
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING)
        mPersonalSignInPresenter.signInPersonalWithTwitter(token)
    }

    override fun onTwitterFailed() {
        hideDialog()
        showSnackBar(R.string.socials_twitter_failed_auth)
    }

    override fun onFacebookSuccess(token: String) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING)
        mPersonalSignInPresenter.signInPersonalWithFacebook(token)
    }

    override fun onFacebookLogin(message: String) {
        showQuestionDialog(getString(R.string.dialogs_question_facebook_title),
            getString(R.string.dialogs_question_facebook_question) + message,
            getString(R.string.dialogs_question_accept_yes), getString(R.string.dialogs_question_accept_no),
            TAG_DIALOG_LOGIN_FACEBOOK)
    }

    override fun onFacebookCancel() {
        showSnackBar(R.string.socials_facebook_cancel_auth)
    }

    override fun onFacebookFailed() {
        showSnackBar(R.string.socials_facebook_failed_auth)
    }

    override fun onGoogleSuccess(account: Account) {
        showWaitingDialog(getString(R.string.dialogs_wait_title), getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_WAITING)
        mPersonalSignInPresenter.signInPersonalWithGoogle(account)
    }

    override fun onGoogleFailed() {
        showSnackBar(R.string.socials_google_failed_auth)
    }

    private fun init(savedInstanceState: Bundle?) {
        mFieldsWatcher = FieldsWatcher()
        initListeners()
        val facebookId = if (mPreferenceTool!!.isPortalInfo) getString(R.string.facebook_app_id_info) else getString(R.string.facebook_app_id)
        mSocialViews = SocialViews(activity, viewBinding?.socialNetworkLayout?.socialNetworkLayout, facebookId)
        mSocialViews!!.setOnSocialNetworkCallbacks(this)
        viewBinding?.loginPersonalPortalEmailEdit?.clearFocus()
        viewBinding?.loginPersonalSigninButton?.isEnabled = false
        restoreValues(savedInstanceState)
    }

    private fun initListeners() {
        viewBinding?.loginPersonalSigninButton?.setOnClickListener {
            onSignInClick()
        }

        viewBinding?.loginPersonalSignupButton?.setOnClickListener {
            signUpClick()
        }
        viewBinding?.loginPersonalPortalEmailEdit?.addTextChangedListener(mFieldsWatcher)

        viewBinding?.loginPersonalPortalPasswordEdit?.addTextChangedListener(mFieldsWatcher)

        viewBinding?.loginPersonalPortalPasswordEdit?.setOnEditorActionListener { v, actionId, event ->
            actionKeyPress(v, actionId, event)
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