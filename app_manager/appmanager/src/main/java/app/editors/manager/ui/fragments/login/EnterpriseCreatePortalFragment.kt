package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentLoginEnterpriseCreatePortalBinding
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseInputFilter
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.CreatePortalState
import app.editors.manager.viewModels.login.EnterpriseCreateValidateViewModel
import lib.toolkit.base.managers.utils.StringUtils.isAlphaNumeric
import lib.toolkit.base.managers.utils.StringUtils.isCreateUserName
import lib.toolkit.base.managers.utils.UiUtils.measureTextSizes
import lib.toolkit.base.ui.dialogs.common.CommonDialog.Dialogs

class EnterpriseCreatePortalFragment : BaseAppFragment() {

    companion object {
        val TAG: String = EnterpriseCreatePortalFragment::class.java.simpleName
        fun newInstance(): EnterpriseCreatePortalFragment {
            return EnterpriseCreatePortalFragment()
        }
    }

    private val viewModel by viewModels<EnterpriseCreateValidateViewModel>()

    private var viewBinding: FragmentLoginEnterpriseCreatePortalBinding? = null

    private var fieldsWatcher: FieldsWatcher? = null
    private var paddingTop = 0
    private var paddingLeft = 0
    private var paddingRight = 0
    private var paddingBottom = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        viewModel.validatePortal(address, email, first, last)
    }


    private fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onNextClick()
            return true
        }
        return false
    }

    override fun onCancelClick(dialogs: Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        viewModel.cancelRequest()
    }

    fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(message) }
    }

    private fun onValidatePortalSuccess(email: String?, first: String?, last: String?) {
        hideDialog()
        showFragment(
            EnterpriseCreateSignInFragment.newInstance(email, first, last),
            EnterpriseCreateSignInFragment.TAG, false
        )
    }

    private fun onPortalNameError(message: String) {
        setEditHintVisibility(false)
        viewBinding?.loginCreatePortalAddressEditLayout?.error = message
    }

    private fun onEmailNameError(message: String) {
        viewBinding?.loginCreatePortalEmailLayout?.error = message
    }

    private fun onFirstNameError(message: String) {
        viewBinding?.loginCreatePortalFirstNameLayout?.error = message
    }

    private fun onLastNameError(message: String) {
        viewBinding?.loginCreatePortalLastNameLayout?.error = message
    }

    private fun onRegionDomain(domain: String) {
        val textWidth = measureTextSizes(
            domain + "X", viewBinding?.loginCreatePortalAddressHintEnd?.textSize?.toInt() ?: -1
        ).x
        viewBinding?.loginCreatePortalAddressHintEnd?.apply {
            layoutParams.width = textWidth
            text = domain
        }
        paddingRight = textWidth
    }

    private fun onShowWaitingDialog(@StringRes title: Int) {
        showWaitingDialog(getString(title))
    }

    private fun init() {
        fieldsWatcher = FieldsWatcher()
        initListeners()
        setActionBarTitle(getString(R.string.login_create_portal_title))
        showKeyboard(viewBinding?.loginCreatePortalAddressEdit)
        viewBinding?.loginSigninCreatePortalButton?.isEnabled = false
        viewBinding?.loginCreatePortalAddressEdit?.apply {
            requestFocus()
            filters = arrayOf<InputFilter>(FieldsFilter())
        }
        setPadding()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            onError(error.message)
        }
        viewModel.regionLiveData.observe(viewLifecycleOwner) { domain ->
            onRegionDomain(domain ?: "")
        }
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreatePortalState.Success -> {
                    onValidatePortalSuccess(
                        state.portalModel.email,
                        state.portalModel.firstName,
                        state.portalModel.lastName
                    )
                }
                is CreatePortalState.Error -> {
                    checkError(state.res)
                }
                is CreatePortalState.Progress -> {
                    onShowWaitingDialog(R.string.dialogs_wait_title)
                }
            }
        }
        viewModel.getDomain()
    }

    private fun checkError(errorMessage: Int?) {
        errorMessage?.let { message ->
            when (message) {
                R.string.login_api_portal_name_length -> {
                    onPortalNameError(getString(message))
                }
                R.string.errors_email_syntax_error -> {
                    onEmailNameError(getString(message))
                }
                R.string.errors_first_name -> {
                    onFirstNameError(getString(message))
                }
                R.string.errors_last_name -> {
                    onLastNameError(getString(message))
                }
                else -> onError(getString(message))
            }
        } ?: onError(null)
    }

    private fun initListeners() {
        viewBinding?.loginSigninCreatePortalButton?.setOnClickListener {
            onNextClick()
        }

        viewBinding?.loginCreatePortalLastNameEdit?.setOnEditorActionListener { v, actionId, event ->
            actionKeyPress(v, actionId, event)
        }
        viewBinding?.loginCreatePortalAddressEdit?.addTextChangedListener(fieldsWatcher)
        viewBinding?.loginCreatePortalEmailEdit?.addTextChangedListener(fieldsWatcher)
        viewBinding?.loginCreatePortalFirstNameEdit?.addTextChangedListener(fieldsWatcher)
        viewBinding?.loginCreatePortalLastNameEdit?.addTextChangedListener(fieldsWatcher)
    }

    private fun setPadding() {
        paddingTop = viewBinding?.loginCreatePortalAddressEdit?.paddingTop ?: 0
        paddingLeft = viewBinding?.loginCreatePortalAddressEdit?.paddingLeft ?: 0
        paddingRight = viewBinding?.loginCreatePortalAddressEdit?.paddingRight ?: 0
        paddingBottom = viewBinding?.loginCreatePortalAddressEdit?.paddingBottom ?: 0
    }

    private fun setEditHintVisibility(isVisible: Boolean) {
        if (isVisible) {
            viewBinding?.loginCreatePortalAddressHintEnd?.apply {
                visibility = View.VISIBLE
                setPadding(
                    paddingLeft,
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
            }
        } else {
            viewBinding?.loginCreatePortalAddressHintEnd?.apply {
                visibility = View.GONE
                setPadding(paddingLeft, paddingTop, 0, paddingBottom)
            }
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
            if (viewModel.checkPhrase(mResultString)) {
                return null
            }
            return if (!isAlphaNumeric(mResultString)) {
                viewBinding?.loginCreatePortalAddressEditLayout?.error =
                    getString(R.string.login_api_portal_name_content)
                source
            } else {
                viewBinding?.loginCreatePortalAddressEditLayout?.isErrorEnabled = false
                setEditHintVisibility(true)
                null
            }
        }
    }
}