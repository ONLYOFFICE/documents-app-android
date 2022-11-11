package app.documents.core.login

import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.RequestDeviceToken
import app.documents.core.network.models.login.RequestPushSubscribe
import app.documents.core.network.models.login.request.*
import app.documents.core.network.models.login.response.*
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
    fun signIn(@Body body: RequestSignIn): Single<Response<ResponseSignIn>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/capabilities" + ApiContract.RESPONSE_FORMAT)
    fun capabilities(): Single<Response<ResponseCapabilities>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/version/build" + ApiContract.RESPONSE_FORMAT)
    fun getSettings(): Single<Response<ResponseSettings>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings" + ApiContract.RESPONSE_FORMAT)
    fun getAllSettings(): Single<Response<ResponseAllSettings>>

    /*
     * Auth with SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/{sms_code}" + ApiContract.RESPONSE_FORMAT)
    fun smsSignIn(
        @Body body: RequestSignIn,
        @Path(value = "sms_code") smsCode: String
    ): Single<Response<ResponseSignIn>>

    /*
     * Resend SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/sendsms" + ApiContract.RESPONSE_FORMAT)
    fun sendSms(@Body body: RequestSignIn): Single<Response<ResponseSignIn>>

    /*
     * Change number
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/setphone" + ApiContract.RESPONSE_FORMAT)
    fun changeNumber(@Body body: RequestNumber): Single<Response<ResponseSignIn>>

    /*
     * Validate portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("/api/portal/validateportalname")
    fun validatePortal(@Body body: RequestValidatePortal): Single<Response<ResponseValidatePortal>>

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
    ): Single<Response<ResponseRegisterPortal>>

    /*
     * Register personal portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("api/" + ApiContract.API_VERSION + "/authentication/register" + ApiContract.RESPONSE_FORMAT)
    fun registerPersonalPortal(@Body body: RequestRegister): Single<Response<ResponseRegisterPersonalPortal>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" + ApiContract.RESPONSE_FORMAT)
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Single<Response<ResponseUser>>

    /*
     * Password recovery
     * */

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/people/password" + ApiContract.RESPONSE_FORMAT)
    fun forgotPassword(@Body body: RequestPassword?): Single<Response<ResponsePassword>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/settings/push/docregisterdevice" + ApiContract.RESPONSE_FORMAT)
    fun registerDevice(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: RequestDeviceToken,
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/settings/push/docsubscribe" + ApiContract.RESPONSE_FORMAT)
    fun subscribe(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: RequestPushSubscribe
    ): Single<Response<ResponseBody>>

}