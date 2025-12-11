package app.editors.manager.managers.accounts

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import app.documents.core.login.LoginResult
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.ui.activities.login.PortalsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
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
                    getToken(cloudAccount, response, AccountUtils.getPassword(context, cloudAccount.accountName))
                } else {
                    Bundle.EMPTY
                }
            }
        }
        return Bundle.EMPTY
    }

    private fun getToken(
        cloudAccount: CloudAccount,
        response: AccountAuthenticatorResponse?,
        password: String?
    ): Bundle = runBlocking(Dispatchers.Main) {
        App.getApp().refreshLoginComponent(context.accountOnline?.portal)
        val signInResponse = App.getApp().loginComponent.cloudLoginRepository.signInByEmail(
            email = cloudAccount.login,
            password = password ?: "",
            code = null
        ).firstOrNull()

        when (signInResponse) {
            is LoginResult.Success -> {
                val result = bundleOf(
                    AccountManager.KEY_AUTHTOKEN to
                            AccountUtils.getToken(context, signInResponse.cloudAccount.accountName)
                )
                response?.onResult(result)
                return@runBlocking result
            }
            is LoginResult.Error, null -> {
                response?.onError(1, "need provider token")
                return@runBlocking Bundle.EMPTY
            }
            else -> return@runBlocking Bundle.EMPTY
        }
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
