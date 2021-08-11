package app.editors.manager.mvp.presenters.login

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.documents.core.login.LoginResponse
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.webDavApi
import app.editors.manager.mvp.views.login.AccountsView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import okhttp3.Credentials
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

@InjectViewState
class AccountsPresenter : BaseLoginPresenter<AccountsView>() {

    companion object {
        val TAG: String = AccountsPresenter::class.java.simpleName
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private lateinit var clickedAccount: CloudAccount
    private var accountClickedPosition = 0

    private var disposable: Disposable? = null

    val accounts: Unit
        get() {
            CoroutineScope(Dispatchers.Default).launch {
                viewState.onUsersAccounts(accountDao.getAccounts().toMutableList())
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun setAccountClicked(account: CloudAccount, position: Int) {
        clickedAccount = account
        accountClickedPosition = position
    }

    fun deleteAccount() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.deleteAccount(clickedAccount)
            AccountUtils.removeAccount(
                context,
                Account(clickedAccount.getAccountName(), context.getString(R.string.account_type))
            )
            withContext(Dispatchers.Main) {
                viewState.onAccountDelete(accountClickedPosition)
            }
        }
    }

    fun loginAccount() {
        if (clickedAccount.isOnline) {
            viewState.onError(context.getString(R.string.errors_sign_in_account_already_use))
            return
        }
        if (clickedAccount.isWebDav) {
            loginWebDav()
            return
        }

        login()
    }

    private fun loginWebDav() {
        AccountUtils.getPassword(
            context,
            Account(clickedAccount.getAccountName(), context.getString(R.string.account_type))
        )?.let { password ->
            val credential =
                Credentials.basic(clickedAccount.login ?: "", password)
            setNetworkSettings()
            disposable = context.webDavApi()
                .capabilities(credential, clickedAccount.webDavPath)
                .doOnSubscribe { viewState.showWaitingDialog() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { responseBody: Response<ResponseBody> ->
                    if (responseBody.isSuccessful && responseBody.code() == 207) {
                        return@map responseBody.body()
                    } else {
                        throw HttpException(responseBody)
                    }
                }
                .subscribe({ setAccount() }) { throwable: Throwable ->
                    setOnlineSettings()
                    fetchError(throwable)
                }
        } ?: run {
            viewState.onWebDavLogin(clickedAccount)
        }
    }

    private fun login() {
        val token = AccountUtils.getToken(
            context,
            Account(clickedAccount.getAccountName(), context.getString(R.string.account_type))
        )
        val portal = clickedAccount.portal
        if (token != null && token.isNotEmpty()) {
            setNetworkSettings()
            disposable =
                App.getApp().appComponent.loginService.getUserInfo(token)
                    .doOnSubscribe { viewState.showWaitingDialog() }
                    .subscribe({ response ->
                        when (response) {
                            is LoginResponse.Success -> {
                                setAccount()
                            }
                            is LoginResponse.Error -> {
                                setOnlineSettings()
                                viewState.onSignIn(clickedAccount.portal ?: "", clickedAccount.login ?: "")
                            }
                        }
                    }, { fetchError(it) })
        } else if (token != null && token.isEmpty()) {
            viewState.onSignIn(portal ?: "", clickedAccount.login ?: "")
        } else {
            viewState.onError(context.getString(R.string.errors_sign_in_account_error))
        }
    }

    private fun setAccount() {
        CoroutineScope(Dispatchers.Default).launch {
            val account = accountDao.getAccountOnline()?.let { accountDao.getAccount(it.id)?.copy(isOnline = false) }
            account?.let { accountDao.updateAccount(it) }
            accountDao.updateAccount(clickedAccount.copy(isOnline = true))

            withContext(Dispatchers.Main) {
                viewState.onAccountLogin()
            }
        }
    }

    private fun setNetworkSettings() {
        networkSettings.setSettingsByAccount(clickedAccount)
    }

    private fun setOnlineSettings() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                networkSettings.setSettingsByAccount(it)
            }
        }
    }
}