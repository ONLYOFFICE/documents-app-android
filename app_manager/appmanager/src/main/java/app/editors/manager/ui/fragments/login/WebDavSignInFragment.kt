package app.editors.manager.ui.fragments.login

import android.annotation.SuppressLint
import androidx.core.view.isVisible
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.login.OidcConfiguration
import app.documents.core.network.common.utils.OwnCloudOcisUtils
import app.editors.manager.R
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import app.editors.manager.ui.activities.login.WebViewCloudLoginActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.putArgs

class WebDavSignInFragment : WebDavBaseSignInFragment() {
    companion object {
        val TAG: String = WebDavSignInFragment::class.java.simpleName

        fun newInstance(provider: WebdavProvider?, account: String?): WebDavSignInFragment {
            return WebDavSignInFragment().putArgs(
                KEY_PROVIDER to provider,
                WebDavLoginActivity.KEY_ACCOUNT to account
            )
        }
    }

    override val showOnlyServer: Boolean
        get() = webDavProvider is WebdavProvider.NextCloud || webDavProvider is WebdavProvider.OwnCloud

    override fun initFields() {
        when (webDavProvider) {
            is WebdavProvider.NextCloud -> initOnlyServerState()
            is WebdavProvider.OwnCloud -> initOnlyServerState()
            WebdavProvider.Yandex -> initYandexState()
            WebdavProvider.KDrive -> initKDriveState()
            else -> {
                viewBinding?.storageWebDavPasswordEdit?.setActionDoneListener(this::connect)
            }
        }
    }

    private fun initOnlyServerState() {
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


    override fun onOwnCloudLogin(url: String, config: OidcConfiguration?) {
        if (config == null) {
            val account = try {
                Json.encodeToString(cloudAccount)
            } catch (_: Throwable) {
                null
            }
            onDialogClose()
            showFragment(
                fragment = OwnCloudBasicSignInFragment.newInstance(url, account),
                tag = OwnCloudBasicSignInFragment::class.java.simpleName,
                isAdd = false
            )
        } else {
            val storage = OwnCloudOcisUtils.storage
            val url = StorageUtils.getStorageUrl(
                providerKey = storage.name,
                clientId = storage.clientId,
                redirectUrl = config.issuer + storage.redirectUrl,
                authUrl = config.authorizationEndpoint + "?"
            ).orEmpty()

            WebViewCloudLoginActivity.show(
                activity = requireActivity(),
                portal = url,
                provider = WebdavProvider.OwnCloud.name,
                config = Json.encodeToString(config)
            )
        }
    }
}