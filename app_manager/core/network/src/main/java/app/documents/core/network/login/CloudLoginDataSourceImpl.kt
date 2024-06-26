package app.documents.core.network.login

import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.login.AllSettings
import app.documents.core.model.login.Capabilities
import app.documents.core.model.login.RequestDeviceToken
import app.documents.core.model.login.RequestPushSubscribe
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
import app.documents.core.network.API_VERSION
import app.documents.core.network.BaseResponse
import app.documents.core.network.HEADER_ACCEPT
import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HEADER_CACHE
import app.documents.core.network.HEADER_CONTENT_OPERATION_TYPE
import app.documents.core.network.VALUE_ACCEPT
import app.documents.core.network.VALUE_CACHE
import app.documents.core.network.VALUE_CONTENT_TYPE
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

private interface LoginApi {

    @Headers(
        "$HEADER_CACHE: $VALUE_CACHE",
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/authentication")
    suspend fun signIn(@Body body: RequestSignIn): BaseResponse<Token>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET("api/$API_VERSION/capabilities")
    suspend fun getCapabilities(): BaseResponse<Capabilities>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET("api/$API_VERSION/settings/version/build")
    suspend fun getSettings(@Header(HEADER_AUTHORIZATION) accessToken: String): BaseResponse<Settings>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET("api/$API_VERSION/settings")
    suspend fun getAllSettings(@Header(HEADER_AUTHORIZATION) accessToken: String): BaseResponse<AllSettings>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/authentication/{sms_code}")
    suspend fun smsSignIn(
        @Body body: RequestSignIn,
        @Path(value = "sms_code") smsCode: String
    ): BaseResponse<Token>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/authentication/sendsms")
    suspend fun sendSms(@Body body: RequestSignIn): BaseResponse<Token>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/authentication/setphone")
    suspend fun changeNumber(@Body body: RequestNumber): BaseResponse<Token>

    @Headers("$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE")
    @POST("/apisystem/portal/validateportalname")
    suspend fun validatePortal(@Body body: RequestValidatePortal): ResponseValidatePortal

    @POST("/apisystem/portal/register")
    suspend fun registerPortal(
        @Body body: RequestRegister
    ): ResponseRegisterPortal

    @Headers("$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE")
    @POST("api/$API_VERSION/authentication/register")
    suspend fun registerPersonalPortal(@Body body: RequestRegister): BaseResponse<String>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @GET("api/$API_VERSION/people/@self")
    suspend fun getUserInfo(@Header(HEADER_AUTHORIZATION) token: String): BaseResponse<User>

    @Headers(
        "$HEADER_CONTENT_OPERATION_TYPE: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/people/password")
    suspend fun forgotPassword(@Body body: RequestPassword?): BaseResponse<String>

    @Headers(
        "$HEADER_ACCEPT: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/settings/push/docregisterdevice")
    suspend fun registerDevice(
        @Header(HEADER_AUTHORIZATION) token: String,
        @Body body: RequestDeviceToken,
    )

    @Headers(
        "$HEADER_ACCEPT: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @POST("api/$API_VERSION/settings/push/docregisterdevice")
    suspend fun registerDevice(
        @Url url: String,
        @Header(HEADER_AUTHORIZATION) token: String,
        @Body body: RequestDeviceToken,
    )

    @Headers(
        "$HEADER_ACCEPT: $VALUE_CONTENT_TYPE",
        "$HEADER_ACCEPT: $VALUE_ACCEPT"
    )
    @PUT
    suspend fun subscribe(
        @Url portalUrl: String,
        @Header(HEADER_AUTHORIZATION) token: String,
        @Body body: RequestPushSubscribe
    )
}

internal class CloudLoginDataSourceImpl(
    json: Json,
    okHttpClient: OkHttpClient,
    cloudPortal: CloudPortal?
) : CloudLoginDataSource {

    private val api: LoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .baseUrl(cloudPortal?.urlWithScheme ?: "https://localhost")
        .build()
        .create(LoginApi::class.java)

    override suspend fun signIn(request: RequestSignIn): Token {
        return if (request.code.isNotEmpty())
            api.smsSignIn(request, request.code).response else
            api.signIn(request).response
    }

    override suspend fun getCapabilities(): Capabilities {
        return api.getCapabilities().response
    }

    override suspend fun getSettings(accessToken: String): Settings {
        return api.getSettings(accessToken).response
    }

    override suspend fun getAllSettings(accessToken: String): AllSettings {
        return api.getAllSettings(accessToken).response
    }

    override suspend fun smsSignIn(request: RequestSignIn, smsCode: String): Token {
        return api.smsSignIn(request, smsCode).response
    }

    override suspend fun sendSms(userName: String, password: String, provider: String, accessToken: String): Token {
        return api.sendSms(RequestSignIn(userName, password, provider, accessToken)).response
    }

    override suspend fun changeNumber(request: RequestNumber): Token {
        return api.changeNumber(request).response
    }

    override suspend fun validatePortal(request: RequestValidatePortal): ResponseValidatePortal {
        return api.validatePortal(request)
    }

    override suspend fun registerPortal(request: RequestRegister): ResponseRegisterPortal {
        return api.registerPortal(request)
    }

    override suspend fun registerPersonalPortal(request: RequestRegister): String {
        return api.registerPersonalPortal(request).response
    }


    override suspend fun getUserInfo(token: String): User {
        return api.getUserInfo(token).response
    }

    override suspend fun forgotPassword(url: String, email: String): String {
        return api.forgotPassword(RequestPassword(url, email)).response
    }

    override suspend fun registerDevice(token: String, deviceToken: String) {
        api.registerDevice(token, RequestDeviceToken(deviceToken))
    }

    override suspend fun registerDevice(portalUrl: String, token: String, deviceToken: String) {
        api.registerDevice(portalUrl, token, RequestDeviceToken(deviceToken))
    }

    override suspend fun subscribe(
        portal: CloudPortal,
        token: String,
        deviceToken: String,
        isSubscribe: Boolean
    ) {
        val url = StringBuilder()
            .append(portal.scheme.value)
            .append(portal.url)
            .append("/api/$API_VERSION/settings/push/docsubscribe")
            .toString()

        api.subscribe(url, token, RequestPushSubscribe(deviceToken, isSubscribe))
    }

    override suspend fun getPortalSettings(cloudPortal: CloudPortal, accessToken: String): CloudPortal {
        val settings = getSettings(accessToken)
        val allSettings = getAllSettings(accessToken)
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
}