package app.editors.manager.onedrive.onedrive.authorization

import app.documents.core.network.ApiContract
import app.editors.manager.onedrive.mvp.models.response.AuthResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface OneDriveAuthService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @FormUrlEncoded
    @POST("common/oauth2/v2.0/token")
    fun getToken(@FieldMap request: Map<String, String>): Single<Response<AuthResponse>>

}