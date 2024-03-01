package app.documents.core.login

import android.accounts.Account
import app.documents.core.account.AccountManager
import app.documents.core.di.dagger.AccountType
import app.documents.core.model.login.RequestDeviceToken
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestRegister
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import lib.toolkit.base.managers.utils.AccountData

internal class LoginRepositoryImpl(
    private val loginDataSource: LoginDataSource,
    private val networkSettings: NetworkSettings,
    @AccountType private val accountType: String,
    private val accountManager: AccountManager,
    private val accountDao: AccountDao
) : LoginRepository {

    private var savedAccessToken: String? = null

    override suspend fun signInByEmail(email: String, password: String, code: String?): Flow<LoginResult> {
        return signIn(request = RequestSignIn(userName = email, password = password, code = code.orEmpty()))
    }

    override suspend fun signInWithProvider(accessToken: String?, provider: String): Flow<LoginResult> {
        if (provider == ApiContract.Social.GOOGLE) savedAccessToken = accessToken
        return signIn(
            request = RequestSignIn(
                accessToken = checkNotNull(accessToken ?: savedAccessToken),
                provider = provider
            )
        )
    }

    override suspend fun signInWithSSO(accessToken: String): Flow<Result<CloudAccount>> {
        return flowOf(onSuccessResponse(RequestSignIn(accessToken = accessToken), Token(token = accessToken)))
            .asResult()
    }

    override suspend fun signInWithToken(accessToken: String): Flow<Result<*>> {
        return flowOf(loginDataSource.getUserInfo(accessToken))
            .flowOn(Dispatchers.IO)
            .map { Result.Success(null) }
            .catch { cause -> Result.Error(cause) }
    }

    override suspend fun registerPortal(
        portalName: String,
        email: String,
        firstName: String,
        lastName: String,
        password: String,
        recaptchaResponse: String
    ): Flow<ResponseRegisterPortal> {
        return flowOf(loginDataSource.registerPortal(RequestRegister()))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun switchAccount(account: CloudAccount): Flow<Result<*>> {
        return flow {
            accountDao.getAccountOnline()?.let { oldAccount ->
                val token = accountManager.getToken(oldAccount.getAccountName())
                setSettings(oldAccount)
                unsubscribePush(token.orEmpty())
                setSettings(account)
                accountDao.updateAccount(oldAccount.copyWithToken(isOnline = false))
                accountDao.updateAccount(account.copyWithToken(isOnline = true))
            } ?: run {
                setSettings(account)
                accountDao.updateAccount(account.copyWithToken(isOnline = true))
            }
            emit(Result.Success(null))
        }.flowOn(Dispatchers.IO)
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
            unsubscribePush(accessToken.orEmpty())
            accountDao.updateAccount(it.copyWithToken(isOnline = false))
        }
    }

    private suspend fun subscribePush(accessToken: String) {
        val deviceToken = getDeviceToken()
        loginDataSource.registerDevice(accessToken, RequestDeviceToken(deviceToken))
        loginDataSource.subscribe(accessToken, deviceToken, true)
        //        preferenceTool.deviceMessageToken = deviceToken
    }

    private suspend fun unsubscribePush(accessToken: String) {
        loginDataSource.subscribe(accessToken.orEmpty(), getDeviceToken(), false)
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
    }

    private fun setSettings(account: CloudAccount) {
        networkSettings.setBaseUrl(account.portal ?: "")
        networkSettings.setScheme(account.scheme ?: "")
        networkSettings.setSslState(account.isSslState)
        networkSettings.setCipher(account.isSslCiphers)
    }


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