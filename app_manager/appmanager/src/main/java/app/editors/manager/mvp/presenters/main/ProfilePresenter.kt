package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.content.Context
import app.documents.core.account.AccountDao
import app.documents.core.account.CloudAccount
import app.documents.core.account.RecentDao
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.di.component.DaggerApiComponent
import app.editors.manager.di.module.ApiModule
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

    private lateinit var account: CloudAccount
    private var disposable = CompositeDisposable()
    private var service: Api? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun setAccount(jsonAccount: String) {
        account = Json.decodeFromString(jsonAccount)
        if (account.isWebDav) {
            viewState.onRender(ProfileState.WebDavState(account))
        } else {
            viewState.onRender(ProfileState.CloudState(account))
            getService(account)
            getThirdparty(account)
            updateAccountInfo(account)
        }
    }

    private fun getService(account: CloudAccount) {
        AccountUtils.getToken(context, Account(account.name, context.getString(R.string.account_type)))?.let {
            service = DaggerApiComponent.builder().appComponent(App.getApp().appComponent)
                .apiModule(ApiModule(it))
                .build()
                .getApi()
        }

    }

    private fun getThirdparty(account: CloudAccount) {
        service?.let { api ->
            disposable.add(api
                .thirdPartyList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.response }
                .subscribe({ list: List<Thirdparty> ->
                    viewState.onRender(ProfileState.ProvidersState(list, account))
                }) { throwable: Throwable -> viewState.onError(throwable.message) })
        }
    }

    private fun updateAccountInfo(account: CloudAccount) {
        service?.let { api ->
            disposable.add(api.userInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.response }
                .subscribe({
                    CoroutineScope(Dispatchers.Default).launch {
                        accountDao.updateAccount(
                            account.copy(
                                avatarUrl = it.avatarMedium,
                                name = it.displayName,
                                isAdmin = it.isAdmin,
                                isVisitor = it.isVisitor
                            )
                        )
                    }
                }, {
                    // Nothing
                })
            )
        }
    }

    fun removeAccount() {
        CoroutineScope(Dispatchers.Default).launch {
            if (AccountUtils.removeAccount(
                    context,
                    Account(account.getAccountName(), context.getString(R.string.account_type))
                )
            ) {
                accountDao.deleteAccount(account)
                recentDao.removeAllByOwnerId(account.id)
                withContext(Dispatchers.Main) {
                    viewState.onClose(false)
                }
            } else {
                viewState.onError("Error delete account")
            }
        }

    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            AccountUtils.getAccount(context, account.getAccountName())?.let {
                if (account.isWebDav) {
                    AccountUtils.setPassword(context, it, null)
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