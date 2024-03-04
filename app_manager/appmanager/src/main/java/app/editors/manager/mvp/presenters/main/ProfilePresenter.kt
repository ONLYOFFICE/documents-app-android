package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import app.documents.core.database.dao.RecentDao
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.network.login.ILoginServiceProvider
import app.documents.core.network.login.LoginResponse
import app.documents.core.network.login.models.response.ResponseUser
import app.documents.core.network.manager.models.user.Thirdparty
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.app.loginService
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.GoogleUtils
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.ProfileView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import javax.inject.Inject

sealed class ProfileState(val account: CloudAccount) {
    class WebDavState(cloudAccount: CloudAccount) : ProfileState(cloudAccount)
    class CloudState(cloudAccount: CloudAccount) : ProfileState(cloudAccount)
    class ProvidersState(val providers: List<Thirdparty>, cloudAccount: CloudAccount) : ProfileState(cloudAccount)
}

@InjectViewState
class ProfilePresenter : BasePresenter<ProfileView>() {

    @Inject
    lateinit var recentDao: RecentDao

    init {
        App.getApp().appComponent.inject(this)
    }

    private lateinit var account: CloudAccount
    private var disposable = CompositeDisposable()
    private var loginService: ILoginServiceProvider? = null


    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun setAccount(jsonAccount: String) {
        account = Json.decodeFromString(jsonAccount)

        updateBaseUrl(account)

        if (account.isWebDav) {
            viewState.onRender(ProfileState.WebDavState(account))
        } else if (account.isDropbox || account.isOneDrive || account.isGoogleDrive) {
            viewState.onRender(ProfileState.CloudState(account))
        } else {
            viewState.onRender(ProfileState.CloudState(account))
            //TODO will rework by next release
            //getThirdparty(account)
            updateAccountInfo(account)
        }
    }

    private fun updateBaseUrl(account: CloudAccount) {
        networkSettings.setBaseUrl(account.portal.portal ?: "")
        loginService = context.loginService
    }

    private fun getThirdparty(account: CloudAccount) {
        try {
            disposable.add(context.api
                .thirdPartyList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.response }
                .subscribe({ list: List<Thirdparty> ->
                    viewState.onRender(ProfileState.ProvidersState(list, account))
                }) { throwable: Throwable -> fetchError(throwable = throwable) })
        } catch (error: RuntimeException) {
            //nothing
        }

    }

    private fun updateAccountInfo(account: CloudAccount) {
        CoroutineScope(Dispatchers.Default).launch {
            val token = AccountUtils.getToken(
                context,
                Account(account.accountName, context.getString(lib.toolkit.base.R.string.account_type))
            )

            when (val response = token?.let { loginService?.getUserInfo(it)?.blockingGet() }) {
                is LoginResponse.Success -> {
                    val user = (response.response as ResponseUser).response
                    withContext(Dispatchers.Main) {
                        AccountUtils.getAccount(context, account.accountName)?.let { systemAccount ->
                            if (systemAccount.name != "${user.email}@${account.portal}") {
                                try {
                                    AccountUtils.getAccountManager(context)
                                        .renameAccount(systemAccount, "${user.email}@${account.portal}", {
                                        }, Handler(Looper.getMainLooper()))
                                } catch (exception: NullPointerException) {
                                    exception.message?.let { FirebaseUtils.addCrash(it) }
                                }

                            }
                        }
                    }
                    cloudDataSource.updateAccount(
                        account.copy(
                            avatarUrl = user.avatarMedium,
                            name = user.getName(),
                            isAdmin = user.isAdmin,
                            isVisitor = user.isVisitor
                        )
                    )
                }
                is LoginResponse.Error -> {
                    fetchError(response.error)
                }
                else -> {
                    // Nothing
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun logout() {
        AccountUtils.getAccount(context, account.accountName)?.let { systemAccount ->
            if (account.isWebDav) {
                AccountUtils.setPassword(context, systemAccount, null)
                update(systemAccount)
            } else if (account.isDropbox || account.isOneDrive || account.isGoogleDrive) {
                AccountUtils.setToken(context, systemAccount, "")
                update(systemAccount)
            } else {
                GoogleUtils.getDeviceToken({ deviceToken ->
                    context.loginService.subscribe(
                        AccountUtils.getToken(context, account.accountName) ?: "",
                        deviceToken,
                        false
                    ).subscribe({
                        update(systemAccount)
                    }) {
                        update(systemAccount)
                    }
                }) {
                    update(systemAccount)
                }
            }
        }
    }

    private fun update(systemAccount: Account) {
        CoroutineScope(Dispatchers.Default).launch {
            AccountUtils.setToken(context, systemAccount, null)
            cloudDataSource.updateAccount(account.copy(isOnline = false))
            withContext(Dispatchers.Main) {
                viewState.onClose(true, account.copy(isOnline = false))
            }
        }
    }
}