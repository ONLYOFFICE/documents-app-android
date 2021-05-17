package app.editors.manager.ui.fragments.login

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import app.editors.manager.R
import app.editors.manager.databinding.FragmentLoginEnterpriseCreatePortalBinding
import app.editors.manager.mvp.presenters.login.EnterpriseCreateValidatePresenter
import app.editors.manager.mvp.views.login.EnterpriseCreateValidateView
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseInputFilter
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.managers.utils.StringUtils.isAlphaNumeric
import lib.toolkit.base.managers.utils.StringUtils.isCreateUserName
import lib.toolkit.base.managers.utils.UiUtils.measureTextSizes
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs
import moxy.presenter.InjectPresenter

class EnterpriseCreatePortalFragment : BaseAppFragment(), EnterpriseCreateValidateView {

    companion object {
        val TAG: String = EnterpriseCreatePortalFragment::class.java.simpleName
        fun newInstance(): EnterpriseCreatePortalFragment {
            return EnterpriseCreatePortalFragment()
        }
    }

    @InjectPresenter
    lateinit var mCreatePortalPresenter: EnterpriseCreateValidatePresenter

    private var viewBinding: FragmentLoginEnterpriseCreatePortalBinding? = null

    private var mFieldsWatcher: FieldsWatcher? = null
    private var mPaddingTop = 0
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var mPaddingBottom = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginEnterpriseCreatePortalBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }


    private fun onNextClick() {
        hideKeyboard(viewBinding?.loginCreatePortalAddressEdit)
        val address = viewBinding?.loginCreatePortalAddressEdit?.text.toString()
        val email = viewBinding?.loginCreatePortalEmailEdit?.text.toString()
        val first = viewBinding?.loginCreatePortalFirstNameEdit?.text.toString()
        val last = viewBinding?.loginCreatePortalLastNameEdit?.text.toString()
        mCreatePortalPresenter.validatePortal(address, email, first, last)
    }


    private fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onNextClick()
            return true
        }
        return false
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        mCreatePortalPresenter.cancelRequest()
    }

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message!!)
    }

    override fun onValidatePortalSuccess(email: String?, first: String?, last: String?) {
        hideDialog()
        showFragment(
            EnterpriseCreateSignInFragment.newInstance(email, first, last),
            EnterpriseCreateSignInFragment.TAG, false
        )
    }

    override fun onPortalNameError(message: String) {
        setEditHintVisibility(false)
        viewBinding?.loginCreatePortalAddressEditLayout?.error = message
    }

    override fun onEmailNameError(message: String) {
        viewBinding?.loginCreatePortalEmailLayout?.error = message
    }

    override fun onFirstNameError(message: String) {
        viewBinding?.loginCreatePortalFirstNameLayout?.error = message
    }

    override fun onLastNameError(message: String) {
        viewBinding?.loginCreatePortalLastNameLayout?.error = message
    }

    override fun onRegionDomain(domain: String) {
        val textWidth = measureTextSizes(
            domain + "X", viewBinding?.loginCreatePortalAddressHintEnd?.textSize!!
                .toInt()
        ).x
        viewBinding?.loginCreatePortalAddressHintEnd?.apply {
            layoutParams.width = textWidth
            text = domain
        }
        mPaddingRight = textWidth
    }

    override fun onShowWaitingDialog(@StringRes title: Int) {
        showWaitingDialog(getString(title))
    }

    private fun init() {
        mFieldsWatcher = FieldsWatcher()
        initListeners()
        setActionBarTitle(getString(R.string.login_create_portal_title))
        showKeyboard(viewBinding?.loginCreatePortalAddressEdit)
        viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
        viewBinding?.loginCreatePortalAddressEdit?.apply {
            requestFocus()
            filters = arrayOf<InputFilter>(FieldsFilter())
        }
        mCreatePortalPresenter.domain
        setPadding()
    }

    private fun initListeners() {
        viewBinding?.loginSigninCreatePortalButton?.setOnClickListener {
            onNextClick()
        }
        viewBinding?.loginCreatePortalLastNameEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }
        viewBinding?.loginCreatePortalAddressEdit?.addTextChangedListener(mFieldsWatcher)
        viewBinding?.loginCreatePortalEmailEdit?.addTextChangedListener(mFieldsWatcher)
        viewBinding?.loginCreatePortalFirstNameEdit?.addTextChangedListener(mFieldsWatcher)
        viewBinding?.loginCreatePortalLastNameEdit?.addTextChangedListener(mFieldsWatcher)
    }

    private fun setPadding() {
        mPaddingTop = viewBinding?.loginCreatePortalAddressEdit?.paddingTop ?: 0
        mPaddingLeft = viewBinding?.loginCreatePortalAddressEdit?.paddingLeft ?: 0
        mPaddingRight = viewBinding?.loginCreatePortalAddressEdit?.paddingRight ?: 0
        mPaddingBottom = viewBinding?.loginCreatePortalAddressEdit?.paddingBottom ?: 0
    }

    private fun setEditHintVisibility(isVisible: Boolean) {
        if (isVisible) {
            viewBinding?.loginCreatePortalAddressHintEnd?.visibility = View.VISIBLE
            viewBinding?.loginCreatePortalAddressEdit?.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
        } else {
            viewBinding?.loginCreatePortalAddressHintEnd?.visibility = View.GONE
            viewBinding?.loginCreatePortalAddressEdit?.setPadding(mPaddingLeft, mPaddingTop, 0, mPaddingBottom)
        }
    }

    /*
     * Filtering inputs
     * */
    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginCreatePortalEmailLayout?.isErrorEnabled = false
            viewBinding?.loginCreatePortalFirstNameLayout?.isErrorEnabled = false
            viewBinding?.loginCreatePortalLastNameLayout?.isErrorEnabled = false
            val address = viewBinding?.loginCreatePortalAddressEdit?.text.toString()
            val email = viewBinding?.loginCreatePortalEmailEdit?.text.toString()
            val first = viewBinding?.loginCreatePortalFirstNameEdit?.text.toString()
            val last = viewBinding?.loginCreatePortalLastNameEdit?.text.toString()
            if (isAlphaNumeric(address)) {
                viewBinding?.loginCreatePortalAddressEditLayout?.isErrorEnabled = false
            }
            when {
                ("" != first) and isCreateUserName(first) -> {
                    viewBinding?.loginCreatePortalFirstNameLayout?.error = getString(R.string.errors_first_name)
                    viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
                }
                ("" != last) and isCreateUserName(last) -> {
                    viewBinding?.loginCreatePortalLastNameLayout?.error = getString(R.string.errors_last_name)
                    viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
                }
                else -> viewBinding?.loginSigninCreatePortalButton?.isEnabled =
                    "" != address && "" != email && "" != first && "" != last && isAlphaNumeric(
                        address
                    )
            }
        }
    }

    private inner class FieldsFilter : BaseInputFilter() {
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            super.filter(source, start, end, dest, dstart, dend)
            if (mCreatePortalPresenter.checkPhrase(mResultString)) {
                return null
            }

            return if (!isAlphaNumeric(mResultString)) {
                viewBinding?.loginCreatePortalAddressEditLayout?.error = getString(R.string.login_api_portal_name_content)
                source
            } else {
                viewBinding?.loginCreatePortalAddressEditLayout?.isErrorEnabled = false
                setEditHintVisibility(true)
                null
            }
        }
    }
}