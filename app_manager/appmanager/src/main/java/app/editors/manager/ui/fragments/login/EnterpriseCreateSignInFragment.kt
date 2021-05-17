package app.editors.manager.ui.fragments.login

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import app.documents.core.network.models.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginEnterpriseCreateSigninBinding
import app.editors.manager.mvp.presenters.login.EnterpriseCreateLoginPresenter
import app.editors.manager.mvp.views.login.EnterpriseCreateSignInView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class EnterpriseCreateSignInFragment : BaseAppFragment(), EnterpriseCreateSignInView {

    companion object {
        val TAG: String = EnterpriseCreateSignInFragment::class.java.simpleName
        const val TAG_EMAIL = "TAG_EMAIL"
        const val TAG_FIRST = "TAG_FIRST"
        const val TAG_LAST = "TAG_LAST"

        fun newInstance(email: String?, first: String?, last: String?): EnterpriseCreateSignInFragment {
            val enterpriseCreateSignInFragment = EnterpriseCreateSignInFragment()
            val bundle = Bundle()
            bundle.putString(TAG_EMAIL, email)
            bundle.putString(TAG_FIRST, first)
            bundle.putString(TAG_LAST, last)
            enterpriseCreateSignInFragment.arguments = bundle
            return enterpriseCreateSignInFragment
        }
    }

    @InjectPresenter
    lateinit  var mSignInPortalPresenter: EnterpriseCreateLoginPresenter

    private var mFieldsWatcher: FieldsWatcher? = null
    private var viewBinding: FragmentLoginEnterpriseCreateSigninBinding? = null

    private var email: String? = null
    private var first: String? = null
    private var last: String? = null

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
        init(savedInstanceState)
    }

    private fun onSignInClick() {
        hideKeyboard(viewBinding?.loginSigninPasswordEdit)
        val password = viewBinding?.loginSigninPasswordEdit?.text.toString()
        val repeat = viewBinding?.loginSigninRepeatEdit?.text.toString()
        if (password != repeat) {
            viewBinding?.loginSigninRepeatLayout?.error =
                getString(R.string.login_create_signin_passwords_mismatch)
            return
        }
        showWaitingDialog(getString(R.string.dialogs_wait_title))
        email?.let { first?.let { it1 ->
            last?.let { it2 ->
                mSignInPortalPresenter.createPortal(password, it,
                    it1, it2
                )
            }
        } }
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        mSignInPortalPresenter.cancelRequest()
    }


    private fun onAgreeTerms() {
        showUrlInBrowser(getString(R.string.app_url_terms))
    }


    private fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onSignInClick()
            return true
        }
        return false
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

    private fun init(savedInstanceState: Bundle?) {
        mFieldsWatcher = FieldsWatcher()
        initListeners()
        setActionBarTitle(getString(R.string.login_create_signin_title))
        showKeyboard(viewBinding?.loginSigninPasswordEdit)
        viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
        viewBinding?.loginSigninPasswordEdit?.apply {
            requestFocus()
            addTextChangedListener(mFieldsWatcher)
        }
        args
    }

    private fun initListeners() {
        viewBinding?.loginSigninCreatePortalButton?.setOnClickListener {
            onSignInClick()
        }

        viewBinding?.loginSigninTermsInfoButton?.setOnClickListener {
            onAgreeTerms()
        }

        viewBinding?.loginSigninRepeatEdit?.setOnEditorActionListener { v, actionId, event ->
            actionKeyPress(v, actionId, event)
        }

        viewBinding?.loginSigninRepeatEdit?.addTextChangedListener(mFieldsWatcher)
    }

    private val args: Unit
        get() {
            val bundle = arguments
            email = bundle?.getString(TAG_EMAIL)
            first = bundle?.getString(TAG_FIRST)
            last = bundle?.getString(TAG_LAST)
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