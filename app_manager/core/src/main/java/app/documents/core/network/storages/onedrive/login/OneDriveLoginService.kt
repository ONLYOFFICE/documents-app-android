package app.documents.core.network.storages.onedrive.login

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.storages.onedrive.api.OneDriveService
import app.documents.core.network.storages.onedrive.models.response.AuthResponse
import app.documents.core.network.storages.onedrive.models.user.User
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface OneDriveLoginService {

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + "application/x-www-form-urlencoded",
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @FormUrlEncoded
    @POST("common/oauth2/v2.0/token")
    fun getToken(@FieldMap request: Map<String, String>): Single<Response<AuthResponse>>
}