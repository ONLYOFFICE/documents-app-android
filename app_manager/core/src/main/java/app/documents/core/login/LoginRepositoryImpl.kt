package app.documents.core.login

import app.documents.core.account.AccountManager
import app.documents.core.account.AccountPreferences
import app.documents.core.database.datasource.CloudDataSource
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.request.RequestPassword
import app.documents.core.model.login.request.RequestRegister
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.LoginDataSource
import app.documents.core.utils.displayNameFromHtml
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import lib.toolkit.base.managers.utils.AccountData
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

internal class LoginRepositoryImpl(
    private val cloudPortal: CloudPortal?,
    private val loginDataSource: LoginDataSource,
    private val cloudDataSource: CloudDataSource,
    private val accountManager: AccountManager,
    private val accountPreferences: AccountPreferences
) : LoginRepository {

    private var savedAccessToken: String? = null

    override suspend fun checkPortal(portal: String, scheme: Scheme): Flow<PortalResult> {
        return flow {
            try {
                val capabilities = loginDataSource.getCapabilities()
                CloudPortal(
                    scheme = scheme,
                    url = portal,
                    settings = PortalSettings(
                        ssoLabel = capabilities.ssoLabel,
                        ldap = capabilities.ldapEnabled,
                        ssoUrl = capabilities.ssoUrl
                    )
                ).let { portal ->
                    cloudDataSource.insertOrUpdatePortal(portal)
                }
                emit(PortalResult.Success(capabilities.providers, scheme == Scheme.Http))
            } catch (e: UnknownHostException) {
                if (scheme == Scheme.Https) {
                    emit(PortalResult.ShouldUseHttp)
                } else {
                    throw e
                }
            }
        }.catch { exception ->
            emit(PortalResult.Error(exception))
        }
    }

    override suspend fun logOut(cloudAccount: CloudAccount?): Flow<Result<*>> {
        return flow {
            val account = checkNotNull(cloudAccount ?: getOnlineAccount())
            with(accountManager) {
                when (account.portal.provider) {
                    is PortalProvider.Webdav -> setPassword(account.accountName, null)
                    PortalProvider.DropBox,
                    PortalProvider.GoogleDrive,
                    PortalProvider.OneDrive -> setToken(account.accountName, "")
                    else -> {
                        setPassword(account.accountName, null)
                        setToken(account.accountName, "")
                    }
                }
            }
            accountPreferences.onlineAccountId = null
            emit(Result.Success(null))
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun logOut(accountId: String): Flow<Result<*>> {
        return logOut(checkNotNull(cloudDataSource.getAccount(accountId)))
    }

    override suspend fun deleteAccounts(vararg accountIds: String): Flow<Result<List<CloudAccount>>> {
        return flow {
            accountIds.forEach { accountId ->
                cloudDataSource.getAccount(accountId)?.let { account ->
                    if (account.portal.provider is PortalProvider.Cloud &&
                        accountId == accountPreferences.onlineAccountId
                    ) {
                        unsubscribePush(account)
                    }
                    accountManager.removeAccount(account.accountName)
                    cloudDataSource.deleteAccount(account)
                }
            }
            emit(cloudDataSource.getAccounts())
        }.flowOn(Dispatchers.IO)
            .asResult()
    }

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

    override suspend fun registerPersonal(email: String, language: String): Flow<Result<*>> {
        return flowOf(loginDataSource.registerPersonalPortal(RequestRegister(email, language)))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun checkLogin(account: CloudAccount): Flow<CheckLoginResult> {
        return flow {
            if (account.id == accountPreferences.onlineAccountId) {
                emit(CheckLoginResult.AlreadyUse)
                return@flow
            }

            val accessToken = accountManager.getToken(account.accountName)
            if (accessToken == null) {
                emit(CheckLoginResult.NeedLogin)
            } else {
                if (account.portal.provider is PortalProvider.Cloud) {
                    val userInfo = loginDataSource.getUserInfo(accessToken)
                    val cloudAccount = createCloudAccount(
                        userInfo = userInfo,
                        login = account.login,
                        socialProvider = account.socialProvider,
                        portal = account.portal
                    )

                    cloudDataSource.insertOrUpdateAccount(
                        cloudAccount.copy(
                            name = userInfo.displayNameFromHtml,
                            avatarUrl = userInfo.avatarMedium,
                            isAdmin = userInfo.isAdmin,
                            isVisitor = userInfo.isVisitor
                        )
                    )
                }
                accountPreferences.onlineAccountId = account.id
                emit(CheckLoginResult.Success)
            }
        }.flowOn(Dispatchers.IO)
            .catch { emit(CheckLoginResult.Error(it)) }
    }

    override suspend fun checkLogin(accountId: String): Flow<CheckLoginResult> {
        return checkLogin(checkNotNull(cloudDataSource.getAccount(accountId)))
    }

    override suspend fun sendSms(
        userName: String,
        password: String,
        accessToken: String,
        provider: String
    ): Flow<Result<*>> {
        return flowOf(loginDataSource.sendSms(RequestSignIn(userName, password, provider, accessToken)))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun passwordRecovery(portal: String, email: String): Flow<Result<*>> {
        return flowOf(loginDataSource.forgotPassword(RequestPassword(portal, email)))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun unsubscribePush(account: CloudAccount) {
        unsubscribePush(account.portal, accountManager.getToken(account.accountName))
    }

    override fun getSavedPortals(): Flow<List<String>> {
        return flow { emit(cloudDataSource.getPortals()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun changeNumber(requestNumber: RequestNumber): Flow<Result<*>> {
        TODO("Not yet implemented")
    }

    override suspend fun validatePortal(portalName: String): Flow<Result<*>> {
        TODO("Not yet implemented")
    }

    override suspend fun configConnection(exception: Exception): Boolean {
        val account = runBlocking { cloudDataSource.getAccount(accountPreferences.onlineAccountId.orEmpty()) }
        if (account != null) {
            val portal = account.portal
            if (exception is SSLHandshakeException && !portal.settings.isSslCiphers && portal.scheme == Scheme.Https) {
                cloudDataSource.insertOrUpdatePortal(portal.copy(settings = portal.settings.copy(isSslCiphers = true)))
                return true
            } else if ((exception is ConnectException ||
                        exception is SocketTimeoutException ||
                        exception is SSLHandshakeException ||
                        exception is SSLPeerUnverifiedException) &&
                portal.scheme == Scheme.Https
            ) {
                cloudDataSource.insertOrUpdatePortal(
                    portal.copy(
                        scheme = Scheme.Http,
                        settings = portal.settings.copy(isSslCiphers = true)
                    )
                )
                return true
            }
        }
        return false
    }

    override suspend fun registerDevice(portalUrl: String, token: String, deviceToken: String) {
        loginDataSource.registerDevice(portalUrl, token, deviceToken)
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
        var cloudAccount = cloudDataSource.getAccount(userInfo.id)
        val cloudPortal = checkNotNull(cloudDataSource.getPortal(cloudAccount?.portalUrl.orEmpty()) ?: cloudPortal)

        disableOldAccount()
        cloudAccount = createCloudAccount(userInfo, request.userName, request.provider, cloudPortal)
        addAccountToAccountManager(cloudAccount.toAccountData(response), request.password, response.token)
        subscribePush(cloudAccount.portal, response.token)

        accountPreferences.onlineAccountId = userInfo.id
        cloudDataSource.insertOrUpdateAccount(cloudAccount)
        cloudDataSource.insertOrUpdatePortal(getPortalSettings(cloudPortal, response.token))
        return cloudAccount
    }

    private suspend fun getPortalSettings(cloudPortal: CloudPortal, accessToken: String): CloudPortal {
        val settings = loginDataSource.getSettings(accessToken)
        val allSettings = loginDataSource.getAllSettings(accessToken)
        return cloudPortal.copy(
            version = PortalVersion(
                serverVersion = settings.communityServer.orEmpty(),
                documentServerVersion = settings.documentServer.orEmpty()
            ),
            provider = when {
                allSettings.docSpace -> PortalProvider.Cloud.DocSpace
                allSettings.personal -> PortalProvider.Cloud.Personal
                else -> PortalProvider.Cloud.Workspace
            }
        )
    }

    private fun createCloudAccount(
        userInfo: User,
        login: String,
        socialProvider: String,
        portal: CloudPortal
    ): CloudAccount {
        return CloudAccount(
            id = userInfo.id,
            portalUrl = portal.url,
            login = login,
            name = userInfo.displayNameFromHtml,
            avatarUrl = userInfo.avatarMedium,
            socialProvider = socialProvider,
            isAdmin = userInfo.isAdmin,
            isVisitor = userInfo.isVisitor,
            portal = portal
        )
    }

    private fun CloudAccount.toAccountData(token: Token): AccountData {
        return AccountData(
            portal = portal.url,
            scheme = portal.scheme.value,
            displayName = name,
            userId = id,
            email = login,
            avatar = avatarUrl,
            expires = token.expires
        )
    }

    private fun addAccountToAccountManager(
        accountData: AccountData,
        password: String,
        accessToken: String
    ): String? {
        with(accountData) {
            val accountName = "$email@$portal"
            if (!accountManager.addAccount(accountName, password, accountData)) {
                accountManager.setAccountData(accountName, accountData)
                accountManager.setPassword(accountName, password)
            }
            accountManager.setToken(accountName, accessToken)
            return accountManager.getToken(accountName)
        }
    }

    private suspend fun disableOldAccount() {
        getOnlineAccount()?.let { account ->
            unsubscribePush(
                account.portal,
                accountManager.getToken(account.accountName).orEmpty()
            )
        }
    }

    private suspend fun subscribePush(cloudPortal: CloudPortal, accessToken: String) {
        if (cloudPortal.provider is PortalProvider.Cloud) {
            val deviceToken = getDeviceToken()
            loginDataSource.registerDevice(accessToken, deviceToken)
            loginDataSource.subscribe(cloudPortal, accessToken, deviceToken, true)
        }
    }

    private suspend fun unsubscribePush(cloudPortal: CloudPortal, accessToken: String?) {
        if (cloudPortal.provider is PortalProvider.Cloud) {
            loginDataSource.subscribe(cloudPortal, accessToken.orEmpty(), getDeviceToken(), false)
        }
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
    }

    private suspend fun getOnlineAccount(): CloudAccount? {
        return cloudDataSource.getAccount(accountPreferences.onlineAccountId ?: return null)
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