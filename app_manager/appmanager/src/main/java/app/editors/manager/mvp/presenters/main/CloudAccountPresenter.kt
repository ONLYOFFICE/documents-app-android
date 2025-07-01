package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.os.Bundle
import app.documents.core.login.CheckLoginResult
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.network.common.Result
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.login.BaseLoginPresenter
import app.editors.manager.mvp.views.main.CloudAccountView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.ActivitiesUtils
import moxy.InjectViewState
import moxy.presenterScope
import retrofit2.HttpException

sealed class CloudAccountState {
    class AccountLoadedState(val account: List<CloudAccount>, val state: Bundle? = null) : CloudAccountState()
}

@InjectViewState
class CloudAccountPresenter : BaseLoginPresenter<CloudAccountView>() {

    companion object {
        val TAG: String = CloudAccountPresenter::class.java.simpleName

    }

    var contextAccount: CloudAccount? = null

    init {
        App.getApp().appComponent.inject(this)
        App.getApp().refreshLoginComponent(null)
    }

    fun getAccounts(saveState: Bundle? = null) {
        presenterScope.launch {
            val accounts = cloudDataSource.getAccounts()
            withContext(Dispatchers.Main) {
                viewState.onRender(CloudAccountState.AccountLoadedState(accounts, saveState))
                if (preferenceTool.fileData.isNotEmpty()) {
                    switchAccount()
                }
            }
        }
    }

    private suspend fun switchAccount() {
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
//            preferenceTool.fileData = ""
            withContext(Dispatchers.Main) {
                viewState.onAccountLogin(data.portal ?: "", null)
            }
        }
    }

    fun logOut(accountId: String) {
        viewState.onWaiting()
        signInJob = presenterScope.launch {
            loginRepository.logOut(accountId)
                .collect { result ->
                    viewState.onHideDialog()
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> {
                            App.getApp().refreshAppComponent(context)
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

    fun deleteSelected(selection: List<String>?) {
        viewState.onWaiting()
        signInJob = presenterScope.launch {
            loginRepository.deleteAccounts(*selection.orEmpty().toTypedArray())
                .collect { result ->
                    viewState.onHideDialog()
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> {
                            withContext(Dispatchers.Main) {
                                selection?.forEach { id -> deleteFromAccountProvider(id) }
                                viewState.onRender(CloudAccountState.AccountLoadedState(result.result))
                            }
                        }
                    }
                }
        }
    }

    fun deleteAccount(accountId: String) {
        viewState.onWaiting()
        signInJob = presenterScope.launch {
            loginRepository.deleteAccounts(accountId)
                .collect { result ->
                    viewState.onHideDialog()
                    when (result) {
                        is Result.Error -> fetchError(result.exception)
                        is Result.Success -> {
                            withContext(Dispatchers.Main) {
                                deleteFromAccountProvider(accountId)
                                viewState.onRender(CloudAccountState.AccountLoadedState(result.result))
                            }
                        }
                    }
                }
        }
    }

    fun checkLogin(accountId: String) {
        viewState.onWaiting()
        signInJob = presenterScope.launch {
            val account = checkNotNull(cloudDataSource.getAccount(accountId))
            App.getApp().refreshLoginComponent(account.portal)
            loginRepository.checkLogin(accountId).collect { onCheckLoginCollect(it, account) }
        }
    }

    fun checkLogin(account: CloudAccount) {
        viewState.onWaiting()
        signInJob = presenterScope.launch {
            App.getApp().refreshLoginComponent(account.portal)
            loginRepository.checkLogin(account.id).collect { onCheckLoginCollect(it, account) }
        }
    }

    private fun onCheckLoginCollect(result: CheckLoginResult, account: CloudAccount) {
        viewState.onHideDialog()
        when (result) {
            is CheckLoginResult.Success -> {
                App.getApp().showPersonalPortalMigration = true
                App.getApp().refreshAppComponent(context)
                viewState.onSuccessLogin()
            }

            is CheckLoginResult.Error -> checkError(result.exception, account)
            is CheckLoginResult.NeedLogin -> showLoginFragment(account)
            CheckLoginResult.AlreadyUse -> onAlreadyUse()
        }
    }

    private fun onAlreadyUse() {
        viewState.onError(context.getString(R.string.errors_sign_in_account_already_use))
    }

    private fun showLoginFragment(account: CloudAccount) {
        when (val provider = account.portal.provider) {
            PortalProvider.Dropbox -> viewState.onDropboxLogin()
            PortalProvider.GoogleDrive -> viewState.onGoogleDriveLogin()
            PortalProvider.Onedrive -> viewState.onOneDriveLogin()
            is PortalProvider.Webdav -> viewState.onWebDavLogin(
                Json.encodeToString(account),
                provider.provider
            )

            else -> viewState.onAccountLogin(account.portal.url, account.login)
        }
    }

    private fun deleteFromAccountProvider(accountId: String) {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents" && ActivitiesUtils.isPackageExist(
                App.getApp(),
                "com.onlyoffice.projects"
            )
        ) {
            try {
                context.contentResolver.delete(
                    Uri.parse("content://com.onlyoffice.projects.accounts/accounts/$accountId"),
                    null,
                    null
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
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