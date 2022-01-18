package app.editors.manager.managers.accounts

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import app.documents.core.account.CloudAccount
import app.documents.core.login.LoginResponse
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.response.ResponseSignIn
import app.editors.manager.app.App
import app.editors.manager.ui.activities.login.PortalsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.R as Toolkit

class AuthenticatorAccounts(private val context: Context) : AbstractAccountAuthenticator(context) {

    companion object {
        const val ACCOUNT_KEY = "account_key"
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
        return Bundle.EMPTY
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        return Bundle().apply {
            putParcelable(
                AccountManager.KEY_INTENT,
                PortalsActivity.getAddAccountIntent(context, accountType, authTokenType, response)
            )
        }
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        return Bundle.EMPTY
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        authTokenType?.let { tokenType ->
            options?.let { data ->
                return if (tokenType == context.getString(Toolkit.string.account_auth_type) && data.containsKey(
                        ACCOUNT_KEY
                    )
                ) {
                    val cloudAccount = Json.decodeFromString<CloudAccount>(data.getString(ACCOUNT_KEY) ?: "")
                    getToken(cloudAccount, response, AccountUtils.getPassword(context, cloudAccount.getAccountName()))
                } else {
                    Bundle.EMPTY
                }
            }
        } ?: run {
            return Bundle.EMPTY
        }
    }

    private fun getToken(cloudAccount: CloudAccount, response: AccountAuthenticatorResponse?, password: String?) =
        runBlocking(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                if (cloudAccount.provider?.isNotEmpty() == true) {
                    response?.onError(1, "need provider token")
                    return@async Bundle.EMPTY
                } else {
                    val signInResponse = App.getApp().appComponent.loginService
                        .signIn(
                            RequestSignIn(
                                userName = cloudAccount.login ?: "",
                                password = password ?: "",
                            )
                        ).blockingGet()
                    if (signInResponse is LoginResponse.Success) {
                        val token = (signInResponse.response as ResponseSignIn).response.token
                        AccountUtils.setToken(context, cloudAccount.getAccountName(), token)
                        return@async Bundle().apply {
                            putString(
                                AccountManager.KEY_AUTHTOKEN,
                                token
                            )
                        }
                    } else {
                        response?.onError(1, "need provider token")
                        return@async Bundle.EMPTY
                    }
                }
            }.await()
            response?.onResult(result)
            return@runBlocking result
        }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return context.getString(Toolkit.string.account_auth_type)
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        return Bundle.EMPTY
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        return Bundle.EMPTY
    }
}