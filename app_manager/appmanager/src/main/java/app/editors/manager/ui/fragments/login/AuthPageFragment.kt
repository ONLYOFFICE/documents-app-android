package app.editors.manager.ui.fragments.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.documents.core.model.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.databinding.FragmentAuthPageBinding
import app.editors.manager.managers.receivers.SmsReceiver
import app.editors.manager.mvp.presenters.login.EnterpriseAppAuthPresenter
import app.editors.manager.mvp.views.login.EnterpriseAppView
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.presenter.InjectPresenter
import androidx.core.net.toUri

class AuthPageFragment : BaseAppFragment(), EnterpriseAppView {

    companion object {
        val TAG: String = AuthPageFragment::class.java.simpleName
        var SECRET_LABEL = "SECRET_LABEL"

        private const val KEY_POSITION = "KEY_POSITION"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val GOOGLE_AUTHENTICATOR_URL = "com.google.android.apps.authenticator2"
        private const val CODE_PLACEHOLDER = "−"
        private const val PATTERN_CODE = "^[a-zA-Z0-9_-]*$"
        private const val PLACEHOLDER_MASK = "−−− −−−"

        @JvmStatic
        fun newInstance(position: Int, request: String, key: String): AuthPageFragment {
            return AuthPageFragment().apply {
                arguments = Bundle(3).apply {
                    putInt(KEY_POSITION, position)
                    putString(AuthAppActivity.REQUEST_KEY, request)
                    putString(AuthAppActivity.TFA_KEY, key)
                }
            }
        }
    }

    private var viewBinding: FragmentAuthPageBinding? = null

    private var fragmentPosition = 0
    private var codeListener: FieldsWatch? = null
    private var position = 0
    private var key = ""

