package app.documents.core.network.storages.googledrive.login

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.dropbox.models.response.TokenResponse
import io.reactivex.Single
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface GoogleDriveLoginService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @FormUrlEncoded
    @POST("/token")
    fun getToken(@FieldMap map: Map<String, String>): Single<TokenResponse>
}
