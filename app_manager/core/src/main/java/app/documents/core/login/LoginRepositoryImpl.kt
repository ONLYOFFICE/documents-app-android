package app.documents.core.login

import android.accounts.Account
import app.documents.core.account.AccountManager
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.di.dagger.AccountType
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Provider
import app.documents.core.model.cloud.Scheme
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
import app.documents.core.storage.preference.NetworkSettings
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
    private val cloudDataSource: CloudDataSource
) : LoginRepository {

    private var savedAccessToken: String? = null

    override fun getAccountData(accountName: String): AccountData {
        return accountManager.getAccountData(accountName)
    }

    override fun setAccountData(accountName: String, updateAccountData: (AccountData) -> AccountData) {
        val updated = updateAccountData.invoke(accountManager.getAccountData(accountName))
        accountManager.setAccountData(accountName, updated)
    }

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
            cloudDataSource.getAccountOnline()?.let { oldAccount ->
                val token = accountManager.getToken(oldAccount.accountName)
                setSettings(oldAccount)
                unsubscribePush(token.orEmpty())
                setSettings(account)
                cloudDataSource.updateAccount(oldAccount.copy(isOnline = false))
                cloudDataSource.updateAccount(account.copy(isOnline = true))
            } ?: run {
                setSettings(account)
                cloudDataSource.updateAccount(account.copy(isOnline = true))
            }
            emit(Result.Success(null))
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun unsubscribePush(account: CloudAccount) {
        unsubscribePush(accountManager.getToken(account.accountName))
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
        val account = createCloudAccount(userInfo, request.userName, request.provider)
        val accountData = account.toAccountData(response)
        val oldToken = addAccountToAccountManager(accountData, request.password, response.token)
        disableOldAccount()
        cloudDataSource.addAccount(account)
        subscribePush(response.token)
        return account
    }

    private fun createCloudAccount(
        userInfo: User,
        login: String,
        provider: String
    ): CloudAccount {
        val portal = CloudPortal(
            accountId = userInfo.id,
            scheme = Scheme.valueOf(networkSettings.getScheme()),
            version = PortalVersion(networkSettings.serverVersion),
            provider = PortalProvider(Provider.valueOf(provider)),
            settings = PortalSettings(
                isSslState = networkSettings.getSslState(),
                isSslCiphers = networkSettings.getCipher()
            )
        )
        return CloudAccount(
            id = userInfo.id,
            login = login,
            portal = portal,
            avatarUrl = userInfo.avatarMedium,
            isOnline = true,
            isAdmin = userInfo.isAdmin,
            isVisitor = userInfo.isVisitor
        )
    }

    private fun CloudAccount.toAccountData(token: Token): AccountData {
        return AccountData(
            portal = portal.portal,
            scheme = portal.scheme.value,
            displayName = name,
            userId = id,
            provider = portal.provider.provider?.name.orEmpty(),
            email = login,
            avatar = avatarUrl,
            expires = token.expires
        )
    }

    private fun addAccountToAccountManager(accountData: AccountData, password: String, accessToken: String): String? {
        with(accountData) {
            val account = Account("$email@$portal", accountType)
            if (!accountManager.addAccount(account, password, accountData)) {
                accountManager.setAccountData(account, accountData)
                accountManager.setPassword(account, password)
            }
            accountManager.setToken(account, accessToken)
            return accountManager.getToken(account)
        }
    }

    private suspend fun disableOldAccount() {
        cloudDataSource.getAccountOnline()?.let { account ->
            unsubscribePush(accountManager.getToken(account.accountName).orEmpty())
            cloudDataSource.updateAccount(account.copy(isOnline = false))
        }
    }

    private suspend fun subscribePush(accessToken: String) {
        val deviceToken = getDeviceToken()
        loginDataSource.registerDevice(accessToken, RequestDeviceToken(deviceToken))
        loginDataSource.subscribe(accessToken, deviceToken, true)
        //        preferenceTool.deviceMessageToken = deviceToken
    }

    private suspend fun unsubscribePush(accessToken: String?) {
        loginDataSource.subscribe(accessToken.orEmpty(), getDeviceToken(), false)
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
    }

    private fun setSettings(account: CloudAccount) {
        networkSettings.setBaseUrl(account.portal.portal)
        networkSettings.setScheme(account.portal.scheme.value)
        networkSettings.setSslState(account.portal.settings.isSslState)
        networkSettings.setCipher(account.portal.settings.isSslCiphers)
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