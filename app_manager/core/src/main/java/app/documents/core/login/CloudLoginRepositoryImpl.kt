package app.documents.core.login

import app.documents.core.account.AccountRepository
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.Scheme
import app.documents.core.model.login.Token
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.request.RequestRegister
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.request.RequestValidatePortal
import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.asResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.CloudLoginDataSource
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
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException

internal class CloudLoginRepositoryImpl(
    private val cloudPortal: CloudPortal?,
    private val cloudLoginDataSource: CloudLoginDataSource,
    private val accountRepository: AccountRepository,
    private val isGooglePlayServicesAvailable: Boolean
) : CloudLoginRepository {

    private var savedAccessToken: String? = null

    private val PortalProvider.registerDeviceRequired: Boolean
        get() = this is PortalProvider.Cloud.Workspace || this == PortalProvider.Cloud.DocSpace

    override suspend fun checkPortal(url: String, scheme: Scheme): Flow<PortalResult> {
        return flow {
            try {
                val capabilities = cloudLoginDataSource.getCapabilities()
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
            } catch (e: ConnectException) {
                if (scheme == Scheme.Https) {
                    emit(PortalResult.ShouldUseHttp)
                } else {
                    throw e
                }
            }
        }.catch { cause -> emit(PortalResult.Error(cause)) }
    }

    override suspend fun logOut(accountId: String): Flow<NetworkResult<*>> {
        return flowOf(accountRepository.logOut(accountId))
            .onEach(::unsubscribePush)
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun deleteAccounts(vararg accountIds: String): Flow<NetworkResult<List<CloudAccount>>> {
        return flowOf(accountRepository.deleteAccounts(accountIds, ::unsubscribePush))
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

    override suspend fun signInWithProvider(accessToken: String?, provider: String, code: String?): Flow<LoginResult> {
        if (provider == ApiContract.Social.GOOGLE) savedAccessToken = accessToken
        return signIn(
            request = RequestSignIn(
                accessToken = checkNotNull(accessToken ?: savedAccessToken),
                provider = provider,
                code = code.orEmpty()
            )
        )
    }

    override suspend fun signInWithSSO(accessToken: String): Flow<NetworkResult<CloudAccount>> {
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
            cloudLoginDataSource.registerPortal(
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

    override suspend fun registerPersonal(email: String, language: String): Flow<NetworkResult<*>> {
        return flowOf(cloudLoginDataSource.registerPersonalPortal(RequestRegister(email, language)))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun checkLogin(accountId: String): Flow<CheckLoginResult> {
        return flowOf(accountRepository.checkLogin(accountId))
            .onEach { result ->
                if (result is CheckLoginResult.Success && result.provider is PortalProvider.Cloud) {
                    val userInfo = cloudLoginDataSource.getUserInfo(result.accessToken)

                    accountRepository.updateAccount(accountId) { account ->
                        account.copy(
                            avatarUrl = userInfo.avatarMedium,
                            name = userInfo.displayNameFromHtml,
                            isAdmin = userInfo.isAdmin,
                            isVisitor = userInfo.isVisitor,
                        )
                    }

                    try {
                        val oldAccount = accountRepository.getAccount(result.oldAccountId)
                        if (oldAccount != null) {
                            unsubscribePush(oldAccount.apply { unsubToken = accountRepository.getToken(oldAccount.accountName).orEmpty() })
                        }

                        val newAccount = accountRepository.getOnlineAccount()
                        val token = accountRepository.getToken(newAccount?.accountName.orEmpty())
                        val portal = newAccount?.portal
                        if (token != null && portal != null) {
                            subscribePush(portal, token)
                        }
                    } catch (_: Exception) {
                        // Nothing
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
    ): Flow<NetworkResult<*>> {
        return flowOf(cloudLoginDataSource.sendSms(userName, password, provider, accessToken))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun passwordRecovery(portal: String, email: String): Flow<NetworkResult<*>> {
        return flowOf(cloudLoginDataSource.forgotPassword(portal, email))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override fun getSavedPortals(): Flow<List<String>> {
        return flow { emit(accountRepository.getPortals()) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun changeNumber(requestNumber: RequestNumber): Flow<NetworkResult<*>> {
        return flowOf(cloudLoginDataSource.changeNumber(requestNumber))
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun validatePortal(portalName: String): Flow<NetworkResult<*>> {
        return flow {
            cloudLoginDataSource.validatePortal(RequestValidatePortal(portalName))
            emit(null)
        }
            .flowOn(Dispatchers.IO)
            .asResult()
    }

    override suspend fun handleIOException(exception: IOException): Flow<Boolean> {
        return flowOf(accountRepository.handleIOException(exception))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updatePortalSettings() {
        try {
            cloudPortal?.let { portal ->
                val token = accountRepository.getToken(accountRepository.getOnlineAccount()?.accountName.orEmpty())
                val cloudPortal = cloudLoginDataSource.getPortalSettings(portal, token.orEmpty())
                accountRepository.updateAccount { it.copy(portal = cloudPortal) }
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun registerDevice(portalUrl: String, token: String, deviceToken: String) {
        cloudLoginDataSource.registerDevice(portalUrl, token, deviceToken)
    }

    private fun signIn(request: RequestSignIn): Flow<LoginResult> {
        return flow {
            val response = cloudLoginDataSource.signIn(request)
            when {
                !response.token.isNullOrEmpty() -> {
                    val account = onSuccessResponse(request, response)
                    emit(LoginResult.Success(account))
                }
                response.tfa -> emit(LoginResult.Tfa(response.tfaKey))
                response.sms -> emit(LoginResult.Sms(response.phoneNoise))
            }
        }
            .catch { cause ->
                if (cause is HttpException && cause.code() != ApiContract.HttpCodes.CLIENT_PAYMENT_REQUIRED) {
                    emit(LoginResult.Error(cause))
                }
            }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun onSuccessResponse(request: RequestSignIn, response: Token): CloudAccount {
        val accessToken = requireNotNull(response.token)
        val userInfo = cloudLoginDataSource.getUserInfo(accessToken)
        val cloudAccount = CloudAccount(
            id = userInfo.id,
            portalUrl = requireNotNull(cloudPortal).url,
            login = request.userName.ifEmpty { userInfo.email.orEmpty() },
            name = userInfo.displayNameFromHtml,
            avatarUrl = userInfo.avatarMedium,
            socialProvider = request.provider,
            isAdmin = userInfo.isAdmin,
            isVisitor = userInfo.isVisitor,
            portal = cloudLoginDataSource.getPortalSettings(cloudPortal, requireNotNull(response.token))
        )
        accountRepository.addAccount(
            cloudAccount = cloudAccount,
            accessToken = accessToken,
            onOldAccount = ::unsubscribePush
        )

        try {
            subscribePush(cloudAccount.portal, response.token.orEmpty())
        } catch (_: Exception) {
            // Stub
        }

        return cloudAccount
    }

    private suspend fun unsubscribePush(account: CloudAccount) {
        if (isGooglePlayServicesAvailable) {
            val token = account.unsubToken
            if (account.portal.provider.registerDeviceRequired || token.isNotEmpty()) {
                try {
                    val deviceToken = getDeviceToken()
                    cloudLoginDataSource.subscribe(
                        portal = account.portal,
                        token = token,
                        deviceToken = deviceToken,
                        isSubscribe = false
                    )
                } catch (_: Exception) {
                    // Stub
                }
            }
        }
    }

    private suspend fun unsubscribePush(accounts: List<CloudAccount>) {
        accounts.forEach { account ->
            unsubscribePush(account)
        }
    }

    private suspend fun subscribePush(cloudPortal: CloudPortal, accessToken: String) {
        if (cloudPortal.provider.registerDeviceRequired && isGooglePlayServicesAvailable) {
            try {
                val deviceToken = getDeviceToken()
                cloudLoginDataSource.registerDevice(accessToken, deviceToken)
                cloudLoginDataSource.subscribe(cloudPortal, accessToken, deviceToken, true)
            } catch (_: Exception) {
                // Stub
            }
        }
    }

    private suspend fun getDeviceToken(): String {
        return FirebaseMessaging.getInstance()
            .token
            .addOnFailureListener(::error)
            .await()
    }
}