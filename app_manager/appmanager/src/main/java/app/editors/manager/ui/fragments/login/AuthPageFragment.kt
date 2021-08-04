package app.editors.manager.ui.fragments.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.documents.core.network.models.login.request.RequestSignIn
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentAuthPageBinding
import app.editors.manager.managers.receivers.SmsReceiver
import app.editors.manager.mvp.presenters.login.EnterpriseAppAuthPresenter
import app.editors.manager.mvp.views.login.EnterpriseAppView
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.presenter.InjectPresenter

class AuthPageFragment : BaseAppFragment(), EnterpriseAppView {

    companion object {
        val TAG: String = AuthPageFragment::class.java.simpleName
        var SECRET_LABEL = "SECRET_LABEL"

        private const val KEY_POSITION = "KEY_POSITION"
        private const val KEY_TITLE = "KEY_TITLE"
        private const val GOOGLE_AUTHENTICATOR_URL = "com.google.android.apps.authenticator2"
        private const val CODE_PLACEHOLDER = "−"
        private const val PATTERN_CODE = "^[a-zA-Z0-9_-]*$"

        @JvmStatic
        fun newInstance(position: Int, request: String, key: String): AuthPageFragment {
            return AuthPageFragment().apply {
                arguments = Bundle().apply {
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
        if (supportActionBar != null && supportActionBar?.title != null) {
            outState.putString(KEY_TITLE, supportActionBar?.title.toString())
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

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_TITLE)) {
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
        viewBinding?.authPageHeader?.text = getString(R.string.auth_header_screen_1)
        viewBinding?.authPageImage?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.image_auth_screen_1
            )
        )

        viewBinding?.authPageInfo?.text = getString(R.string.auth_info_screen_1)
        viewBinding?.authSecretKeyLayout?.visibility = View.GONE
        viewBinding?.confirmButton?.apply {
            visibility = View.VISIBLE
            text = getString(R.string.app_versions_without_release_accept)
            setOnClickListener {
                ActivitiesUtils.showPlayMarket(requireContext(), GOOGLE_AUTHENTICATOR_URL)
            }
        }
    }

    private fun initSecondFragment() {
        viewBinding?.authPageHeader?.text = getString(R.string.auth_header_screen_2)
        viewBinding?.authPageImage?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.image_auth_screen_2
            )
        )
        viewBinding?.authPageInfo?.text = getString(R.string.auth_info_screen_2)
        viewBinding?.authSecretKeyLayout?.visibility = View.GONE
        viewBinding?.confirmButton?.visibility = View.GONE
    }

    private fun initThirdFragment() {
        viewBinding?.authPageHeader?.text = getString(R.string.auth_header_screen_3)
        viewBinding?.authPageImage?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.image_auth_screen_3
            )
        )
        viewBinding?.authPageInfo?.text = getString(R.string.auth_info_screen_3)
        viewBinding?.confirmButton?.visibility = View.GONE
        viewBinding?.authSecretKeyLayout?.visibility = View.VISIBLE
        viewBinding?.authSecretKeyEditText?.setText(arguments?.getString(AuthAppActivity.TFA_KEY))
        viewBinding?.authCopyButton?.setOnClickListener {
            if (!viewBinding?.authSecretKeyEditText?.text.isNullOrEmpty()) {
                KeyboardUtils.setDataToClipboard(
                    requireContext(),
                    viewBinding?.authSecretKeyEditText?.text.toString(),
                    SECRET_LABEL
                )
            }
        }
    }

    private fun initFourthFragment() {
        codeListener = FieldsWatch()
        viewBinding?.authPageHeader?.text = getString(R.string.auth_header_screen_4)
        viewBinding?.authPageImage?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.image_auth_screen_4
            )
        )
        viewBinding?.authPageInfo?.text = getString(R.string.auth_info_screen_4)
        viewBinding?.authCopyButton?.visibility = View.GONE
        viewBinding?.authCodeEdit?.apply {
            visibility = View.VISIBLE
            addTextChangedListener(codeListener)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && viewBinding?.confirmButton?.isEnabled == true) {
                    viewBinding?.confirmButton?.callOnClick()
                }
                return@setOnEditorActionListener true
            }
        }

        viewBinding?.authSecretKeyLayout?.visibility = View.GONE
        viewBinding?.confirmButton?.apply {
            visibility = View.VISIBLE
            isEnabled = false
            text = getString(R.string.on_boarding_next_button)
            setOnClickListener {
                showWaitingDialog(getString(R.string.dialogs_wait_title))
                presenter.signInPortal(
                    viewBinding?.authCodeEdit?.text.toString(),
                    arguments?.getString(AuthAppActivity.REQUEST_KEY)
                )
            }
        }
    }

    fun onPageSelected(position: Int) {
        this.position = position
        if (position == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
            checkCode()
        } else if (position == AuthPagerFragment.KEY_THIRD_FRAGMENT) {
            Handler(Looper.getMainLooper()).postDelayed({ openAuth() }, 1000)
        }
    }

    private fun openAuth() {
        val request = Json.decodeFromString<RequestSignIn>(arguments?.getString(AuthAppActivity.REQUEST_KEY) ?: "")
        val settings = App.getApp().appComponent.networkSettings
        try {
            val uri =
                "otpauth://totp/" + request.userName + "?secret=" + key + "&issuer= " + settings.getPortal()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.d(TAG, "openAuth: " + e.message)
        }
    }

    private fun checkCode() {
        val code = KeyboardUtils.getTextFromClipboard(requireContext())
        if (code.isNotEmpty() && code.length == 6 && fragmentPosition == AuthPagerFragment.KEY_FOURTH_FRAGMENT) {
            viewBinding?.authCodeEdit?.setText(code)
            clearClipboard()
            Handler(Looper.getMainLooper()).postDelayed({ viewBinding?.confirmButton?.callOnClick() }, 500)
        }
    }

    override fun onSuccessLogin() {
        hideDialog()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onError(message: String?) {
        hideDialog()
        showSnackBar(message!!)
    }

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
                if ("" != subString) {
                    selectPosition = if (count == 6) {
                        0
                    } else {
                        start + count
                    }
                    srcString?.replace(start, start + count, subString ?: "")
                } else {
                    val repeat = StringUtils.repeatString(CODE_PLACEHOLDER, before)
                    selectPosition = start
                    srcString?.replace(start, start + before, repeat)
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            var resultString = srcString.toString()
            if (resultString.length > 6) {
                resultString = KeyboardUtils.getTextFromClipboard(context!!)
            }
            viewBinding?.authCodeEdit?.apply {
                removeTextChangedListener(codeListener)
                setText(resultString)
                setSelection(selectPosition)
                addTextChangedListener(codeListener)
            }
            viewBinding?.confirmButton?.isEnabled = resultString.matches(PATTERN_CODE.toRegex())
        }
    }
}