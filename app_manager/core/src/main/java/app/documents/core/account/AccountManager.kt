package app.documents.core.account

import android.accounts.Account
import android.content.Context
import lib.toolkit.base.managers.utils.AccountData
import lib.toolkit.base.managers.utils.AccountUtils

class AccountManager(private val context: Context) {

    fun addAccount(account: Account, password: String, accountData: AccountData): Boolean {
        return AccountUtils.addAccount(context, account, password, accountData)
    }

    fun setAccountData(account: Account, accountData: AccountData) {
        AccountUtils.setAccountData(context, account, accountData)
    }

    fun setPassword(account: Account, password: String?) {
        AccountUtils.setPassword(context, account, password)
    }

    fun setToken(account: Account, accessToken: String?) {
        AccountUtils.setToken(context, account, accessToken)
    }

    fun getToken(account: Account): String? {
        return AccountUtils.getToken(context, account)
    }

    fun getToken(accountName: String): String? {
        return AccountUtils.getToken(context, accountName)
    }
}