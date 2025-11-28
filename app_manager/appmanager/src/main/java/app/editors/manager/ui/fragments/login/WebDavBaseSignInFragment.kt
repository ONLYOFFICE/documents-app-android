package app.editors.manager.ui.fragments.login

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.WebdavProvider
import app.editors.manager.R
import app.editors.manager.mvp.presenters.login.WebDavSignInPresenter
import app.editors.manager.mvp.views.login.WebDavSignInView
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.login.WebViewCloudLoginActivity
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.WebDavBaseFragment
import app.editors.manager.ui.interfaces.WebDavInterface
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import moxy.presenter.InjectPresenter

abstract class WebDavBaseSignInFragment : WebDavBaseFragment(), WebDavSignInView {

    companion object {
        const val KEY_PROVIDER = "KEY_PROVIDER"
        private const val KEY_PORTAL = "KEY_PORTAL"
        private const val KEY_LOGIN = "KEY_LOGIN"
        private const val KEY_PASSWORD = "KEY_PASSWORD"
    }

    @InjectPresenter
    lateinit var presenter: WebDavSignInPresenter

    abstract val showOnlyServer: Boolean

    protected val cloudAccount: CloudAccount? by lazy {
        Json.decodeFromString(arguments?.getString(WebDavLoginActivity.KEY_ACCOUNT) ?: return@lazy null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = try {
            context as WebDavInterface
        } catch (_: ClassCastException) {
            throw RuntimeException(
                WebDavBaseSignInFragment::class.java.simpleName + " - must implement - " +
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

    override fun initViews(onlyServer: Boolean) {
        initFields()
        viewBinding?.storageWebDavTitleLayout?.isVisible = false
        viewBinding?.connectButton?.setOnClickListener { connect() }
        super.initViews(showOnlyServer)

        cloudAccount?.apply {
            if (portal.urlWithScheme.isNotEmpty()) {
                viewBinding?.storageWebDavServerEdit?.setText(portal.urlWithScheme)
            }
            if (login.isNotEmpty()) {
                viewBinding?.storageWebDavLoginEdit?.setText(login)
            }
        }
    }

    protected fun connect() {
        hideKeyboard()
        viewBinding?.let { binding ->
            val url = binding.storageWebDavServerEdit.text?.trim().toString()
            val login = binding.storageWebDavLoginEdit.text?.trim().toString()
            val password = binding.storageWebDavPasswordEdit.text?.trim().toString()

            webDavProvider?.let { provider ->
                presenter.checkPortal(provider, url, login, password)
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
        MainActivity.show(requireContext())
        requireActivity().finish()
    }

    override fun onNextCloudLogin(url: String) {
        WebViewCloudLoginActivity.show(
            activity = requireActivity(),
            portal = url,
            provider = WebdavProvider.NextCloud.name
        )
    }

    override fun onUrlError() {
        onServerError()
    }

    override fun onDialogWaiting() {
        showWaitingDialog(getString(R.string.dialogs_wait_title))
    }

    override fun onDialogClose() {
        hideDialog()
    }

    abstract fun initFields()
}