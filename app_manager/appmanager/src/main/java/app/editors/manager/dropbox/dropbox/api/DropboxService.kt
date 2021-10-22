package app.editors.manager.dropbox.dropbox.api

import app.documents.core.network.ApiContract
import app.editors.manager.dropbox.mvp.models.request.DeleteRequest
import app.editors.manager.dropbox.mvp.models.request.ExplorerRequest
import app.editors.manager.dropbox.mvp.models.response.ExplorerResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DropboxService {

    companion object {
        const val API_VERSION = "2/"
    }

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/list_folder")
    fun getFiles(@Body request: ExplorerRequest): Single<Response<ExplorerResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/download")
    fun download(@Body request: ExplorerRequest): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${API_VERSION}files/delete_v2")
    fun delete(@Body request: DeleteRequest): Single<Response<ResponseBody>>
}