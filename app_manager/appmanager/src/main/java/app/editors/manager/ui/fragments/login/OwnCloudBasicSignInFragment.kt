package app.editors.manager.ui.fragments.login

import androidx.core.view.isVisible
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.model.login.OidcConfiguration
import app.editors.manager.ui.activities.login.WebDavLoginActivity
import lib.toolkit.base.managers.utils.putArgs

class OwnCloudBasicSignInFragment : WebDavBaseSignInFragment() {

    companion object {
        private const val KEY_URL = "KEY_URL"

        fun newInstance(url: String, account: String?): OwnCloudBasicSignInFragment {
            return OwnCloudBasicSignInFragment().putArgs(
                KEY_URL to url,
                KEY_PROVIDER to WebdavProvider.OwnCloud,
                WebDavLoginActivity.KEY_ACCOUNT to account
            )
        }
    }

    override val showOnlyServer: Boolean = false

    override fun initFields() {
        viewBinding?.storageWebDavServerEdit?.setText(arguments?.getString(KEY_URL).orEmpty())
        viewBinding?.storageWebDavServerLayout?.isVisible = false
        viewBinding?.storageWebDavPasswordEdit?.setActionDoneListener(this::connect)
    }

    override fun onOwnCloudLogin(url: String, config: OidcConfiguration?) {}
}