package app.editors.manager.mvp.presenters.main

import android.accounts.Account
import app.documents.core.account.CloudAccount
import app.documents.core.login.LoginResponse
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.exceptions.UrlSyntaxMistake
import app.editors.manager.mvp.models.account.AccountsSqlData
import app.editors.manager.mvp.models.response.ResponseUser
import app.editors.manager.mvp.models.user.User
import app.editors.manager.mvp.presenters.login.BaseLoginPresenter
import app.editors.manager.mvp.views.main.CloudAccountsView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import retrofit2.Response
import java.util.*

//sealed class CloudAccountState {
//    class AccountLoadedState(val account: List<CloudAccount>): CloudAccountState()
//    object DefaultState : CloudAccountState()
//    object SelectedState : CloudAccountState()
//}

@InjectViewState
class CloudAccountsPresenter : BaseLoginPresenter<CloudAccountsView>() {

    companion object {
        val TAG: String = CloudAccountsPresenter::class.java.simpleName
    }

    init {
//        App.getApp().appComponent.inject(this)
    }

    private val service = App.getApp().appComponent.loginService

    var contextAccount: CloudAccount? = null
    private var mClickedAccount: CloudAccount? = null
    private var mSelectedItems: MutableList<CloudAccount>? = null
    private var isSelectionMode = false
    private var mContextPosition = 0
    private var disposable: Disposable? = null

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getAccounts() {
        CoroutineScope(Dispatchers.Default).launch {
            getTokens(accountDao.getAccounts())
        }
    }

    private suspend fun getTokens(accounts: List<CloudAccount>) {
        val accountsWithToken = accounts.map { account ->
            AccountUtils.getToken(
                context,
                Account(account.getAccountName(), context.getString(R.string.account_type))
            )?.let { token ->
                account.token = token
                return@map account
            } ?: run {
                return@map account
            }
        }
        withContext(Dispatchers.Main) {
//            viewState.onRender(CloudAccountState.AccountLoadedState(accountsWithToken))
        }
    }

    private fun checkSelected(accounts: List<CloudAccount>) {
        if (mSelectedItems != null) {
            for (selectedAccount in mSelectedItems!!) {
                for (account in accounts) {
                    if (selectedAccount.id == account.id) {
//                        account.isSelection = true
                    }
                }
            }
            mSelectedItems!!.clear()
            for (account in accounts) {
//                if (account.isSelection) {
//                    mSelectedItems!!.add(account)
//                }
            }
        }
    }

    fun checkLogin(account: CloudAccount, position: Int) {
        mClickedAccount = account
        if (isSelectionMode) {
            addSelectionItem(account, position)
        } else {
            if (account.isOnline) {
                viewState.onShowClouds()
            } else if (account.isWebDav) {
                loginWebDav(account)
            } else {
                login(account)
            }
        }
    }

    private fun addSelectionItem(account: CloudAccount, position: Int) {
        if (mSelectedItems!!.contains(account)) {
//            account.isSelection = false
            mSelectedItems!!.remove(account)
        } else {
//            account.isSelection = true
//            mSelectedItems!!.add(account)
        }
        viewState.onActionBarTitle(mSelectedItems!!.size.toString())
        viewState.onSelectedItem(position)
    }

    fun contextClick(account: CloudAccount?, position: Int) {
        contextAccount = account
        mContextPosition = position
        viewState.onShowBottomDialog(account)
    }

    private fun loginWebDav(account: CloudAccount) {
//        if (account.password == null || account.password == "") {
////            viewState.onWebDavLogin(account)
//            return
//        }
//        val credential = Credentials.basic(account.login, account.password)
//        val provider = WebDavApi.Providers.valueOf(account.webDavProvider)
//        val path: String
//        path =
//            if (provider == WebDavApi.Providers.OwnCloud || provider == WebDavApi.Providers.NextCloud || provider == WebDavApi.Providers.WebDav) {
//                account.webDavPath
//            } else {
//                provider.path
//            }
//        mDisposable.add(WebDavApi.getApi(account.scheme + account.portal)
//            .capabilities(credential, path)
//            .doOnSubscribe { disposable: Disposable? -> viewState.onShowWaitingDialog() }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .map { responseBody: Response<ResponseBody?> ->
//                if (responseBody.isSuccessful && responseBody.code() == 207) {
//                    return@map responseBody.body()
//                } else {
//                    throw HttpException(responseBody)
//                }
//            }
//            .subscribe({ body: ResponseBody? -> saveWebDav(account) }) { throwable: Throwable? ->
//                fetchError(
//                    throwable
//                )
//            })
    }

