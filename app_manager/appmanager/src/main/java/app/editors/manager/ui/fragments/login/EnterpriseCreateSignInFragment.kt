package app.editors.manager.ui.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginEnterpriseCreateSigninBinding
import app.editors.manager.mvp.presenters.login.EnterpriseCreateLoginPresenter
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import com.hcaptcha.sdk.HCaptcha
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class EnterpriseCreateSignInFragment : BaseAppFragment(), EnterpriseCreateSignInView {

    companion object {
        val TAG: String = EnterpriseCreateSignInFragment::class.java.simpleName
        const val TAG_PORTAL = "TAG_PORTAL"
        const val TAG_EMAIL = "TAG_EMAIL"
        const val TAG_FIRST = "TAG_FIRST"
        const val TAG_LAST = "TAG_LAST"

        fun newInstance(
            portalName: String?,
            email: String?,
            first: String?,
            last: String?
        ): EnterpriseCreateSignInFragment {
            return EnterpriseCreateSignInFragment().apply {
                arguments = Bundle().apply {
                    putString(TAG_PORTAL, portalName)
                    putString(TAG_EMAIL, email)
                    putString(TAG_FIRST, first)
                    putString(TAG_LAST, last)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var signInPortalPresenter: EnterpriseCreateLoginPresenter

    private var fieldsWatcher: FieldsWatcher? = null
    private var viewBinding: FragmentLoginEnterpriseCreateSigninBinding? = null
    private var hCaptcha: HCaptcha? = null

    private var portalName: String? = null
    private var email: String? = null
    private var first: String? = null
    private var last: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgs()
        initHCaptcha()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginEnterpriseCreateSigninBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hCaptcha?.removeAllListeners()
        hCaptcha?.destroy()
        hCaptcha = null
    }

    private fun initHCaptcha() {
        val siteKey = if (portalName?.endsWith(".info") == true) {
            BuildConfig.CAPTCHA_PUBLIC_KEY_INFO
        } else {
            BuildConfig.CAPTCHA_PUBLIC_KEY_COM
        }

        hCaptcha = HCaptcha.getClient(requireActivity()).apply {
            setup(siteKey)
            addOnSuccessListener { result ->
                if (result.tokenResult?.isNotEmpty() == true) {
                    signInPortalPresenter.createPortal(
                        password = viewBinding?.loginSigninPasswordEdit?.text.toString(),
                        email = checkNotNull(email),
                        first = checkNotNull(first),
                        last = checkNotNull(last),
                        recaptcha = checkNotNull(result.tokenResult)
                    )
                }
            }
            addOnFailureListener { error ->
                onError(error.message)
            }
        }
    }

    private fun onSignInClick() {
        hideKeyboard(viewBinding?.loginSigninPasswordEdit)
        val password = viewBinding?.loginSigninPasswordEdit?.text.toString()
        val repeat = viewBinding?.loginSigninRepeatEdit?.text.toString()

        if (password.length < 8 || password.length > 30) {
            viewBinding?.loginSigninPasswordLayout?.error = getString(R.string.login_create_signin_passwords_length)
            return
        } else if (password != repeat) {
            viewBinding?.loginSigninRepeatLayout?.error = getString(R.string.login_create_signin_passwords_mismatch)
            return
        }

        hCaptcha?.verifyWithHCaptcha()
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        signInPortalPresenter.cancelRequest()
    }

    private fun onAgreeTerms() {
        showUrlInBrowser(getString(R.string.app_url_terms))
    }

    private fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick()
            return true
        }
        return false
    }

    override fun onShowProgress() {
        showWaitingDialog(getString(R.string.dialogs_wait_title))
    }

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message!!)
    }

    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onTwoFactorAuth(phoneNoise: String?, request: String?) {
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

    private fun init() {
        fieldsWatcher = FieldsWatcher()
        initListeners()
        setActionBarTitle(getString(R.string.login_create_signin_title))
        showKeyboard(viewBinding?.loginSigninPasswordEdit)
        viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
        viewBinding?.loginSigninPasswordEdit?.apply {
            requestFocus()
            addTextChangedListener(fieldsWatcher)
        }
    }

    private fun initListeners() {
        viewBinding?.loginSigninCreatePortalButton?.setOnClickListener {
            onSignInClick()
        }

        viewBinding?.loginSigninTermsInfoButton?.setOnClickListener {
            onAgreeTerms()
        }

        viewBinding?.loginSigninRepeatEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }

        viewBinding?.loginSigninRepeatEdit?.addTextChangedListener(fieldsWatcher)

        viewBinding?.loginSigninPasswordEdit?.addTextChangedListener {
            viewBinding?.loginSigninPasswordLayout?.error = null
        }
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            portalName = bundle.getString(TAG_PORTAL)
            email = bundle.getString(TAG_EMAIL)
            first = bundle.getString(TAG_FIRST)
            last = bundle.getString(TAG_LAST)
        }
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginSigninRepeatLayout?.error = null
            val password = viewBinding?.loginSigninPasswordEdit?.text.toString()
            val repeat = viewBinding?.loginSigninRepeatEdit?.text.toString()
            viewBinding?.loginSigninCreatePortalButton?.isEnabled = "" != password && "" != repeat
        }
    }
}