    @InjectPresenter
    lateinit var presenter: EnterpriseAppAuthPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fragmentPosition = it.getInt(KEY_POSITION)
            key = it.getString(AuthAppActivity.TFA_KEY) ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        if (position == AuthPagerFragment.KEY_FOURTH_FRAGMENT || parentFragment == null) {
            checkCode()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            checkCode()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportActionBar?.let {
            outState.putString(KEY_TITLE, it.title.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentAuthPageBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragment(savedInstanceState)
    }

    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onError(message: String?) {
        hideDialog()
        message?.let { showSnackBar(it) }
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState?.containsKey(KEY_TITLE) == true) {
            setActionBarTitle(savedInstanceState.getString(KEY_TITLE))
        }
        when (fragmentPosition) {
            AuthPagerFragment.KEY_FIRST_FRAGMENT -> initFirstFragment()
            AuthPagerFragment.KEY_SECOND_FRAGMENT -> initSecondFragment()
            AuthPagerFragment.KEY_THIRD_FRAGMENT -> initThirdFragment()
            AuthPagerFragment.KEY_FOURTH_FRAGMENT -> initFourthFragment()
        }
    }

    private fun initFirstFragment() {
        val fragmentCount = (parentFragment as AuthPagerFragment).fragmentCount
        viewBinding?.let { binding ->
            binding.authPageHeader.text = getString(R.string.auth_header_screen_1)
            binding.authPageStep.text = getString(R.string.auth_step_title, 1, fragmentCount)
            binding.authPageInfo.text = getString(R.string.auth_info_screen_1)
            binding.authSecretKeyLayout.isVisible = false
            binding.authPageImage
                .setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.drawable_image_auth_screen_1))
            binding.confirmButton.apply {
                isVisible = true
                text = context.getString(R.string.auth_install_button)
                setOnClickListener {
                    ActivitiesUtils.showPlayMarket(requireContext(), GOOGLE_AUTHENTICATOR_URL)
                }
            }
        }
    }

    private fun initSecondFragment() {
        viewBinding?.let { binding ->
            binding.authPageHeader.text = getString(R.string.auth_header_screen_2)
            binding.authPageInfo.text = getString(R.string.auth_info_screen_2)
            binding.authSecretKeyLayout.isVisible = false
            binding.confirmButton.isVisible = false
            binding.authPageImage
                .setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.drawable_image_auth_screen_2))
        }
    }

    private fun initThirdFragment() {
        viewBinding?.let { binding ->
            binding.authPageHeader.text = getString(R.string.auth_header_screen_3)
            binding.authPageInfo.text = getString(R.string.auth_info_screen_3)
            binding.confirmButton.isVisible = false
            binding.authSecretKeyLayout.isVisible = true
            binding.authPageImage
                .setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.drawable_image_auth_screen_3))
            binding.authSecretKeyEditText.apply {
                arguments?.getString(AuthAppActivity.TFA_KEY)?.let { key ->
                    setText(key.putSpace(4))
                }
                setTextIsSelectable(true)
                keyListener = null
            }
            binding.authSecretKeyLayout.setEndIconOnClickListener {
                if (!binding.authSecretKeyEditText.text.isNullOrEmpty()) {
                    KeyboardUtils.setDataToClipboard(
                        requireContext(),
                        binding.authSecretKeyEditText.text.toString(), SECRET_LABEL
                    )
                    showToast(getString(R.string.auth_clipboard_key_copied))
                }
            }
        }
    }

    private fun initFourthFragment() {
        codeListener = FieldsWatch()
        viewBinding?.let { binding ->
            binding.authPageHeader.text = getString(R.string.auth_header_screen_4)
            binding.authPageInfo.text = getString(R.string.auth_info_screen_4)
            binding.authPageStep.isVisible = key.isNotEmpty()
            binding.authPageImage
                .setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.drawable_image_auth_screen_4))
            binding.authSecretKeyLayout.apply {
                endIconDrawable = null
                isVisible = true
            }

            binding.authSecretKeyEditText.apply {
                var focused = false
                isFocusableInTouchMode = false
                inputType = InputType.TYPE_CLASS_NUMBER
                setHintTextColor(requireContext().getColor(lib.toolkit.base.R.color.colorTextSecondary))
                addTextChangedListener(codeListener)
                setText(PLACEHOLDER_MASK)

                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE && binding.confirmButton.isEnabled) {
                        binding.confirmButton.callOnClick()
                    }
                    return@setOnEditorActionListener true
                }

                setOnClickListener {
                    val indexOfPlaceholder = text.toString().indexOfFirst { it == '−' }
                    if (!focused) {
                        showKeyboard(this)
                        if (indexOfPlaceholder != -1)
                            setSelection(indexOfPlaceholder)
                    } else {
                        if (indexOfPlaceholder != -1)
                            setSelection(indexOfPlaceholder)
                    }
                    isFocusableInTouchMode = true
                    requestFocus()
                }

                setOnFocusChangeListener { _, isFocused ->
                    focused = isFocused
                    isFocusableInTouchMode = isFocused
                }
            }

            binding.confirmButton.apply {
                visibility = View.VISIBLE
                isEnabled = false
                text = getString(R.string.auth_confirm_button)
                setOnClickListener {
                    showWaitingDialog(getString(R.string.dialogs_wait_title))
                    val request = Json.decodeFromString<RequestSignIn>(
                        arguments?.getString(AuthAppActivity.REQUEST_KEY).orEmpty()
                    )
                    if (request.userName.isNotEmpty() && request.password.isNotEmpty()) {
                        presenter.signInWithEmail(
                            request.userName,
                            request.password,
                            binding.authSecretKeyEditText.text.toString().replace(" ", "")
                        )
                    }
                    if (request.provider.isNotEmpty() && !request.accessToken.isNullOrEmpty()) {
                        presenter.signInWithProvider(
                            request.accessToken,
                            request.provider,
                            binding.authSecretKeyEditText.text.toString().replace(" ", "")
                        )
                    }
                }
            }
        }
    }

    private fun openAuth() {
        val request = Json.decodeFromString<RequestSignIn>(arguments?.getString(AuthAppActivity.REQUEST_KEY) ?: "")
        try {
            val uri =
                "otpauth://totp/" + request.userName + "?secret=" + key + "&issuer= " + context?.accountOnline?.portalUrl
            val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.d(TAG, "openAuth: " + e.message)
        }
    }

    private fun checkCode() {
        val code = KeyboardUtils.getTextFromClipboard(requireContext())
        if (code.isNotEmpty() && code.length == 6 && fragmentPosition == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
            viewBinding?.authSecretKeyEditText?.setText(code)
            clearClipboard()
            Handler(Looper.getMainLooper()).postDelayed({ viewBinding?.confirmButton?.callOnClick() }, 500)
        }
    }

    private fun String.putSpace(count: Int) = mapIndexed { index, s ->
        if (index % count == count - 1 && index < length - 1) "$s " else s
    }.joinToString("")

    fun onPageSelected(position: Int, count: Int) {
        this.position = position
        viewBinding?.authPageStep?.text = getString(R.string.auth_step_title, position + 1, count)
        if (position == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
            checkCode()
        } else if (position == AuthPagerFragment.KEY_THIRD_FRAGMENT) {
            Handler(Looper.getMainLooper()).postDelayed({ openAuth() }, 1000)
        }
    }

    private inner class FieldsWatch : BaseWatcher() {

        private var srcString: StringBuilder? = null
        private var subString: String? = null
        private var selectPosition = 0

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            srcString = StringBuilder(s)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (start < SmsReceiver.SMS_CODE_LENGTH + 1 && count <= SmsReceiver.SMS_CODE_LENGTH + 1) {
                val repeat = StringUtils.repeatString(CODE_PLACEHOLDER, before)
                subString = s.subSequence(start, start + count).toString()
                subString?.let {
                    if (it.isNotEmpty()) {
                        if (count == SmsReceiver.SMS_CODE_LENGTH) {
                            srcString?.clear()?.append(subString?.putSpace(3))
                            selectPosition = 0
                        } else {
                            if (start == 3) {
                                selectPosition = start + count + 1
                                srcString?.replace(start + 1, start + count + 1, it)
                            } else {
                                srcString?.replace(start, start + count, it)
                                selectPosition = start + count
                            }
                        }
                    } else {
                        if (before >= 6) {
                            srcString?.clear()?.append(PLACEHOLDER_MASK)
                            selectPosition = 0
                        } else {
                            if (start == 3) {
                                selectPosition = start - 1
                                srcString?.replace(start - 1, start, repeat)
                                srcString?.replace(start, start + before, " ")
                            } else {
                                selectPosition = start
                                srcString?.replace(start, start + before, repeat)
                            }
                        }
                    }
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            val resultString = srcString.toString()
            viewBinding?.authSecretKeyEditText?.apply {
                removeTextChangedListener(codeListener)
                setText(resultString)
                setSelection(selectPosition)
                addTextChangedListener(codeListener)
            }
            viewBinding?.confirmButton?.isEnabled = resultString
                .replace(" ", "")
                .matches(PATTERN_CODE.toRegex())
        }
    }
}