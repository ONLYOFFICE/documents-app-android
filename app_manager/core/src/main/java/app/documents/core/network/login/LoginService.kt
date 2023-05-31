package app.documents.core.network.login

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.login.models.RequestDeviceToken
import app.documents.core.network.login.models.RequestPushSubscribe
import app.documents.core.network.login.models.request.*
import app.documents.core.network.login.models.response.*
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
    @POST("api/" + ApiContract.API_VERSION + "/authentication")
    fun signIn(@Body body: RequestSignIn): Single<Response<ResponseSignIn>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/capabilities")
    fun capabilities(): Single<Response<ResponseCapabilities>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/version/build")
    fun getSettings(): Single<Response<ResponseSettings>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings")
    fun getAllSettings(): Single<Response<ResponseAllSettings>>

    /*
     * Auth with SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/{sms_code}")
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
    @POST("api/" + ApiContract.API_VERSION + "/authentication/sendsms")
    fun sendSms(@Body body: RequestSignIn): Single<Response<ResponseSignIn>>

    /*
     * Change number
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/setphone")
    fun changeNumber(@Body body: RequestNumber): Single<Response<ResponseSignIn>>

    /*
     * Validate portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("/apisystem/portal/validateportalname")
    fun validatePortal(@Body body: RequestValidatePortal): Single<Response<ResponseValidatePortal>>

    /*
     * Register portal
     * */

    @POST("/apisystem/portal/register")
    fun registerPortal(
        @Body body: RequestRegister
    ): Single<Response<ResponseRegisterPortal>>

    /*
     * Register personal portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("api/" + ApiContract.API_VERSION + "/authentication/register" )
    fun registerPersonalPortal(@Body body: RequestRegister): Single<Response<ResponseRegisterPersonalPortal>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" )
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Single<Response<ResponseUser>>

    /*
     * Password recovery
     * */

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/people/password" )
    fun forgotPassword(@Body body: RequestPassword?): Single<Response<ResponsePassword>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/settings/push/docregisterdevice" )
    fun registerDevice(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: RequestDeviceToken,
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/settings/push/docsubscribe" )
    fun subscribe(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Body body: RequestPushSubscribe
    ): Single<Response<ResponseBody>>

}