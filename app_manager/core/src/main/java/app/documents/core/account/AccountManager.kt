package app.documents.core.account

import android.accounts.Account
import android.content.Context
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils
import javax.inject.Inject

class AccountManager @Inject constructor(private val context: Context) {

    private val accountType: String = context.getString(lib.toolkit.base.R.string.account_type)

    private fun getAccount(accountName: String): Account = Account(accountName, accountType)

    fun updateAccountData(accountName: String, update: (AccountData) -> AccountData) {
        val accountData = getAccountData(accountName)
        update.invoke(accountData)
    }

    fun addAccount(account: Account, password: String, accountData: AccountData): Boolean {
        return AccountUtils.addAccount(context, account, password, accountData)
    }

    fun addAccount(accountName: String, password: String, accountData: AccountData): Boolean {
        return addAccount(getAccount(accountName), password, accountData)
    }

    fun getAccountData(accountName: String): AccountData {
        return AccountUtils.getAccountData(context, getAccount(accountName))
    }

    fun setAccountData(account: Account, accountData: AccountData) {
        AccountUtils.setAccountData(context, account, accountData)
    }

    fun setAccountData(accountName: String, accountData: AccountData) {
        AccountUtils.setAccountData(context, getAccount(accountName), accountData)
    }

    fun setPassword(account: Account, password: String?) {
        AccountUtils.setPassword(context, account, password)
    }

    fun setPassword(accountName: String, password: String?) {
        AccountUtils.setPassword(context, getAccount(accountName), password)
    }

    fun setToken(account: Account, accessToken: String?) {
        AccountUtils.setToken(context, account, accessToken)
    }

    fun setToken(accountName: String, accessToken: String?) {
        AccountUtils.setToken(context, accountName, accessToken)
    }

    fun getToken(account: Account): String? {
        return AccountUtils.getToken(context, account)
    }

    fun getToken(accountName: String): String? {
        return AccountUtils.getToken(context, accountName)
    }

    fun removeAccount(accountName: String): Boolean {
        return AccountUtils.removeAccount(context, accountName)
    }
}