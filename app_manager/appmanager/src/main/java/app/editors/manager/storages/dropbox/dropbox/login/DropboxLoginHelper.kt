package app.editors.manager.storages.dropbox.dropbox.login

import android.accounts.Account
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.documents.core.settings.NetworkSettings
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.storages.dropbox.dropbox.api.DropboxService
import app.editors.manager.storages.dropbox.managers.utils.DropboxUtils
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.FullAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import javax.inject.Inject

sealed class DropboxLoginState {
    object LoggingIn : DropboxLoginState()
    object None : DropboxLoginState()
}

class DropboxLoginHelper(private val context: Context) {

    @Inject
    lateinit var networkSettings: NetworkSettings

    @Inject
    lateinit var accountDao: AccountDao

    init {
        App.getApp().appComponent.inject(this)
    }

    private val requestConfig = DbxRequestConfig("OnlyOffice/${BuildConfig.VERSION_NAME}")
    private val scopes = listOf("account_info.read", "files.content.write", "files.content.read")
    private var loginCallback: (() -> Unit)? = null
    private var loginState: DropboxLoginState = DropboxLoginState.None

    fun startSignInActivity(lifecycleOwner: LifecycleOwner, loginCallback: () -> Unit) {
        loginState = DropboxLoginState.LoggingIn
        this.loginCallback = loginCallback
        Auth.startOAuth2PKCE(context, BuildConfig.DROP_BOX_COM_CLIENT_ID, requestConfig, scopes)
        lifecycleOwner.lifecycle.addObserver(getLifecycleObserver(lifecycleOwner))
    }

    private fun getLifecycleObserver(lifecycleOwner: LifecycleOwner): LifecycleObserver {
        return object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                val observer = this
                owner.lifecycleScope.launch {
                    owner.whenResumed {
                        checkAccount()
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
            }
        }
    }

    private suspend fun checkAccount() = withContext(Dispatchers.IO) {
        if (loginState is DropboxLoginState.LoggingIn) {
            try {
                Auth.getDbxCredential()?.let { credential ->
                    val user = DbxClientV2(requestConfig, credential).users().currentAccount
                    createUser(user, credential.accessToken, credential.refreshToken)
                }
            } catch (exception: InvalidAccessTokenException) {
                Log.e(this::class.simpleName, exception.message ?: exception.toString())
            } finally {
                loginState = DropboxLoginState.None
            }
        }
    }

    private suspend fun createUser(user: FullAccount, accessToken: String, refreshToken: String) {
        networkSettings.setBaseUrl(DropboxService.DROPBOX_BASE_URL)
        val cloudAccount = CloudAccount(
            id = user.accountId,
            isWebDav = false,
            isOneDrive = false,
            isDropbox = true,
            portal = DropboxUtils.DROPBOX_PORTAL,
            webDavPath = "",
            webDavProvider = "",
            login = user.email,
            scheme = "https://",
            isSslState = networkSettings.getSslState(),
            isSslCiphers = networkSettings.getCipher(),
            name = user.name?.displayName,
            avatarUrl = user.profilePhotoUrl,
            refreshToken = refreshToken
        )

        val accountData = AccountData(
            portal = cloudAccount.portal ?: "",
            scheme = cloudAccount.scheme ?: "",
            displayName = user.name?.displayName!!,
            userId = cloudAccount.id,
            provider = cloudAccount.webDavProvider ?: "",
            accessToken = accessToken,
            webDav = cloudAccount.webDavPath,
            email = user.email,
        )

        saveAccount(cloudAccount = cloudAccount, accountData = accountData, accessToken = accessToken)
    }

    private suspend fun saveAccount(cloudAccount: CloudAccount, accountData: AccountData, accessToken: String) {
        val account = Account(cloudAccount.getAccountName(), context.getString(R.string.account_type))

        if (AccountUtils.addAccount(context, account, "", accountData)) {
            addAccountToDb(cloudAccount)
        } else {
            AccountUtils.setAccountData(context, account, accountData)
            AccountUtils.setPassword(context, account, accessToken)
            addAccountToDb(cloudAccount)
        }
        AccountUtils.setToken(context, account, accessToken)
    }

    private suspend fun addAccountToDb(cloudAccount: CloudAccount) {
        accountDao.getAccountOnline()?.let {
            accountDao.addAccount(it.copy(isOnline = false))
        }
        accountDao.addAccount(cloudAccount.copy(isOnline = true))
        withContext(Dispatchers.Main) {
            loginCallback?.invoke()
        }
    }
}