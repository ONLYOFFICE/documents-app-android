package app.documents.core.network.login

import app.documents.core.network.common.contracts.ApiContract
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface LoginService {

    @Headers(
        ApiContract.HEADER_CACHE + ":" + ApiContract.VALUE_CACHE,
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication" + ApiContract.RESPONSE_FORMAT)
    fun signIn(@Body body: app.documents.core.network.login.models.request.RequestSignIn): Single<Response<app.documents.core.network.login.models.response.ResponseSignIn>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/capabilities" + ApiContract.RESPONSE_FORMAT)
    fun capabilities(): Single<Response<app.documents.core.network.login.models.response.ResponseCapabilities>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/version/build" + ApiContract.RESPONSE_FORMAT)
    fun getSettings(): Single<Response<app.documents.core.network.login.models.response.ResponseSettings>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings" + ApiContract.RESPONSE_FORMAT)
    fun getAllSettings(): Single<Response<app.documents.core.network.login.models.response.ResponseAllSettings>>

    /*
     * Auth with SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/{sms_code}" + ApiContract.RESPONSE_FORMAT)
    fun smsSignIn(
        @Body body: app.documents.core.network.login.models.request.RequestSignIn,
        @Path(value = "sms_code") smsCode: String
    ): Single<Response<app.documents.core.network.login.models.response.ResponseSignIn>>

    /*
     * Resend SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/sendsms" + ApiContract.RESPONSE_FORMAT)
    fun sendSms(@Body body: app.documents.core.network.login.models.request.RequestSignIn): Single<Response<app.documents.core.network.login.models.response.ResponseSignIn>>

    /*
     * Change number
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/setphone" + ApiContract.RESPONSE_FORMAT)
    fun changeNumber(@Body body: app.documents.core.network.login.models.request.RequestNumber): Single<Response<app.documents.core.network.login.models.response.ResponseSignIn>>

    /*
     * Validate portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("/api/portal/validateportalname")
    fun validatePortal(@Body body: app.documents.core.network.login.models.request.RequestValidatePortal): Single<Response<app.documents.core.network.login.models.response.ResponseValidatePortal>>

    /*
     * Register portal
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT,
    )
    @FormUrlEncoded
    @POST("/api/portal/register")
    fun registerPortal(
        @Field("recaptchaType") recaptchaType: Int = 1,
        @Field("recaptchaResponse") recaptchaResponse: String = "",
        @Field("firstName") firstName: String = "",
        @Field("email") email: String = "",
        @Field("lastName") lastName: String = "",
        @Field("portalName") portalName: String = "",
        @Field("password") password: String = ""
    ): Single<Response<app.documents.core.network.login.models.response.ResponseRegisterPortal>>

    /*
     * Register personal portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("api/" + ApiContract.API_VERSION + "/authentication/register" + ApiContract.RESPONSE_FORMAT)
    fun registerPersonalPortal(@Body body: app.documents.core.network.login.models.request.RequestRegister): Single<Response<app.documents.core.network.login.models.response.ResponseRegisterPersonalPortal>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" + ApiContract.RESPONSE_FORMAT)
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Single<Response<app.documents.core.network.login.models.response.ResponseUser>>

    /*
     * Password recovery
     * */

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/people/password" + ApiContract.RESPONSE_FORMAT)
    fun forgotPassword(@Body body: app.documents.core.network.login.models.request.RequestPassword?): Single<Response<app.documents.core.network.login.models.response.ResponsePassword>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/settings/push/docregisterdevice" + ApiContract.RESPONSE_FORMAT)
    fun registerDevice(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: app.documents.core.network.login.models.RequestDeviceToken,
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/settings/push/docsubscribe" + ApiContract.RESPONSE_FORMAT)
    fun subscribe(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: app.documents.core.network.login.models.RequestPushSubscribe
    ): Single<Response<ResponseBody>>

}