package app.documents.core.login

import app.documents.core.network.ApiContract
import app.documents.core.network.models.login.request.RequestNumber
import app.documents.core.network.models.login.request.RequestRegister
import app.documents.core.network.models.login.request.RequestSignIn
import app.documents.core.network.models.login.request.RequestValidatePortal
import app.documents.core.network.models.login.response.*
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface LoginService {

    @Headers(
        ApiContract.HEADER_CACHE + ":" + ApiContract.VALUE_CACHE,
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication" + ApiContract.RESPONSE_FORMAT)
    fun signIn(@Body body: RequestSignIn): Observable<Response<ResponseSignIn>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/capabilities" + ApiContract.RESPONSE_FORMAT)
    fun capabilities(): Observable<Response<ResponseCapabilities>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/version/build" + ApiContract.RESPONSE_FORMAT)
    fun getSettings(): Observable<Response<ResponseSettings>>

    /*
     * Auth with SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/{sms_code}" + ApiContract.RESPONSE_FORMAT)
    fun smsSignIn(
        @Body body: RequestSignIn,
        @Path(value = "sms_code") smsCode: String
    ): Observable<Response<ResponseSignIn>>

    /*
     * Resend SMS code
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/sendsms" + ApiContract.RESPONSE_FORMAT)
    fun sendSms(@Body body: RequestSignIn): Observable<Response<ResponseSignIn>>

    /*
     * Change number
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/authentication/setphone" + ApiContract.RESPONSE_FORMAT)
    fun changeNumber(@Body body: RequestNumber): Observable<Response<ResponseSignIn>>

    /*
     * Validate portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("/api/portal/validateportalname")
    fun validatePortal(@Body body: RequestValidatePortal): Observable<Response<ResponseValidatePortal>>

    /*
     * Register portal
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("/api/portal/register")
    fun registerPortal(@Body body: RequestRegister): Observable<Response<ResponseRegisterPortal>>

    /*
     * Register personal portal
     * */
    @Headers(ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE)
    @POST("api/" + ApiContract.API_VERSION + "/authentication/register" + ApiContract.RESPONSE_FORMAT)
    fun registerPersonalPortal(@Body body: RequestRegister): Observable<Response<ResponseRegisterPersonalPortal>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" + ApiContract.RESPONSE_FORMAT)
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Observable<Response<ResponseUser>>

}