package app.documents.core.network.storages.dropbox.api

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.utils.DropboxUtils
import app.documents.core.network.storages.dropbox.models.explorer.DropboxItem
import app.documents.core.network.storages.dropbox.models.request.*
import app.documents.core.network.storages.dropbox.models.response.*
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DropboxContentService {
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${DropboxService.API_VERSION}files/download")
    fun download(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE
    )
    @POST("${DropboxService.API_VERSION}files/download_zip")
    fun downloadFolder(@Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String): Single<Response<ResponseBody>>

    @Multipart
    @Headers("Content-Type:application/octet-stream")
    @POST("${DropboxService.API_VERSION}files/upload")
    fun upload(
        @Header(DropboxUtils.DROPBOX_API_ARG_HEADER) request: String,
        @Part part: MultipartBody.Part
    ): Single<Response<DropboxItem>>
}