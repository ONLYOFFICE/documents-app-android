package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.content.Context
import android.os.Handler
import android.os.Looper
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.documents.core.account.RecentDao
import app.documents.core.login.LoginResponse
import app.documents.core.network.models.login.response.ResponseUser
import app.documents.core.settings.NetworkSettings
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.app.loginService
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.mvp.models.user.Thirdparty
import app.editors.manager.mvp.views.main.ProfileView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import moxy.MvpPresenter
import java.lang.RuntimeException
import javax.inject.Inject

sealed class ProfileState(val account: CloudAccount) {
    class WebDavState(cloudAccount: CloudAccount) : ProfileState(cloudAccount)
    class CloudState(cloudAccount: CloudAccount) : ProfileState(cloudAccount)
    class ProvidersState(val providers: List<Thirdparty>, cloudAccount: CloudAccount) : ProfileState(cloudAccount)
}

@InjectViewState
class ProfilePresenter : MvpPresenter<ProfileView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var accountDao: AccountDao

    @Inject
    lateinit var recentDao: RecentDao

    @Inject
    lateinit var networkSettings: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    private lateinit var account: CloudAccount
    private var disposable = CompositeDisposable()
    private var loginService = context.loginService


    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun setAccount(jsonAccount: String) {
        account = Json.decodeFromString(jsonAccount)

        updateBaseUrl(account)

        if (account.isWebDav) {
            viewState.onRender(ProfileState.WebDavState(account))
        } else if (account.isDropbox || account.isOneDrive) {
            viewState.onRender(ProfileState.CloudState(account))
        } else {
            viewState.onRender(ProfileState.CloudState(account))
            //TODO will rework by next release
            //getThirdparty(account)
            updateAccountInfo(account)
        }
    }

    private fun updateBaseUrl(account: CloudAccount) {
        networkSettings.setBaseUrl(account.portal ?: "")
        loginService = context.loginService
    }

    private fun getThirdparty(account: CloudAccount) {
        try {
            disposable.add(context.api()
                .thirdPartyList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.response }
                .subscribe({ list: List<Thirdparty> ->
                    viewState.onRender(ProfileState.ProvidersState(list, account))
                }) { throwable: Throwable -> viewState.onError(throwable.message) })
        } catch (error: RuntimeException) {
            //nothing
        }

    }

    private fun updateAccountInfo(account: CloudAccount) {
        CoroutineScope(Dispatchers.Default).launch {
            val token = AccountUtils.getToken(context, Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type)))

            when (val response = token?.let { loginService.getUserInfo(it).blockingGet() }) {
                is LoginResponse.Success -> {
                    val user = (response.response as ResponseUser).response
                    withContext(Dispatchers.Main) {
                        AccountUtils.getAccount(context, account.getAccountName())?.let { systemAccount ->
                            if (systemAccount.name != "${user.email}@${account.portal}") {
                                try {
                                    AccountUtils.getAccountManager(context).renameAccount(systemAccount, "${user.email}@${account.portal}", {
                                    }, Handler(Looper.getMainLooper()))
                                } catch (exception: NullPointerException) {
                                    exception.message?.let { FirebaseUtils.addCrash(it) }
                                }

                            }
                        }
                    }
                    accountDao.updateAccount(
                        account.copy(
                            avatarUrl = user.avatarMedium,
                            name = user.getName(),
                            isAdmin = user.isAdmin,
                            isVisitor = user.isVisitor
                        )
                    )
                }
                is LoginResponse.Error -> {
                    viewState.onError(response.error.message)
                }
            }
        }
    }

    fun removeAccount() {
        CoroutineScope(Dispatchers.Default).launch {
            AccountUtils.removeAccount(
                context,
                Account(account.getAccountName(), context.getString(lib.toolkit.base.R.string.account_type))
            )
            accountDao.deleteAccount(account)
            recentDao.removeAllByOwnerId(account.id)
            withContext(Dispatchers.Main) {
                viewState.onClose(false)
            }
        }


    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            AccountUtils.getAccount(context, account.getAccountName())?.let {
                if (account.isWebDav) {
                    AccountUtils.setPassword(context, it, null)
                } else if (account.isDropbox || account.isOneDrive) {
                    AccountUtils.setToken(context, it, "")
                } else {
                    AccountUtils.setToken(context, it, null)
                }
            }
            accountDao.updateAccount(account.copy(isOnline = false))
            withContext(Dispatchers.Main) {
                viewState.onClose(true, account.copy(isOnline = false))
            }
        }
    }

}