    private fun saveWebDav(account: AccountsSqlData) {
//        account = false
//        mPreferenceTool.setDefault()
//        mPreferenceTool.portal = account.portal
//        mPreferenceTool.login = account.login
//        mPreferenceTool.password = account.password
//        mPreferenceTool.setVisitor(false)
//        mPreferenceTool.isNoPortal = false
//        account = true
//        viewState.onAccountLogin()
    }

    private fun login(account: CloudAccount?) {
        val token =
            AccountUtils.getToken(context, Account(account?.getAccountName(), context.getString(R.string.account_type)))
        if (token != null && token.isNotEmpty()) {
            viewState.onShowWaitingDialog()
            //onGetToken(account.token, account)
        } else {
            if (account?.isWebDav!!) {
//                viewState.onWebDavLogin(account)
            } else {
                account.login?.let { viewState.onSignIn(account.portal, it) }
            }
        }
    }

    //    @Override
    protected fun onErrorUser(response: Response<ResponseUser?>?) {
//        if (mClickedAccount != null) {
//            viewState.onSignIn(mClickedAccount!!.portal, mClickedAccount!!.login)
//        }
    }

    override fun onGetUser(user: User) {
        super.onGetUser(user)
        viewState.onAccountLogin()
    }

//    override var account: Boolean
//        get() = super.account
//        private set(isOnline) {
//            val account = mAccountSqlTool.getAccount(
//                mPreferenceTool.portal!!,
//                mPreferenceTool.login,
//                mPreferenceTool.socialProvider
//            )
//            if (account != null) {
//                account.isOnline = isOnline
//                mAccountSqlTool.setAccount(account)
//            }
//        }

    fun removeAccount() {
        if (contextAccount != null) {
            if (contextAccount!!.isOnline) {
                preferenceTool.setDefault()
            }
            CoroutineScope(Dispatchers.Default).launch {
                accountDao.deleteAccount(contextAccount!!)
                if (accountDao.getAccounts().isEmpty()) {
                    viewState.onEmptyList()
                } else {
                    withContext(Dispatchers.Main) {
                        viewState.removeItem(mContextPosition)
                    }
                }
            }
        }
    }

    fun logout() {
        if (contextAccount != null) {
            CoroutineScope(Dispatchers.Main).launch {
                AccountUtils.setPassword(
                    context,
                    Account(contextAccount?.getAccountName(), context.getString(R.string.account_type)),
                    ""
                )
                AccountUtils.setToken(
                    context,
                    Account(contextAccount?.getAccountName(), context.getString(R.string.account_type)),
                    ""
                )
                val logoutAccount = contextAccount?.copy(isOnline = false)
                logoutAccount?.let { accountDao.updateAccount(it) }
                logoutAccount?.let { viewState.onUpdateItem(it, mContextPosition) }
            }
        }
    }

    fun signIn() {
        if (contextAccount != null) {
            login(contextAccount!!)
        }
    }

    fun longClick(account: AccountsSqlData, position: Int) {
        //        mSelectedItems = ArrayList()
        //        isSelectionMode = true
        //        account.isSelection = true
        //        mSelectedItems.add(account)
        //        viewState.onSelectionMode()
        //        viewState.onActionBarTitle(mSelectedItems.size.toString())
    }

    fun onBackPressed(): Boolean {
        if (isSelectionMode) {
            isSelectionMode = false
            //            clearSelected()
            viewState.onDefaultState()
            return true
        }
        return false
    }

    //    private fun clearSelected() {
    //        for (account in mSelectedItems!!) {
    //            account.isSelection = false
    //        }
    //        mSelectedItems!!.clear()
    //        mSelectedItems = null
    //    }

    fun selectAll(itemList: MutableList<CloudAccount>) {
//            for (account in itemList) {
//                if (!account.isSelection && !account.isIdNull) {
//                    account.isSelection = true
//    //                mSelectedItems!!.add(account)
//                }
//            }
//            viewState.onActionBarTitle(mSelectedItems!!.size.toString())
//            viewState.onNotifyItems()
    }

    fun deleteAll() {
        if (mSelectedItems != null) {
            for (account in mSelectedItems!!) {
                if (account.isOnline) {
                    preferenceTool.setDefault()
                }
                //                mAccountSqlTool.delete(account)
            }
            onBackPressed()
            if (accountSqlTool.accounts.isEmpty()) {
                viewState.onEmptyList()
            } else {
                //                viewState.onSetAccounts(mAccountSqlTool.accounts)
            }
        }
    }

