package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.Token
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.request.RequestRegister
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.request.RequestValidatePortal
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.net.UnknownHostException

internal class LoginRepositoryImpl(
    private val cloudPortal: CloudPortal?,
    private val loginDataSource: LoginDataSource,
    private val accountRepository: AccountRepository
) : LoginRepository {

    private var savedAccessToken: String? = null

    private val PortalProvider.registerDeviceRequired: Boolean
        get() = this is PortalProvider.Cloud.Workspace || this == PortalProvider.Cloud.DocSpace

    override suspend fun checkPortal(url: String, scheme: Scheme): Flow<PortalResult> {
        return flow {
            try {
                val capabilities = loginDataSource.getCapabilities()
                val cloudPortal = CloudPortal(
                    scheme = scheme,
                    url = url,
                    socialProviders = capabilities.providers,
                    settings = PortalSettings(
                        ssoLabel = capabilities.ssoLabel,
                        ldap = capabilities.ldapEnabled,
                        ssoUrl = capabilities.ssoUrl
                    )
                )
                emit(PortalResult.Success(cloudPortal))
            } catch (e: UnknownHostException) {
                if (scheme == Scheme.Https) {
                    emit(PortalResult.ShouldUseHttp)
                } else {
                    throw e
                }
            }
        }.catch { cause -> emit(PortalResult.Error(cause)) }
    }

    override suspend fun logOut(accountId: String): Flow<Result<*>> {
        return flowOf(accountRepository.logOut(accountId))
            .onEach(::unsubscribePush)
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun deleteAccounts(vararg accountIds: String): Flow<Result<List<CloudAccount>>> {
        return flowOf(accountRepository.deleteAccounts(accountIds))
            .onEach { accounts -> accounts.forEach { account -> unsubscribePush(account) } }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun signInByEmail(email: String, password: String, code: String?): Flow<LoginResult> {
        return signIn(
            request = RequestSignIn(
                userName = email,
                password = password,
                code = code.orEmpty()
            )
        )
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
        return flowOf(
            loginDataSource.registerPortal(
                RequestRegister(
                    firstName = firstName,
                    email = email,
                    lastName = lastName,
                    portalName = portalName,
                    password = password,
                    recaptchaResponse = recaptchaResponse
                )
            )
        ).flowOn(Dispatchers.IO)
    }

    override suspend fun registerPersonal(email: String, language: String): Flow<Result<*>> {
        return flowOf(loginDataSource.registerPersonalPortal(RequestRegister(email, language)))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun checkLogin(accountId: String): Flow<CheckLoginResult> {
        return flowOf(accountRepository.checkLogin(accountId))
            .onEach { result ->
                if (result is CheckLoginResult.Success && result.provider is PortalProvider.Cloud) {
                    val userInfo = loginDataSource.getUserInfo(result.accessToken)
                    accountRepository.updateAccount(accountId) { account ->
                        account.copy(
                            avatarUrl = userInfo.avatarMedium,
                            name = userInfo.displayNameFromHtml,
                            isAdmin = userInfo.isAdmin,
                            isVisitor = userInfo.isVisitor
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .catch { cause -> emit(CheckLoginResult.Error(cause)) }
    }

    override suspend fun sendSms(
        userName: String,
        password: String,
        accessToken: String,
        provider: String
    ): Flow<Result<*>> {
        return flowOf(loginDataSource.sendSms(userName, password, provider, accessToken))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun passwordRecovery(portal: String, email: String): Flow<Result<*>> {
        return flowOf(loginDataSource.forgotPassword(portal, email))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override fun getSavedPortals(): Flow<List<String>> {
        return flow { emit(accountRepository.getPortals()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun changeNumber(requestNumber: RequestNumber): Flow<Result<*>> {
        return flowOf(loginDataSource.changeNumber(requestNumber))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun validatePortal(portalName: String): Flow<Result<*>> {
        return flow {
            loginDataSource.validatePortal(RequestValidatePortal(portalName))
            emit(null)
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun handleIOException(exception: IOException): Flow<Boolean> {
        return flowOf(accountRepository.handleIOException(exception))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun registerDevice(portalUrl: String, token: String, deviceToken: String) {
        loginDataSource.registerDevice(portalUrl, token, deviceToken)
    }

    private fun signIn(request: RequestSignIn): Flow<LoginResult> {
        return flow {
            val response = loginDataSource.signIn(request)
            when {
                !response.token.isNullOrEmpty() -> {
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
        val accessToken = requireNotNull(response.token)
        val userInfo = loginDataSource.getUserInfo(accessToken)
        val cloudAccount = CloudAccount(
            id = userInfo.id,
            portalUrl = requireNotNull(cloudPortal).url,
            login = request.userName.ifEmpty { userInfo.email.orEmpty() },
            name = userInfo.displayNameFromHtml,
            avatarUrl = userInfo.avatarMedium,
            socialProvider = request.provider,
            isAdmin = userInfo.isAdmin,
            isVisitor = userInfo.isVisitor,
            portal = getPortalSettings(cloudPortal, requireNotNull(response.token))
        )
        accountRepository.addAccount(
            cloudAccount = cloudAccount,
            accessToken = accessToken,
            onOldAccount = ::unsubscribePush
        )
        subscribePush(cloudAccount.portal, response.token.orEmpty())
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

    private suspend fun unsubscribePush(account: CloudAccount) {
        val token = accountRepository.getToken(account.accountName)
        if (account.portal.provider.registerDeviceRequired && token != null) {
            loginDataSource.subscribe(
                portal = account.portal,
                token = token,
                deviceToken = getDeviceToken(),
                isSubscribe = false
            )
        }
    }

    private suspend fun subscribePush(cloudPortal: CloudPortal, accessToken: String) {
        if (cloudPortal.provider.registerDeviceRequired) {
            val deviceToken = getDeviceToken()
            loginDataSource.registerDevice(accessToken, deviceToken)
            loginDataSource.subscribe(cloudPortal, accessToken, deviceToken, true)
        }
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
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