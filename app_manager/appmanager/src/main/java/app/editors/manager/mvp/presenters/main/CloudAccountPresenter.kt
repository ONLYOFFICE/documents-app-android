package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import app.editors.manager.BuildConfig
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.Capabilities
import app.documents.core.network.login.models.response.ResponseAllSettings
import app.documents.core.network.login.models.response.ResponseCapabilities
import app.documents.core.network.login.models.response.ResponseSettings
import app.documents.core.network.login.models.response.ResponseUser
import app.documents.core.network.storages.dropbox.login.DropboxLoginHelper
import app.documents.core.network.webdav.WebDavService
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.account.copyWithToken
import app.documents.core.storage.preference.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.loginService
import app.editors.manager.app.webDavApi
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.login.BaseLoginPresenter
import app.editors.manager.mvp.views.main.CloudAccountView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ActivitiesUtils
import moxy.InjectViewState
import moxy.presenterScope
import okhttp3.Credentials
import retrofit2.HttpException
import javax.inject.Inject

sealed class CloudAccountState {
    class AccountLoadedState(val account: List<CloudAccount>, val state: Bundle?) : CloudAccountState()
}

data class RestoreSettingsModel(
    val portal: String,
    val scheme: String,
    val isSsl: Boolean,
    val isCipher: Boolean
) {
    companion object {
        fun getInstance(networkSettings: NetworkSettings): RestoreSettingsModel {
            return RestoreSettingsModel(
                networkSettings.getPortal(),
                networkSettings.getScheme(),
                networkSettings.getSslState(),
                networkSettings.getCipher()
            )
        }
    }
}

@InjectViewState
class CloudAccountPresenter : BaseLoginPresenter<CloudAccountView>() {

    companion object {
        val TAG: String = CloudAccountPresenter::class.java.simpleName

        const val KEY_SWITCH = "switch"
    }

    @Inject
    lateinit var dropboxLoginHelper: DropboxLoginHelper

    init {
        App.getApp().appComponent.inject(this)
    }

    var contextAccount: CloudAccount? = null
    private var restoreState: RestoreSettingsModel? = null
    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getAccounts(saveState: Bundle? = null, isSwitch: Boolean = false) {
        presenterScope.launch {
            if (isSwitch) {
                switchAccount(saveState)
            } else {
                getTokens(accountDao.getAccounts(), saveState)
            }
        }
    }

    private suspend fun switchAccount(saveState: Bundle? = null) {
        val data = Json.decodeFromString<OpenDataModel>(preferenceTool.fileData)
        accountDao.getAccountByLogin(data.email?.lowercase() ?: "")?.let { cloudAccount ->
            checkLogin(cloudAccount)
        } ?: run {
            getTokens(accountDao.getAccounts(), saveState)
        }
    }

    private suspend fun getTokens(accounts: List<CloudAccount>, saveState: Bundle?) {
        val accountsWithToken = accounts.map { account ->
            AccountUtils.getToken(
                context,
                Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
            )?.let { token ->
                account.token = token
                return@map account
            } ?: run {
                return@map account
            }
        }
        withContext(Dispatchers.Main) {
            viewState.onRender(CloudAccountState.AccountLoadedState(accountsWithToken, saveState))
        }
    }

    fun logOut() {
        presenterScope.launch {
            contextAccount?.let { account ->
                if (account.isWebDav) {
                    AccountUtils.setPassword(context, account.getAccountName(), null)
                } else if (account.isOneDrive || account.isDropbox || account.isGoogleDrive) {
                    AccountUtils.setToken(context, account.getAccountName(), "")
                } else {
                    AccountUtils.setPassword(context, account.getAccountName(), null)
                }
                accountDao.updateAccount(account.copyWithToken(isOnline = false).apply {
                    token = ""
                    password = ""
                    expires = ""
                })
            }
            accountDao.getAccounts().let {
                withContext(Dispatchers.Main) {
                    viewState.onRender(CloudAccountState.AccountLoadedState(it, null))
                }
            }
        }
    }

    fun deleteAccount() {
        contextAccount?.let { account ->
            if (account.isDropbox && account.isOneDrive && account.isGoogleDrive && account.isWebDav) {
                deleteAccount(account)
            } else {
                if (account.isOnline) {
                    unsubscribePush(account, AccountUtils.getToken(context, account.getAccountName())) {
                        deleteAccount(account)
                    }
                } else {
                    deleteAccount(account)
                }
            }
        }
    }

