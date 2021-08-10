package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import app.editors.manager.R
import app.editors.manager.app.appComponent
import app.editors.manager.app.loginComponent
import app.editors.manager.databinding.FragmentLoginEnterprisePhoneBinding
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.viewModels.login.EnterprisePhoneState
import app.editors.manager.viewModels.login.EnterprisePhoneViewModel
import app.editors.manager.viewModels.login.EnterprisePhoneViewModelFactory
import javax.inject.Inject

class EnterprisePhoneFragment : BaseAppFragment() {

    companion object {
        @JvmField
        val TAG: String = EnterprisePhoneFragment::class.java.simpleName

        private const val TAG_CODE = "TAG_CODE"
        private const val TAG_NAME = "TAG_NAME"
        private const val TAG_REGION = "TAG_REGION"
        private const val TAG_REQUEST = "TAG_REQUEST"

        fun newInstance(request: String?): EnterprisePhoneFragment {
            return EnterprisePhoneFragment().apply {
                arguments = Bundle(1).apply {
                    putString(TAG_REQUEST, request)
                }
            }
        }

        fun newInstance(code: Int, name: String?, region: String?): EnterprisePhoneFragment {
            return EnterprisePhoneFragment().apply {
                arguments = Bundle(3).apply {
                    putInt(TAG_CODE, code)
                    putString(TAG_NAME, name)
                    putString(TAG_REGION, region)
                }
            }
        }
    }

    private val viewModel by viewModels<EnterprisePhoneViewModel> {
        EnterprisePhoneViewModelFactory(requireContext().loginComponent.loginService)
    }

    @Inject
    lateinit var countriesCodesTool: CountriesCodesTool

    private var countryCode = 0
    private var countryName: String? = null
    private var countryRegion: String? = null

    private var viewBinding: FragmentLoginEnterprisePhoneBinding? = null

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(viewModel)
        requireContext().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentLoginEnterprisePhoneBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun sendSmsClick() {
        val phoneNumber = viewBinding?.loginPhoneNumberEdit?.text.toString().trim { it <= ' ' }.replace(" ", "")
        val validNumber = countriesCodesTool.getPhoneE164(phoneNumber, countryRegion)
        if (validNumber != null) {
            showWaitingDialog(getString(R.string.dialogs_wait_title))
            arguments?.getString(TAG_REQUEST)?.let { viewModel.setPhone(validNumber, it) }
        } else {
            val message = getString(R.string.login_sms_phone_error_format)
            viewBinding?.loginPhoneNumberLayout?.error = message
        }
    }


    private fun actionKeyPress(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            sendSmsClick()
            return true
        }
        return false
    }

    private fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    private fun onSuccessChange(request: String) {
        hideDialog()
        showFragment(
            EnterpriseSmsFragment.newInstance(false, request),
            EnterpriseSmsFragment.TAG,
            false
        )
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        setActionBarTitle(getString(R.string.login_sms_phone_number_verification))
        initListeners()
        viewModel.countiesCodes.observe(viewLifecycleOwner) { codes: CountriesCodesTool.Codes? ->
            codes?.let {
                countryCode = codes.number
                countryName = codes.name
                countryRegion = codes.code
                showKeyboard(viewBinding?.loginPhoneNumberEdit)
                viewBinding?.loginPhoneNumberEdit?.setText("+$countryCode")
                viewBinding?.loginPhoneCountryEdit?.apply {
                    setText(countryName)
                    keyListener = null
                }
            }
        }
//        val codes = countriesCodesTool.getCodeByRegion(Locale.getDefault().country)
//        if (codes != null) {
//            countryCode = codes.number
//            countryName = codes.name
//            countryRegion = codes.code
//        }
//        showKeyboard(viewBinding?.loginPhoneNumberEdit)
//        viewBinding?.loginPhoneNumberEdit?.setText("+$countryCode")
//        viewBinding?.loginPhoneCountryEdit?.apply {
//            setText(countryName)
//            keyListener = null
//        }
//        val bundle = arguments
//        if (bundle != null) {
//            if (bundle.containsKey(TAG_CODE) && bundle.containsKey(TAG_NAME) && bundle.containsKey(
//                    TAG_REGION
//                )
//            ) {
//                countryCode = bundle.getInt(TAG_CODE)
//                countryName = bundle.getString(TAG_NAME)
//                countryRegion = bundle.getString(TAG_REGION)
//                viewBinding?.loginPhoneCountryEdit?.setText(countryName)
//                viewBinding?.loginPhoneNumberEdit?.setText("+$countryCode")
//            }
//        }
        val position = viewBinding?.loginPhoneNumberEdit?.text.toString().length
        viewBinding?.loginPhoneNumberEdit?.setSelection(position)
    }

    private fun initListeners() {
        viewBinding?.loginPhoneSendButton?.setOnClickListener {
            sendSmsClick()
        }

        viewBinding?.loginPhoneNumberEdit?.setOnEditorActionListener { _, actionId, _ ->
            actionKeyPress(actionId)
        }

        viewBinding?.loginPhoneCountryEdit?.setOnClickListener {
            showFragment(
                CountriesCodesFragment.newInstance(),
                CountriesCodesFragment.TAG,
                false
            )
        }

        viewBinding?.loginPhoneNumberEdit?.apply {
            addTextChangedListener(PhoneNumberFormattingTextWatcher())
            addTextChangedListener(FieldsWatcher())
        }

        viewModel.stateLiveData.observe(viewLifecycleOwner) { state: EnterprisePhoneState? ->
            when (state) {
                is EnterprisePhoneState.Success -> {
                    onSuccessChange(state.request)
                }
                is EnterprisePhoneState.Error -> {
                    onError(state.message)
                }
            }
        }
    }

    /*
     * Phone edit field
     * */
    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginPhoneNumberLayout?.isErrorEnabled = false
        }
    }
}