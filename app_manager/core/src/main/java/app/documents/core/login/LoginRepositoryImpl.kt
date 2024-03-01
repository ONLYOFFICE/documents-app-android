package app.documents.core.login

import app.documents.core.model.login.AllSettings
import app.documents.core.model.login.Capabilities
import app.documents.core.model.login.RequestDeviceToken
import app.documents.core.model.login.Settings
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.request.RequestValidatePortal
import app.documents.core.model.login.response.ResponseValidatePortal
import app.documents.core.network.common.Result
import app.documents.core.network.common.asResult
import app.documents.core.network.login.LoginDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

internal class LoginRepositoryImpl(private val loginDataSource: LoginDataSource) : LoginRepository {

    override suspend fun getServerVersion(): Flow<String> {
        return flowOf(checkNotNull(loginDataSource.getSettings().communityServer))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getCapabilities(): Flow<Capabilities> {
        return flowOf(loginDataSource.getCapabilities())
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getSettings(): Flow<Settings> {
        return flowOf(loginDataSource.getSettings())
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getAllSettings(): Flow<AllSettings> {
        return flowOf(loginDataSource.getAllSettings())
            .flowOn(Dispatchers.IO)
    }

    override suspend fun validatePortal(portalName: String): Flow<ResponseValidatePortal> {
        return flowOf(loginDataSource.validatePortal(RequestValidatePortal(portalName)))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun signIn(request: RequestSignIn, smsCode: String?): Flow<Token> {
        return flow {
            val response = smsCode?.let { sms ->
                loginDataSource.smsSignIn(request.copy(code = smsCode), sms)
            } ?: loginDataSource.signIn(request)
            emit(response)
        }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getUserInfo(token: String): Flow<User> {
        return flowOf(loginDataSource.getUserInfo(token))
            .flowOn(Dispatchers.IO)
    }

    override suspend fun setFirebaseToken(token: String, deviceToken: String): Flow<Result<Unit>> {
        return flowOf(loginDataSource.registerDevice(token, RequestDeviceToken(deviceToken))).asResult()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun subscribe(
        token: String,
        deviceToken: String,
        isSubscribe: Boolean
    ): Flow<Result<Unit>> {
        return flowOf(loginDataSource.subscribe(token, deviceToken, isSubscribe)).asResult()
            .flowOn(Dispatchers.IO)
    }

    //    override fun registerPortal(request: RequestRegister): Single<LoginResponse> {
    //        return loginService.registerPortal(
    //            request
    //        ).map { fetchResponse(it) }
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(AndroidSchedulers.mainThread())
    //    }
    //
    //    override fun registerPersonal(request: RequestRegister): Single<LoginResponse> {
    //        return loginService.registerPersonalPortal(request).map { fetchResponse(it) }
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(AndroidSchedulers.mainThread())
    //    }
    //
    //    override fun sendSms(request: RequestSignIn): Single<LoginResponse> {
    //        return loginService.sendSms(request).map { fetchResponse(it) }
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(AndroidSchedulers.mainThread())
    //    }
    //
    //    override fun changeNumber(request: RequestNumber): Single<LoginResponse> {
    //        return loginService.changeNumber(request).map { fetchResponse(it) }
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(AndroidSchedulers.mainThread())
    //    }
    //
    //    override fun passwordRecovery(request: RequestPassword): Single<LoginResponse> {
    //        return loginService.forgotPassword(request).map { fetchResponse(it) }
    //            .subscribeOn(Schedulers.io())
    //            .observeOn(AndroidSchedulers.mainThread())
    //    }
}