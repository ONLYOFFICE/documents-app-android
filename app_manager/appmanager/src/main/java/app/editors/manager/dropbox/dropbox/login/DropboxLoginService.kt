package app.editors.manager.dropbox.dropbox.login

import app.documents.core.network.ApiContract
import app.editors.manager.dropbox.mvp.models.AccountRequest
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DropboxLoginService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("${API_VERSION}users/get_account")
    fun getUserInfo(@Header(ApiContract.HEADER_AUTHORIZATION) token: String, @QueryMap map: Map<String, String>): Single<Response<ResponseBody>>

}