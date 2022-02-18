package app.editors.manager.storages.onedrive.onedrive.login

import app.documents.core.network.ApiContract
import app.editors.manager.storages.onedrive.mvp.models.user.User
import app.editors.manager.storages.onedrive.onedrive.api.OneDriveService
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface OneDriveLoginService {

    companion object {
        const val API_VERSION = "beta/"
        const val ONEDRIVE_BASE_URL = "https://graph.microsoft.com/"
    }
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("${OneDriveService.API_VERSION}me/")
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Single<Response<User>>
}