    fun deleteSelected(selection: List<String>?) {
        presenterScope.launch {
            selection?.forEach { id ->
                accountDao.getAccount(id)?.let { account ->
                    if (account.isOnline) {
                        unsubscribePush(account, AccountUtils.getToken(context, account.getAccountName())) {
                            deleteAccount(account)
                        }
                    } else {
                        deleteAccount(account)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                viewState.onRender(CloudAccountState.AccountLoadedState(accountDao.getAccounts(), null))
            }
        }
    }

    @Suppress("KotlinConstantConditions")
    private fun deleteAccount(account: CloudAccount) {
        presenterScope.launch {
            AccountUtils.removeAccount(context, account.getAccountName())
            accountDao.deleteAccount(account)
            accountDao.getAccounts().let {
                withContext(Dispatchers.Main) {
                    if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents" && ActivitiesUtils.isPackageExist(App.getApp(), "com.onlyoffice.projects")) {
                        context.contentResolver.delete(
                            Uri.parse("content://com.onlyoffice.projects.accounts/accounts/${account.id}"),
                            null,
                            null
                        )
                    }
                    viewState.onRender(CloudAccountState.AccountLoadedState(it, null))
                }
            }
        }
    }

    fun checkLogin(account: CloudAccount) {
        if (!account.isOnline) {
            viewState.onWaiting()
            when {
                account.isWebDav -> {
                    AccountUtils.getPassword(context, account.getAccountName())?.let { password ->
                        if (password.isNotEmpty()) {
                            webDavLogin(account, password)
                        } else {
                            viewState.onWebDavLogin(
                                Json.encodeToString(account),
                                WebDavService.Providers.valueOf(account.webDavProvider ?: "")
                            )
                        }
                    } ?: run {
                        viewState.onWebDavLogin(
                            Json.encodeToString(account),
                            WebDavService.Providers.valueOf(account.webDavProvider ?: "")
                        )
                    }
                }

                account.isOneDrive -> {
                    AccountUtils.getToken(context, account.getAccountName())?.let { token ->
                        if (token.isNotEmpty()) {
                            loginSuccess(account)
                        } else {
                            viewState.onOneDriveLogin()
                        }
                    }
                }

                account.isDropbox -> {
                    AccountUtils.getToken(context, account.getAccountName())?.let { token ->
                        if (token.isNotEmpty()) {
                            loginSuccess(account)
                        } else {
                            viewState.onDropboxLogin()
                        }
                    }
                }

                account.isGoogleDrive -> {
                    AccountUtils.getToken(context, account.getAccountName())?.let { token ->
                        if (token.isNotEmpty()) {
                            loginSuccess(account)
                        } else {
                            viewState.onGoogleDriveLogin()
                        }
                    }
                }

                else -> {
                    AccountUtils.getToken(context, account.getAccountName())?.let { token ->
                        if (token.isNotEmpty()) {
                            login(account, token)
                        } else {
                            viewState.onAccountLogin(account.portal ?: "", account.login ?: "")
                        }
                    } ?: run {
                        setSettings(account)
                        disposable = context.loginService.capabilities().subscribe({ response ->
                            if (response is LoginResponse.Success) {
                                when (response.response) {
                                    is ResponseCapabilities -> {
                                        val capability = (response.response as ResponseCapabilities).response
                                        setSettings(capability)
                                        viewState.onAccountLogin(account.portal ?: "", account.login ?: "")
                                    }

                                    is ResponseSettings -> {
                                        networkSettings.serverVersion =
                                            (response.response as ResponseSettings).response.communityServer ?: ""
                                    }

                                    is ResponseAllSettings -> {
                                        networkSettings.isDocSpace =
                                            (response.response as ResponseAllSettings).response.docSpace
                                    }
                                }
                            } else {
                                fetchError((response as LoginResponse.Error).error)
                            }
                        }) { throwable: Throwable -> checkError(throwable, account) }
                    }
                }
            }
        } else {
            viewState.onError(context.getString(R.string.errors_sign_in_account_already_use))
        }
    }

    fun checkContextLogin() {
        contextAccount?.let {
            checkLogin(it)
        } ?: run {
            viewState.onError("Error login")
        }
    }

    fun dropboxLogin(lifecycleOwner: LifecycleOwner, onLoginCallback: () -> Unit) {
        dropboxLoginHelper.startSignInActivity(lifecycleOwner, onLoginCallback)
    }

    private fun login(account: CloudAccount, token: String) {
        setSettings(account)
        val loginService = context.loginService
        disposable = loginService.capabilities().flatMap { capabilityResponse ->
            checkSettings(capabilityResponse)
            loginService.getUserInfo(token).toObservable()
        }.map {
            when (it) {
                is LoginResponse.Success -> return@map it.response as ResponseUser
                is LoginResponse.Error -> throw throw it.error
            }
        }
            .flatMap { loginService.subscribe(token, preferenceTool.deviceMessageToken, true).toObservable() }
            .toList()
            .subscribe({
                loginSuccess(account)
            }, {
                checkError(it, account)
            })
    }

    private fun checkSettings(capabilityResponse: LoginResponse) {
        when (capabilityResponse) {
            is LoginResponse.Success -> {
                when (capabilityResponse.response) {
                    is ResponseSettings -> {
                        networkSettings.serverVersion =
                            (capabilityResponse.response as ResponseSettings).response.communityServer ?: ""
                    }

                    is ResponseAllSettings -> {
                        networkSettings.isDocSpace =
                            (capabilityResponse.response as ResponseAllSettings).response.docSpace
                    }
                }
            }

            is LoginResponse.Error -> {
                networkSettings.isDocSpace = false
            }
        }
    }

    private fun webDavLogin(account: CloudAccount, password: String) {
        setSettings(account)
        disposable = context.webDavApi
            .capabilities(Credentials.basic(account.login ?: "", password), account.webDavPath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code() == 207 && it.body() != null) {
                    loginSuccess(account)
                } else {
                    restoreSettings()
                    viewState.onWebDavLogin(
                        Json.encodeToString(account),
                        WebDavService.Providers.valueOf(account.webDavProvider ?: "")
                    )
                }
            }, {
                checkError(it, account)
            })
    }

    private fun loginSuccess(account: CloudAccount) {
        context.accountOnline?.let { onlineAccount ->
            setSettings(onlineAccount)
            unsubscribePush(onlineAccount, AccountUtils.getToken(context, onlineAccount.getAccountName())) {
                presenterScope.launch {
                    setSettings(account)
                    accountDao.updateAccount(onlineAccount.copyWithToken(isOnline = false))
                    accountDao.updateAccount(account.copyWithToken(isOnline = true))
                    withContext(Dispatchers.Main) {
                        viewState.onSuccessLogin()
                    }
                }
            }
        } ?: run {
            presenterScope.launch {
                setSettings(account)
                accountDao.updateAccount(account.copyWithToken(isOnline = true))
                withContext(Dispatchers.Main) {
                    viewState.onSuccessLogin()
                }
            }
        }
    }

    private fun setSettings(account: CloudAccount) {
        restoreState = RestoreSettingsModel.getInstance(networkSettings)
        networkSettings.setBaseUrl(account.portal ?: "")
        networkSettings.setScheme(account.scheme ?: "")
        networkSettings.setSslState(account.isSslState)
        networkSettings.setCipher(account.isSslCiphers)
    }

    private fun setSettings(capabilities: Capabilities) {
        networkSettings.ldap = capabilities.ldapEnabled
        networkSettings.ssoUrl = capabilities.ssoUrl
        networkSettings.ssoLabel = capabilities.ssoLabel
    }

    private fun restoreSettings() {
        restoreState?.let { state ->
            networkSettings.setBaseUrl(state.portal)
            networkSettings.setScheme(state.scheme)
            networkSettings.setSslState(state.isSsl)
            networkSettings.setCipher(state.isCipher)
        }
        restoreState = null
    }

    private fun checkError(throwable: Throwable, account: CloudAccount) {
        restoreSettings()
        if (throwable is HttpException && throwable.code() == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED) {
            viewState.onAccountLogin(account.portal ?: "", account.login ?: "")
        } else {
            fetchError(throwable)
        }
    }

}