package app.editors.manager.googledrive.googledrive.api

import app.documents.core.network.ApiContract
import app.editors.manager.googledrive.mvp.models.resonse.GoogleDriveExplorerResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.QueryMap

interface GoogleDriveService {

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("drive/v3/files")
    fun getFiles(@QueryMap map: Map<String,String>): Single<Response<GoogleDriveExplorerResponse>>

}