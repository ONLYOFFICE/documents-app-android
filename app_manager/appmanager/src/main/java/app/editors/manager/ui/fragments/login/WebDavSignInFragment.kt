package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.documents.core.network.webdav.WebDavApi
import app.editors.manager.R
import app.editors.manager.databinding.FragmentStorageWebDavBinding
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import app.editors.manager.ui.activities.login.NextCloudLoginActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.edits.BaseWatcher
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class WebDavSignInFragment : BaseAppFragment(), WebDavSignInView {

    companion object {
        val TAG: String = WebDavSignInFragment::class.java.simpleName

        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val KEY_LOGIN = "KEY_LOGIN"
        private const val KEY_PASSWORD = "KEY_PASSWORD"

        fun newInstance(provider: WebDavApi.Providers?): WebDavSignInFragment {
            return WebDavSignInFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_PROVIDER, provider)
                }
            }
        }
    }

    private val textWatcher: TextWatcher by lazy { FieldsWatcher() }
    private lateinit var wevDavProvider: WebDavApi.Providers

    @InjectPresenter
    lateinit var presenter: WebDavSignInPresenter

    private var viewBinding: FragmentStorageWebDavBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wevDavProvider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(KEY_PROVIDER, WebDavApi.Providers::class.java) as WebDavApi.Providers
            } else {
                it.getSerializable(KEY_PROVIDER) as WebDavApi.Providers
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding?.let { view ->
            outState.putString(KEY_PORTAL, view.storageWebDavUrlEdit.text?.toString())
            outState.putString(KEY_LOGIN, view.storageWebDavLoginEdit.text?.toString())
            outState.putString(KEY_PASSWORD, view.storageWebDavPasswordEdit.text?.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        viewBinding = FragmentStorageWebDavBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreState(savedInstanceState)
        initViews()
    }

    private fun restoreState(bundle: Bundle?) {
        bundle?.let { data ->
            viewBinding?.let { view ->
                view.storageWebDavUrlEdit.setText(data.getString(KEY_PORTAL))
                view.storageWebDavLoginEdit.setText(data.getString(KEY_LOGIN))
                view.storageWebDavPasswordEdit.setText(bundle.getString(KEY_PASSWORD))
            }
        }
    }

    private fun initViews() {
        addTextWatcher()
        viewBinding?.storageWebDavTitleLayout?.visibility = View.GONE
        viewBinding?.storageWebDavSaveButton?.isEnabled = false

        when (wevDavProvider) {
            WebDavApi.Providers.Yandex -> {
                initYandexState()
            }
            WebDavApi.Providers.NextCloud -> {
                initNextCloudState()
            }

            WebDavApi.Providers.KDrive -> {
                initKDriveState()
            }
            else -> {
                // Nothing
            }
        }

        initListeners()
    }

    private fun initNextCloudState() {
        viewBinding?.storageWebDavPasswordLayout?.visibility = View.GONE
        viewBinding?.storageWebDavLoginLayout?.visibility = View.GONE
        viewBinding?.storageWebDavLoginEdit?.removeTextChangedListener(textWatcher)
        viewBinding?.storageWebDavPasswordEdit?.removeTextChangedListener(textWatcher)
    }

    @SuppressLint("SetTextI18n")
    private fun initKDriveState() {
        viewBinding?.storageWebDavUrlEdit?.setText("https://connect.drive.infomaniak.com")
        viewBinding?.storageWebDavUrlLayout?.visibility = View.GONE
        viewBinding?.storageWebDavLoginLayout?.hint = getString(R.string.login_enterprise_email_hint)
        viewBinding?.storageWebDavPasswordLayout?.helperText = getString(R.string.krdive_password_helper_text)
        viewBinding?.storageWebDavSaveButton?.text = getString(R.string.storage_email_connection)
        viewBinding?.storageInfoTitle?.apply {
            isVisible = true
            text = getString(R.string.kdrive_info_title)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initYandexState() {
        viewBinding?.storageWebDavUrlEdit?.setText("webdav.yandex.ru/")
        viewBinding?.storageWebDavUrlLayout?.visibility = View.GONE
    }

    private fun addTextWatcher() {
        viewBinding?.let { view ->
            view.storageWebDavUrlEdit.addTextChangedListener(textWatcher)
            view.storageWebDavLoginEdit.addTextChangedListener(textWatcher)
            view.storageWebDavPasswordEdit.addTextChangedListener(textWatcher)
        }
    }

    private fun initListeners() {
        viewBinding?.storageWebDavSaveButton?.setOnClickListener {
            val url = viewBinding?.storageWebDavUrlEdit?.text?.toString()?.trim() ?: ""
            val login = viewBinding?.storageWebDavLoginEdit?.text?.toString()?.trim() ?: ""
            val password = viewBinding?.storageWebDavPasswordEdit?.text?.toString()?.trim() ?: ""
            connect(url, login, password)
        }
    }

    private fun connect(url: String, login: String, password: String) {
        hideKeyboard()
        if (wevDavProvider === WebDavApi.Providers.NextCloud) {
            presenter.checkNextCloud(wevDavProvider, url)
        } else {
            presenter.checkPortal(wevDavProvider, url, login, password)
        }
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        super.onCancelClick(dialogs, tag)
        tag?.let {
            if (dialogs?.equals(CommonDialog.Dialogs.WAITING) == true) {
                presenter.cancelRequest()
            }
        }
    }

    override fun onLogin() {
        onDialogClose()
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onNextCloudLogin(url: String) {
        onDialogClose()
        NextCloudLoginActivity.show(requireActivity(), url)
    }

    override fun onUrlError(string: String) {
        onDialogClose()
        viewBinding?.storageWebDavUrlLayout?.error = string
    }

    override fun onError(message: String?) {
        onDialogClose()
        message?.let { showSnackBar(it) }
    }

    override fun onDialogWaiting(string: String) {
        showWaitingDialog(string)
    }

    override fun onDialogClose() {
        hideDialog()
    }

    private inner class FieldsWatcher : BaseWatcher() {
        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            viewBinding?.storageWebDavUrlLayout?.error = null

            val url = viewBinding?.storageWebDavUrlEdit?.text?.toString()
            val login = viewBinding?.storageWebDavLoginEdit?.text?.toString()
            val password = viewBinding?.storageWebDavPasswordEdit?.text?.toString()

            if (wevDavProvider === WebDavApi.Providers.NextCloud) {
                viewBinding?.storageWebDavSaveButton?.isEnabled = !url.isNullOrEmpty()
            } else {
                viewBinding?.storageWebDavSaveButton?.isEnabled =
                    !url.isNullOrEmpty() && !login.isNullOrEmpty() && !password.isNullOrEmpty()
            }
        }
    }


}