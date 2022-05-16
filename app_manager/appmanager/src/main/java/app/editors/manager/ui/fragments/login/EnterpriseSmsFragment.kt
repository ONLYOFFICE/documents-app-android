package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentLoginEnterpriseSmsBinding
import app.editors.manager.managers.receivers.SmsReceiver
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.presenters.login.EnterpriseSmsPresenter
import app.editors.manager.mvp.views.login.EnterpriseSmsView
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.custom.TimerViews
import app.editors.manager.ui.views.edits.BaseEditText.OnContextMenu
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.managers.utils.StringUtils.repeatString
import moxy.presenter.InjectPresenter
import javax.inject.Inject

class EnterpriseSmsFragment : BaseAppFragment(), EnterpriseSmsView, OnContextMenu {

    companion object {
        val TAG: String = EnterpriseSmsFragment::class.java.simpleName

        const val TAG_TIMER = "TAG_TIMER"
        const val TAG_SMS = "TAG_SMS"
        const val TAG_REQUEST = "TAG_ACCOUNT"

        private const val SMS_CODE_PLACEHOLDER = "−"
        private const val PATTER_DIGITS = ".*\\d+.*"
        private const val PATTERN_NUMERIC = "^[0-9]*$"
        private const val RESEND_TIMER = 30

        fun newInstance(isSms: Boolean, request: String?): EnterpriseSmsFragment {
            return EnterpriseSmsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(TAG_SMS, isSms)
                    putString(TAG_REQUEST, request)
                }
            }
        }
    }

    @Inject
    lateinit var preferenceTool: PreferenceTool

    @InjectPresenter
    lateinit var enterpriseSmsPresenter: EnterpriseSmsPresenter

    private var viewBinding: FragmentLoginEnterpriseSmsBinding? = null

    private var smsInputWatch: FieldsWatch? = null
    private var timerViews: TimerViews? = null


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
        viewBinding = FragmentLoginEnterpriseSmsBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setDataFromClipboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerViews?.removeFragment()
        timerViews?.cancel(true)
        enterpriseSmsPresenter.cancelRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TAG_TIMER, timerViews!!.currentTimer)
    }

    private fun resendSmsClick() {
        hideKeyboard(viewBinding?.loginSmsCodeEdit)
        startTimer(RESEND_TIMER)
        arguments?.getString(TAG_REQUEST)?.let { enterpriseSmsPresenter.resendSms(it) }
    }

    private fun changeNumberClick() {
        showFragment(
            EnterprisePhoneFragment.newInstance(arguments?.getString(TAG_REQUEST)),
            EnterprisePhoneFragment.TAG,
            false
        )
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

    override fun onResendSms() {
        showSnackBar(R.string.login_sms_resend_ok)
    }

    override fun onTextPaste(text: String?): Boolean {
        if (text != null) {
            val code = SmsReceiver.getCodeFromSms(text)
            if (code.isNotEmpty()) {
                viewBinding?.loginSmsCodeEdit?.setText(code)
                return true
            }
        }
        return false
    }

    private fun init(savedInstanceState: Bundle?) {
        setActionBarTitle(getString(R.string.login_sms_phone_header))
        smsInputWatch = FieldsWatch()
        initListeners()
        viewBinding?.loginSmsCodeNumberText?.text = preferenceTool.phoneNoise.toString()
        viewBinding?.loginSmsCodeEdit?.apply {
            setTextColor(viewBinding?.loginSmsCodeEdit?.hintTextColors)
            isCursorVisible = false
            setOnContextMenu(this@EnterpriseSmsFragment)
        }
        viewBinding?.loginSmsCodeChangeNumberButton?.visibility = View.INVISIBLE
        showKeyboard(viewBinding?.loginSmsCodeEdit)
        restoreStates(savedInstanceState)
        if (savedInstanceState == null) {
            clearClipboard()
        }
    }

    private fun initListeners() {
        viewBinding?.loginSmsCodeSendAgainButton?.setOnClickListener {
            resendSmsClick()
        }

        viewBinding?.loginSmsCodeChangeNumberButton?.setOnClickListener {
            changeNumberClick()
        }
        viewBinding?.loginSmsCodeEdit?.apply {
            addTextChangedListener(smsInputWatch)
            setOnClickListener(SetSelectionOnClick())
        }
    }

    private val args: Unit
        get() {
            val bundle = arguments
            if (bundle!!.getBoolean(TAG_SMS)) {
                viewBinding?.loginSmsCodeChangeNumberButton?.visibility = View.VISIBLE
                bundle.getString(TAG_REQUEST)?.let { enterpriseSmsPresenter.resendSms(it) }
            }
        }

    private fun restoreStates(savedInstanceState: Bundle?) {
        var timer = RESEND_TIMER
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_TIMER)) {
                timer = savedInstanceState.getInt(TAG_TIMER)
            }
        } else {
            viewBinding?.loginSmsCodeEdit?.setSelection(0)
        }
        startTimer(timer)
    }

    /*
     * Get data from clipboard
     * */
    private fun setDataFromClipboard() {
        val data = dataFromClipboard
        if (SmsReceiver.isSmsCode(data)) {
            viewBinding?.loginSmsCodeEdit?.apply {
                setText(SmsReceiver.getCodeFromSms(data))
                performClick()
            }
        }
    }

    private fun startTimer(timer: Int) {
        if (timerViews == null || timerViews!!.isCancelled || !timerViews!!.isActive) {
            timerViews = TimerViews(timer)
            timerViews?.setFragment(this)
            timerViews?.execute()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setTimerButton(timer: Int) {
        viewBinding?.loginSmsCodeSendAgainButton?.apply {
            isEnabled = false
            text = getString(R.string.login_sms_send_again_after) + " " + timer
        }
    }

    fun setTimerButton() {
        viewBinding?.loginSmsCodeSendAgainButton?.apply {
            isEnabled = true
            text = getString(R.string.login_sms_send_again)
        }
    }

    /*
     * Set selection on click
     * */
    private inner class SetSelectionOnClick : View.OnClickListener {
        override fun onClick(v: View) {
            val string = viewBinding?.loginSmsCodeEdit?.text.toString()
            for (i in string.indices) {
                if (Character.isDigit(string[i])) {
                    viewBinding?.loginSmsCodeEdit?.setSelection(i + 1)
                } else {
                    viewBinding?.loginSmsCodeEdit?.setSelection(i)
                    break
                }
            }
        }
    }

    /*
     * Edit sms code controller
     * * */
    private inner class FieldsWatch : BaseWatcher() {
        private var srcString: StringBuilder? = null
        private var subString: String? = null
        private var selectPosition = 0

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            srcString = StringBuilder(s)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (start < SmsReceiver.SMS_CODE_LENGTH && count <= SmsReceiver.SMS_CODE_LENGTH) {
                subString = s.subSequence(start, start + count).toString()
                // Input symbol or delete
                if ("" != subString) {
                    selectPosition = start + count
                    srcString?.replace(start, start + count, subString ?: "")
                } else {
                    val repeat = repeatString(SMS_CODE_PLACEHOLDER, before)
                    selectPosition = start
                    srcString?.replace(start, start + before, repeat)
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            // Result text
            val resultString = srcString.toString()
//            if (resultString.matches(PATTER_DIGITS.toRegex())) {
//                viewBinding?.loginSmsCodeEdit?.setTextColor(resources.getColor(android.R.color.black, null))
//            } else {
//                viewBinding?.loginSmsCodeEdit?.setTextColor(viewBinding?.loginSmsCodeEdit?.hintTextColors)
//            }

            // Remove listener, else will be recursion
            viewBinding?.loginSmsCodeEdit?.apply {
                removeTextChangedListener(smsInputWatch)
                setText(resultString)
                setSelection(selectPosition)
                addTextChangedListener(smsInputWatch)
            }

            // Check length of sms code
            if (resultString.matches(PATTERN_NUMERIC.toRegex())) {
                showWaitingDialog(getString(R.string.dialogs_wait_title))
                enterpriseSmsPresenter.signInPortal(
                    resultString, arguments?.getString(
                        TAG_REQUEST
                    ) ?: ""
                )
            }
        }
    }
}