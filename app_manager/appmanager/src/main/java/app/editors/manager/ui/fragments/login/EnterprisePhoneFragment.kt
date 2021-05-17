package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentLoginEnterprisePhoneBinding
import app.editors.manager.managers.tools.CountriesCodesTool
import app.editors.manager.mvp.presenters.login.EnterprisePhonePresenter
import app.editors.manager.mvp.views.login.EnterprisePhoneView
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import moxy.presenter.InjectPresenter
import java.util.*
import javax.inject.Inject

class EnterprisePhoneFragment : BaseAppFragment(), EnterprisePhoneView {


    companion object {
        @JvmField
        val TAG: String = EnterprisePhoneFragment::class.java.simpleName
        private const val TAG_CODE = "TAG_CODE"
        private const val TAG_NAME = "TAG_NAME"
        private const val TAG_REGION = "TAG_REGION"
        private const val TAG_REQUEST = "TAG_REQUEST"

        fun newInstance(request: String?): EnterprisePhoneFragment {
            return EnterprisePhoneFragment().apply {
                arguments = Bundle().apply {
                    putString(TAG_REQUEST, request)
                }
            }
        }

        fun newInstance(code: Int, name: String?, region: String?): EnterprisePhoneFragment {
            return EnterprisePhoneFragment().apply {
                arguments = Bundle().apply {
                    putInt(TAG_CODE, code)
                    putString(TAG_NAME, name)
                    putString(TAG_REGION, region)
                }
            }
        }
    }

    @JvmField
    @Inject
    var mCountriesCodesTool: CountriesCodesTool? = null

    @InjectPresenter
    lateinit var mEnterprisePhonePresenter: EnterprisePhonePresenter

    private var mCountryCode = 0
    private var mCountryName: String? = null
    private var mCountryRegion: String? = null

    private var viewBinding: FragmentLoginEnterprisePhoneBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
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

    override fun onDestroyView() {
        super.onDestroyView()
        mEnterprisePhonePresenter.cancelRequest()
    }


    private fun sendSmsClick() {
        val phoneNumber = viewBinding?.loginPhoneNumberEdit?.text.toString().trim { it <= ' ' }.replace(" ", "")
        val validNumber = mCountriesCodesTool?.getPhoneE164(phoneNumber, mCountryRegion)
        if (validNumber != null) {
            showWaitingDialog(getString(R.string.dialogs_wait_title))
            arguments?.getString(TAG_REQUEST)?.let { mEnterprisePhonePresenter.setPhone(validNumber, it) }
        } else {
            val message = getString(R.string.login_sms_phone_error_format)
            viewBinding?.loginPhoneNumberLayout?.error = message
        }
    }


    private fun actionKeyPress(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            sendSmsClick()
            return true
        }
        return false
    }

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message!!)
    }

    override fun onSuccessChange(request: String) {
        hideDialog()
        showFragment(
            EnterpriseSmsFragment.newInstance(false, request),
            EnterpriseSmsFragment.TAG,
            false
        )
    }

    private fun init() {
        setActionBarTitle(getString(R.string.login_sms_phone_number_verification))
        initListeners()
        val codes = mCountriesCodesTool!!.getCodeByRegion(Locale.getDefault().country)
        if (codes != null) {
            mCountryCode = codes.mNumber
            mCountryName = codes.mName
            mCountryRegion = codes.mCode
        }
        showKeyboard(viewBinding?.loginPhoneNumberEdit)
        viewBinding?.loginPhoneNumberEdit?.setText("+$mCountryCode")
        viewBinding?.loginPhoneCountryEdit?.apply {
            setText(mCountryName)
            keyListener = null
        }
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey(TAG_CODE) && bundle.containsKey(TAG_NAME) && bundle.containsKey(
                    TAG_REGION
                )
            ) {
                mCountryCode = bundle.getInt(TAG_CODE)
                mCountryName = bundle.getString(TAG_NAME)
                mCountryRegion = bundle.getString(TAG_REGION)
                viewBinding?.loginPhoneCountryEdit?.setText(mCountryName)
                viewBinding?.loginPhoneNumberEdit?.setText("+$mCountryCode")
            }
        }
        val position = viewBinding?.loginPhoneNumberEdit?.text.toString().length
        viewBinding?.loginPhoneNumberEdit?.setSelection(position)
    }

    private fun initListeners() {
        viewBinding?.loginPhoneSendButton?.setOnClickListener {
            sendSmsClick()
        }

        viewBinding?.loginPhoneNumberEdit?.setOnEditorActionListener { v, actionId, event ->
            actionKeyPress(v, actionId, event)
        }

        viewBinding?.loginPhoneCountryEdit?.setOnClickListener { v: View? ->
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
    }

    /*
     * Phone edit field
     * */
    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.loginPhoneNumberLayout!!.isErrorEnabled = false
        }
    }
}