    fun deselectAll() {
        if (mSelectedItems != null) {
            for (account in mSelectedItems!!) {
//                account.isSelection = false
            }
            mSelectedItems!!.clear()
            viewState.onActionBarTitle(mSelectedItems!!.size.toString())
            viewState.onNotifyItems()
        }
    }

    //    private fun checkUpdate(accounts: List<CloudAccount>) {
    //        for (account in accounts) {
    //            if (!account.isWebDav && account.token != null && !account.token.isEmpty()) {
    //                mDisposable.add(getUser(account)
    //                    .subscribeOn(Schedulers.io())
    //                    .observeOn(AndroidSchedulers.mainThread())
    //                    .map { responseUser: ResponseUser ->
    //                        if (responseUser.response == null) {
    //                            return@map User()
    //                        } else {
    //                            return@map responseUser.response
    //                        }
    //                    }
    //                    .subscribe(
    //                        { user: User -> updateAccount(user, account) }
    //                    ) { throwable: Throwable -> Log.d(TAG, "checkUpdate: " + throwable.message) })
    //            }
    //        }
    //    }

    //    private fun updateAccount(user: User, account: AccountsSqlData) {
    //        if (!user.displayNameHtml.isEmpty() && account.name != user.displayNameHtml) {
    //            account.name = user.displayNameHtml
    //        }
    //        if (!user.avatar.isEmpty() && account.avatarUrl != user.avatar) {
    //            account.avatarUrl = user.avatar
    //        }
    //        mAccountSqlTool.setAccount(account)
    //        viewState.onNotifyItems()
    //    }
    //
    //    private fun getUser(account: AccountsSqlData): Observable<ResponseUser> {
    //        val token = account.token
    //        val retrofitTool = RetrofitTool(mContext)
    //        retrofitTool.isCiphers = account.isSslCiphers
    //        retrofitTool.isSslOn = account.isSslState
    //        return Observable.fromCallable {
    //            val response = retrofitTool.getApi(account.scheme + account.portal)
    //                .getUserInfo(token).execute()
    //            if (response.isSuccessful) {
    //                return@fromCallable response.body()
    //            } else {
    //                return@fromCallable ResponseUser()
    //            }
    //        }
    //    }
    //
    //
    //
    //    fun restoreAccount() {
    //        val accountsSqlData = mAccountSqlTool.accountOnline
    //        if (accountsSqlData != null) {
    //            mPreferenceTool.login = accountsSqlData.login
    //            mPreferenceTool.scheme = accountsSqlData.scheme
    //            mPreferenceTool.portal = accountsSqlData.portal
    //            mPreferenceTool.token = accountsSqlData.token
    //            mPreferenceTool.sslState = accountsSqlData.isSslState
    //            mPreferenceTool.sslCiphers = accountsSqlData.isSslCiphers
    //        }
    //    }

    fun accountClick(account: CloudAccount) {
        val token =
            AccountUtils.getToken(context, Account(account.getAccountName(), context.getString(R.string.account_type)))
        val portal = account.portal
        if (token != null && token.isNotEmpty() && portal != null && portal.isNotEmpty()) {
            disposable =
                service.getUserInfo(token).subscribe({ response ->
                    when (response) {
                        is LoginResponse.Success -> {
                            setAccount(account)
                            try {
                                viewState.onAccountLogin()
                            } catch (urlSyntaxMistake: UrlSyntaxMistake) {
                                urlSyntaxMistake.printStackTrace()
                            }
                        }
                        is LoginResponse.Error -> {
                            account.token = ""
                            account.login?.let { viewState.onSignIn(portal, it) }
                        }
                    }
                }, {
                    fetchError(it)
                })
        } else if (token != null && token.isEmpty()) {
            account.login?.let { viewState.onSignIn(portal, it) }
        } else {
            viewState.onError(context.getString(R.string.errors_sign_in_account_error))
        }
    }

    private fun setAccount(cloudAccount: CloudAccount) {
        CoroutineScope(Dispatchers.Default).launch {
            val account = accountDao.getAccountOnline()?.let { accountDao.getAccount(it.id)?.copy(isOnline = false) }
            account?.let { accountDao.updateAccount(it) }
            accountDao.updateAccount(cloudAccount.copy(isOnline = true))
        }
    }

    fun accountLongClick(account: CloudAccount) {
    }

//    fun selectAll(itemList: List<CloudAccount>?) {
//
//    }
//
//    fun deselectAll() {
//
//    }
//
//    fun deleteAll() {
//
//    }
}