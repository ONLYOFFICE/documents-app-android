package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.R
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import app.editors.manager.ui.activities.login.NextCloudLoginActivity
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.WebDavBaseFragment
import app.editors.manager.ui.interfaces.WebDavInterface
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

class WebDavSignInFragment : WebDavBaseFragment(), WebDavSignInView {

    companion object {
        val TAG: String = WebDavSignInFragment::class.java.simpleName

        private const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val KEY_LOGIN = "KEY_LOGIN"
        private const val KEY_PASSWORD = "KEY_PASSWORD"

        fun newInstance(provider: WebdavProvider?): WebDavSignInFragment {
            return WebDavSignInFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_PROVIDER, provider)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: WebDavSignInPresenter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = try {
            context as WebDavInterface
        } catch (e: ClassCastException) {
            throw RuntimeException(
                WebDavSignInFragment::class.java.simpleName + " - must implement - " +
                        WebDavLoginActivity::class.java.simpleName
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webDavProvider = it.getSerializableExt(KEY_PROVIDER, WebdavProvider::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding?.let { view ->
            outState.putString(KEY_PORTAL, view.storageWebDavServerEdit.text?.toString())
            outState.putString(KEY_LOGIN, view.storageWebDavLoginEdit.text?.toString())
            outState.putString(KEY_PASSWORD, view.storageWebDavPasswordEdit.text?.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreState(savedInstanceState)
    }

    private fun restoreState(bundle: Bundle?) {
        bundle?.let { data ->
            viewBinding?.let { view ->
                view.storageWebDavServerEdit.setText(data.getString(KEY_PORTAL))
                view.storageWebDavLoginEdit.setText(data.getString(KEY_LOGIN))
                view.storageWebDavPasswordEdit.setText(data.getString(KEY_PASSWORD))
            }
        }
    }

    override fun initViews(isNextCloud: Boolean) {
        when (webDavProvider) {
            is WebdavProvider.NextCloud -> initNextCloudState()
            WebdavProvider.Yandex -> initYandexState()
            WebdavProvider.KDrive -> initKDriveState()
            else -> {
                viewBinding?.storageWebDavPasswordEdit?.setActionDoneListener(this::connect)
            }
        }

        viewBinding?.storageWebDavTitleLayout?.isVisible = false
        viewBinding?.connectButton?.setOnClickListener { connect() }
        super.initViews(webDavProvider is WebdavProvider.NextCloud)
    }

    private fun initNextCloudState() {
        viewBinding?.storageWebDavLoginLayout?.isVisible = false
        viewBinding?.storageWebDavPasswordLayout?.isVisible = false
        viewBinding?.storageWebDavServerEdit?.setActionDoneListener(this::connect)
    }

    @SuppressLint("SetTextI18n")
    private fun initKDriveState() {
        viewBinding?.storageWebDavPasswordEdit?.setActionDoneListener(this::connect)
        viewBinding?.storageWebDavServerEdit?.setText("https://connect.drive.infomaniak.com")
        viewBinding?.storageWebDavServerLayout?.isVisible = false
        viewBinding?.storageWebDavLoginLayout?.setHint(R.string.login_enterprise_email_hint)
        viewBinding?.storageInfoSecond?.setText(R.string.krdive_password_helper_text)
        viewBinding?.storageInfoTitle?.setText(R.string.kdrive_info_title)
        viewBinding?.storageInfoSecond?.isVisible = true
        viewBinding?.storageInfoTitle?.isVisible = true
    }

    @SuppressLint("SetTextI18n")
    private fun initYandexState() {
        viewBinding?.storageWebDavServerEdit?.setText("webdav.yandex.ru/")
        viewBinding?.storageWebDavServerLayout?.isVisible = false
    }

    private fun connect() {
        hideKeyboard()
        viewBinding?.let { binding ->
            val url = binding.storageWebDavServerEdit.text?.trim().toString()
            val login = binding.storageWebDavLoginEdit.text?.trim().toString()
            val password = binding.storageWebDavPasswordEdit.text?.trim().toString()

            webDavProvider?.let { provider ->
                if (provider is WebdavProvider.NextCloud) {
                    presenter.checkNextCloud(provider, url)
                } else {
                    presenter.checkPortal(provider, url, login, password)
                }
            }
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
        onServerError()
    }

    override fun onDialogWaiting(string: String) {
        showWaitingDialog(string)
    }

    override fun onDialogClose() {
        hideDialog()
    }
}