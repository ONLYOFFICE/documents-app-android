package lib.toolkit.base.managers.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.R

/**
 * Model for account data. Key account_data
 *
 * @param portal Portal host
 * @param scheme Scheme connection
 * @param userId User id (webDav login@portal)
 * @param email Email (login)
 * @param displayName Display name. Webdav login
 * @param avatar Url from avatar
 * @param expires Token lifecycle
 */
@Serializable
data class AccountData(
    val portal: String,
    val scheme: String,
    val userId: String,
    val password: String = "",
    val email: String,
    val displayName: String,
    val refreshToken: String? = null,
    val avatar: String? = null,
    val expires: String? = null
)

// TODO: Replace with class AccountManager(context: Context) for correct injection
object AccountUtils {

    const val KEY_ACCOUNT_TYPE = "KEY_ACCOUNT_TYPE"
    const val KEY_AUTH_TYPE = "KEY_AUTH_TYPE"

    private const val ACCOUNT_DATA = "account_data"

    @JvmStatic
    fun addAccount(context: Context, account: Account, password: String, userData: AccountData): Boolean {
        return getAccountManager(context).addAccountExplicitly(account, password, Bundle().apply {
            putString(ACCOUNT_DATA, Json.encodeToString(userData))
        }, mapOf(Pair("com.onlyoffice.projects", AccountManager.VISIBILITY_VISIBLE)))
    }

    /**
     * Return account if exist
     *
     * @param context Context
     * @param accountName email@portal
     */
    @JvmStatic
    fun getAccount(context: Context, accountName: String): Account? {
        return getAccounts(context).find { it.name == accountName }
    }

    @JvmStatic
    fun setAccountData(context: Context, account: Account, data: AccountData) {
        getAccountManager(context).apply {
            setUserData(account, ACCOUNT_DATA, Json.encodeToString(data))
        }
    }

    @JvmStatic
    fun getAccountData(context: Context, account: Account): AccountData {
        return Json.decodeFromString(getAccountManager(context).getUserData(account, ACCOUNT_DATA))
    }

    @JvmStatic
    fun removeAccount(context: Context, account: Account): Boolean {
        return getAccountManager(context).removeAccountExplicitly(account)
    }

    @JvmStatic
    fun removeAccount(context: Context, name: String): Boolean {
        return getAccountManager(context).removeAccountExplicitly(
            Account(
                name,
                context.getString(R.string.account_type)
            )
        )
    }

    @JvmStatic
    fun setPassword(context: Context, account: Account, password: String?) {
        getAccountManager(context).setPassword(account, password)
    }

    @JvmStatic
    fun setPassword(context: Context, name: String, password: String?) {
        getAccountManager(context).setPassword(Account(name, context.getString(R.string.account_type)), password)
    }

    @JvmStatic
    fun getPassword(context: Context, account: Account): String? {
        return getAccountManager(context).getPassword(account)
    }

    @JvmStatic
    fun getPassword(context: Context, name: String): String? {
        return getAccountManager(context).getPassword(Account(name, context.getString(R.string.account_type)))
    }

    @JvmStatic
    fun setToken(context: Context, account: Account, token: String?) {
        getAccountManager(context).setAuthToken(account, context.getString(R.string.account_auth_type), token)
    }

    @JvmStatic
    fun setToken(context: Context, name: String, token: String?) {
        getAccountManager(context).setAuthToken(
            Account(name, context.getString(R.string.account_type)),
            context.getString(R.string.account_auth_type),
            token
        )
    }

    @JvmStatic
    fun getToken(context: Context, account: Account): String? {
        return getAccountManager(context).peekAuthToken(account, context.getString(R.string.account_auth_type))
    }

    @JvmStatic
    fun getToken(context: Context, accountName: String): String? {
        return getAccountManager(context).peekAuthToken(
            Account(accountName, context.getString(R.string.account_type)),
            context.getString(R.string.account_auth_type)
        )
    }

    @JvmStatic
    fun getAccountManager(context: Context): AccountManager {
        return AccountManager.get(context)
    }

    @JvmStatic
    private fun getAccounts(context: Context): Array<Account> {
        return getAccountManager(context).getAccountsByType(context.getString(R.string.account_type))
    }

}