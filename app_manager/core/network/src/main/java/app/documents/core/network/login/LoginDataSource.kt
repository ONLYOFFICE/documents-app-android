package app.documents.core.network.login

import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.login.AllSettings
import app.documents.core.model.login.Capabilities
import app.documents.core.model.login.RequestDeviceToken
import app.documents.core.model.login.Settings
import app.documents.core.model.login.Token
import app.documents.core.model.login.User
import app.documents.core.model.login.request.RequestNumber
import app.documents.core.model.login.request.RequestPassword
import app.documents.core.model.login.request.RequestRegister
import app.documents.core.model.login.request.RequestSignIn
import app.documents.core.model.login.request.RequestValidatePortal
import app.documents.core.model.login.response.ResponseRegisterPortal
import app.documents.core.model.login.response.ResponseValidatePortal

interface LoginDataSource {

    suspend fun signIn(request: RequestSignIn): Token

    suspend fun getCapabilities(): Capabilities

    suspend fun getSettings(): Settings

    suspend fun getAllSettings(): AllSettings

    suspend fun smsSignIn(request: RequestSignIn, smsCode: String): Token

    suspend fun sendSms(request: RequestSignIn): Token

    suspend fun changeNumber(request: RequestNumber): Token

    suspend fun validatePortal(request: RequestValidatePortal): ResponseValidatePortal

    suspend fun registerPortal(request: RequestRegister): ResponseRegisterPortal

    suspend fun registerPersonalPortal(request: RequestRegister): String

    suspend fun getUserInfo(token: String): User

    suspend fun forgotPassword(request: RequestPassword): String

    suspend fun registerDevice(token: String, request: RequestDeviceToken)

    suspend fun subscribe(portal: CloudPortal, token: String, deviceToken: String, isSubscribe: Boolean)
}