package app.documents.core.login

import android.accounts.Account
import app.documents.core.account.AccountManager
import app.documents.core.di.dagger.AccountType
import app.documents.core.model.login.RequestDeviceToken
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.network.login.LoginDataSource
import app.documents.core.storage.account.AccountDao
import app.documents.core.storage.account.CloudAccount
import app.documents.core.storage.account.copyWithToken
import app.documents.core.storage.preference.NetworkSettings
import app.documents.core.utils.displayNameFromHtml
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import lib.toolkit.base.managers.utils.AccountData

internal class LoginRepositoryImpl(
    private val loginDataSource: LoginDataSource,
    private val networkSettings: NetworkSettings,
    @AccountType private val accountType: String,
    private val accountManager: AccountManager,
    private val accountDao: AccountDao
) : LoginRepository {

    override fun signInByEmail(email: String, password: String): Flow<LoginResult> {
        return signIn(request = RequestSignIn(userName = email, password = password))
    }

    override fun signInWithProvider(accessToken: String, provider: String): Flow<LoginResult> {
        return signIn(request = RequestSignIn(accessToken = accessToken, provider = provider))
    }

    private fun signIn(request: RequestSignIn): Flow<LoginResult> {
        return flow {
            val response = loginDataSource.signIn(request)
            when {
                response.token.isNotEmpty() -> {
                    val account = onSuccessResponse(request, response)
                    emit(LoginResult.Success(account))
                }
                response.tfa -> emit(LoginResult.Tfa(response.tfaKey))
                response.sms -> emit(LoginResult.Sms(response.phoneNoise))
            }
        }
            .catch { cause -> emit(LoginResult.Error(cause)) }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun onSuccessResponse(request: RequestSignIn, response: Token): CloudAccount {
        val userInfo = loginDataSource.getUserInfo(response.token)
        val account = createCloudAccount(userInfo, response, request.userName, request.provider)
        val accountData = account.toAccountData(response.token)
        val oldToken = addAccountToAccountManager(accountData, request.password)
        disableOldAccount(oldToken)
        accountDao.addAccount(account)
        subscribePush(response.token)
        return account
    }

    private fun createCloudAccount(
        userInfo: User,
        token: Token,
        login: String,
        provider: String
    ): CloudAccount {
        return CloudAccount(
            id = userInfo.id,
            login = login,
            portal = networkSettings.getPortal(),
            scheme = networkSettings.getScheme(),
            name = userInfo.displayNameFromHtml,
            provider = provider,
            avatarUrl = userInfo.avatarMedium,
            serverVersion = networkSettings.serverVersion,
            isSslCiphers = networkSettings.getCipher(),
            isSslState = networkSettings.getSslState(),
            isOnline = true,
            isAdmin = userInfo.isAdmin,
            isVisitor = userInfo.isVisitor
        ).apply {
            expires = token.expires
            setCryptToken(token.token)
            setCryptPassword(password)
        }
    }

    private fun CloudAccount.toAccountData(accessToken: String): AccountData {
        return AccountData(
            portal = portal.orEmpty(),
            scheme = scheme.orEmpty(),
            displayName = name.orEmpty(),
            userId = id,
            provider = provider.orEmpty(),
            accessToken = accessToken,
            email = login.orEmpty(),
            avatar = avatarUrl,
            expires = expires
        )
    }

    private fun addAccountToAccountManager(accountData: AccountData, password: String): String? {
        with(accountData) {
            val account = Account("$email@$portal", accountType)
            if (!accountManager.addAccount(account, password, accountData)) {
                accountManager.setAccountData(account, accountData)
                accountManager.setPassword(account, if (provider.isNotEmpty()) accessToken else password)
            }
            accountManager.setToken(account, accessToken)
            return accountManager.getToken(account)
        }
    }

    private suspend fun disableOldAccount(accessToken: String?) {
        accountDao.getAccountOnline()?.let {
            loginDataSource.subscribe(accessToken.orEmpty(), getDeviceToken(), false)
            accountDao.updateAccount(it.copyWithToken(isOnline = false))
        }
    }

    private suspend fun subscribePush(accessToken: String) {
        val deviceToken = getDeviceToken()
        loginDataSource.registerDevice(accessToken, RequestDeviceToken(deviceToken))
        loginDataSource.subscribe(accessToken, deviceToken, true)
        //        preferenceTool.deviceMessageToken = deviceToken
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
    }

    //    fun retrySignInWithGoogle() {
    //        signInWithGoogle(account)
    //    }

    //    @Suppress("BlockingMethodInNonBlockingContext")
    //    fun signInWithGoogle(account: Account?) {
    //        if (goggleJob != null && goggleJob?.isActive == true) {
    //            return
    //        }
    //        this.account = account
    //        goggleJob = presenterScope.launch((Dispatchers.IO)) {
    //            val scope = context.getString(R.string.google_scope)
    //            try {
    //                if (account != null) {
    //                    val accessToken = GoogleAuthUtil.getToken(context, account, scope)
    //                    withContext(Dispatchers.Main) {
    //                        signIn(
    //                            app.documents.core.network.login.models.request.RequestSignIn(
    //                                userName = account.name ?: "",
    //                                accessToken = accessToken,
    //                                provider = ApiContract.Social.GOOGLE
    //                            )
    //                        )
    //                    }
    //                }
    //            } catch (e: UserRecoverableAuthException) {
    //                withContext(Dispatchers.Main) {
    //                    onGooglePermission(e.intent)
    //                }
    //            } catch (e: Exception) {
    //                withContext(Dispatchers.Main) {
    //                    viewState.onError(e.message)
    //                }
    //            }
    //
    //        }
    //
    //    }

    //    private fun checkRedirect(requestSignIn: RequestSignIn, error: HttpException) {
    //        try {
    //            if (error.response()?.headers()?.get("Location") != null) {
    //                val url = error.response()?.headers()?.get("Location")
    //                networkSettings.setScheme((HttpUrl.parse(url ?: "")?.scheme() + "://"))
    //                networkSettings.setBaseUrl(HttpUrl.parse(url ?: "")?.host() ?: "")
    //                signIn(requestSignIn)
    //            } else {
    //                fetchError(error)
    //            }
    //        } catch (error: Throwable) {
    //            fetchError(error)
    //        }
    //    }

}