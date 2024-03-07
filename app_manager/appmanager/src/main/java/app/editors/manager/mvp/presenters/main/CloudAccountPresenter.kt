package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.os.Bundle
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.WebdavProvider
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ActivitiesUtils
import moxy.InjectViewState
import moxy.presenterScope
import okhttp3.Credentials
import retrofit2.HttpException

sealed class CloudAccountState {
    class AccountLoadedState(val account: List<CloudAccount>, val state: Bundle? = null) : CloudAccountState()
}

@InjectViewState
class CloudAccountPresenter : BaseLoginPresenter<CloudAccountView>() {

    companion object {
        val TAG: String = CloudAccountPresenter::class.java.simpleName

        const val KEY_SWITCH = "switch"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    var contextAccount: CloudAccount? = null
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
                val accounts = cloudDataSource.getAccounts()
                withContext(Dispatchers.Main) {
                    viewState.onRender(CloudAccountState.AccountLoadedState(accounts, saveState))
                }
            }
        }
    }

    private suspend fun switchAccount(saveState: Bundle? = null) {
        val data = Json.decodeFromString<OpenDataModel>(preferenceTool.fileData)
        cloudDataSource.getAccounts()
            .find { account ->
                data.getPortalWithoutScheme()?.equals(account.portal.url, true) == true && data.email?.equals(
                    account.login,
                    true
                ) == true
            }?.let { cloudAccount ->
                checkLogin(cloudAccount)
            } ?: run {
            withContext(Dispatchers.Main) {
                viewState.onRender(CloudAccountState.AccountLoadedState(cloudDataSource.getAccounts(), saveState))
            }
        }
    }

    fun logOut() {
        presenterScope.launch {
            context.accountOnline?.portal?.let {
                App.getApp().refreshLoginComponent(it)
                loginRepository.logOut()
                    .collect { result ->
                        when (result) {
                            is Result.Error -> fetchError(result.exception)
                            is Result.Success -> {
                                withContext(Dispatchers.Main) {
                                    viewState.onRender(
                                        CloudAccountState.AccountLoadedState(cloudDataSource.getAccounts())
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }

    fun deleteAccount() {
        contextAccount?.let { deleteAccount(it) }
    }

    fun deleteSelected(selection: List<String>?) {
        presenterScope.launch {
            selection?.forEach { id ->
                cloudDataSource.getAccount(id)?.let { account ->
                    if (account.id == accountPreferences.onlineAccountId) {
                        loginRepository.unsubscribePush(account)
                        deleteAccount(account)
                    } else {
                        deleteAccount(account)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                viewState.onRender(CloudAccountState.AccountLoadedState(cloudDataSource.getAccounts()))
            }
        }
    }

    @Suppress("KotlinConstantConditions")
    private fun deleteAccount(account: CloudAccount) {
        App.getApp().refreshLoginComponent(account.portal)
        presenterScope.launch {
            loginRepository.deleteAccounts(account)
                .collect { result ->
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> {
                            withContext(Dispatchers.Main) {
                                if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents" && ActivitiesUtils.isPackageExist(
                                        App.getApp(),
                                        "com.onlyoffice.projects"
                                    )
                                ) {
                                    context.contentResolver.delete(
                                        Uri.parse("content://com.onlyoffice.projects.accounts/accounts/${account.id}"),
                                        null,
                                        null
                                    )
                                }
                                viewState.onRender(CloudAccountState.AccountLoadedState(result.result))
                            }
                        }
                    }
                }
        }
    }

    fun checkLogin(account: CloudAccount) {
        if (account.id == accountPreferences.onlineAccountId) {
            viewState.onError(context.getString(R.string.errors_sign_in_account_already_use))
            return
        }

        when (account.portal.provider) {
            is PortalProvider.Webdav -> checkWebDavLogin(account)
            PortalProvider.OneDrive,
            PortalProvider.DropBox,
            PortalProvider.GoogleDrive -> checkStorageLogin(account)
            else -> checkCloudLogin(account)
        }
    }

    private fun checkCloudLogin(account: CloudAccount) {
        val token = AccountUtils.getToken(context, account.accountName)
        if (token.isNullOrEmpty()) {
            App.getApp().refreshLoginComponent(account.portal)
            viewState.onAccountLogin(account.portal.url, account.login)
        } else {
            cloudSignIn(account, token)
        }
    }

    private fun checkStorageLogin(account: CloudAccount) {
        AccountUtils.getToken(context, account.accountName)?.let { token ->
            if (token.isNotEmpty()) {
                loginSuccess(account)
            } else {
                when {
                    account.isDropbox -> viewState.onDropboxLogin()
                    account.isGoogleDrive -> viewState.onGoogleDriveLogin()
                    account.isOneDrive -> viewState.onOneDriveLogin()
                }
            }
        }
    }

    private fun checkWebDavLogin(account: CloudAccount) {
        AccountUtils.getPassword(context, account.accountName)?.let { password ->
            if (password.isNotEmpty()) {
                webDavLogin(account, password)
            } else {
                viewState.onWebDavLogin(
                    Json.encodeToString(account),
                    WebdavProvider.valueOf(account.portal.provider)
                )
            }
        } ?: run {
            viewState.onWebDavLogin(
                Json.encodeToString(account),
                WebdavProvider.valueOf(account.portal.provider)
            )
        }
    }

    fun checkContextLogin() {
        contextAccount?.let {
            checkLogin(it)
        } ?: run {
            viewState.onError("Error login")
        }
    }

    private fun cloudSignIn(account: CloudAccount, token: String) {
        signInJob = presenterScope.launch {
            loginRepository.signInWithToken(token)
                .collect { result ->
                    when (result) {
                        is Result.Success -> loginSuccess(account)
                        is Result.Error -> checkError(result.exception, account)
                    }
                }
        }
    }

    private fun webDavLogin(account: CloudAccount, password: String) {
        val webdavProvider = (account.portal.provider as? PortalProvider.Webdav)
        disposable = context.webDavApi
            .capabilities(Credentials.basic(account.login, password), webdavProvider?.provider?.path)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code() == 207 && it.body() != null) {
                    loginSuccess(account)
                } else {
                    viewState.onWebDavLogin(
                        Json.encodeToString(account),
                        WebdavProvider.valueOf(account.portal.provider)
                    )
                }
            }, {
                checkError(it, account)
            })
    }

    private fun loginSuccess(account: CloudAccount) {
        App.getApp().refreshLoginComponent(account.portal)
        signInJob = presenterScope.launch {
            loginRepository.switchAccount(account)
                .collect { result ->
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        else -> Unit
                    }
                }
        }
    }

    private fun checkError(throwable: Throwable, account: CloudAccount) {
        if (throwable is HttpException && throwable.code() == ApiContract.HttpCodes.CLIENT_UNAUTHORIZED) {
            App.getApp().refreshLoginComponent(account.portal)
            viewState.onAccountLogin(account.portal.url, account.login)
        } else {
            fetchError(throwable)
        }
    }

    fun getOnlineAccountId(): String {
        return accountPreferences.onlineAccountId.orEmpty()
    }

}