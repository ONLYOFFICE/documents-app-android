package app.documents.core.network.manager

import app.documents.core.model.login.Settings
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.FormRole
import app.documents.core.network.manager.models.request.RequestBatchBase
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDeleteRecent
import app.documents.core.network.manager.models.request.RequestDeleteShare
import app.documents.core.network.manager.models.request.RequestDownload
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.manager.models.request.RequestRenameFile
import app.documents.core.network.manager.models.request.RequestStartEdit
import app.documents.core.network.manager.models.request.RequestStopFilling
import app.documents.core.network.manager.models.request.RequestStorage
import app.documents.core.network.manager.models.request.RequestTitle
import app.documents.core.network.manager.models.response.ResponseCloudTree
import app.documents.core.network.manager.models.response.ResponseConversionStatus
import app.documents.core.network.manager.models.response.ResponseCount
import app.documents.core.network.manager.models.response.ResponseCreateFile
import app.documents.core.network.manager.models.response.ResponseCreateFolder
import app.documents.core.network.manager.models.response.ResponseDownload
import app.documents.core.network.manager.models.response.ResponseExplorer
import app.documents.core.network.manager.models.response.ResponseExternal
import app.documents.core.network.manager.models.response.ResponseFile
import app.documents.core.network.manager.models.response.ResponseFiles
import app.documents.core.network.manager.models.response.ResponseFillResult
import app.documents.core.network.manager.models.response.ResponseFolder
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.manager.models.response.ResponsePortal
import app.documents.core.network.manager.models.response.ResponseThirdparty
import app.documents.core.network.manager.models.response.ResponseVersionHistory
import app.documents.core.network.room.models.DeleteVersionRequest
import app.documents.core.network.room.models.EditCommentRequest
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ManagerService {

    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty/capabilities")
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun thirdpartyCapabilities(): Single<ResponseBody>

    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty")
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    suspend fun getThirdPartyList(): ResponseThirdparty

    /*
     * Counts of users
     * */
    @GET("api/" + ApiContract.API_VERSION + "/portal/userscount")
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun countUsers(): Call<ResponseCount>

    /*
     * Get folder/files by id
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/{item_id}")
    fun getItemById(
        @Path(value = "item_id") folderId: String,
        @QueryMap options: Map<String, String>?
    ): Observable<Response<ResponseExplorer>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/{item_id}")
    suspend fun getExplorer(
        @Path(value = "item_id") folderId: String,
        @QueryMap options: Map<String, String>?
    ): app.documents.core.network.BaseResponse<Explorer>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/recent")
    fun getRecentViaLink(
        @QueryMap options: Map<String, String>?
    ): Single<app.documents.core.network.BaseResponse<Explorer>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(method = "DELETE",  path = "api/" + ApiContract.API_VERSION + "/files/recent", hasBody = true)
    fun deleteRecent(@Body request: RequestDeleteRecent): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/@search/{query}")
    fun search(@Path(value = "query") query: String): Observable<Response<ResponseBody>>

    /*
     * Create docs file
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/file")
    fun createDocs(
        @Path(value = "folder_id") folderId: String,
        @Body body: RequestCreate
    ): Observable<Response<ResponseCreateFile>>

    /*
     * Get file info
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}")
    fun getFileInfo(
        @Path(value = "file_id") fileId: String?,
        @Query(value = "version") version: Int? = null
    ): Observable<Response<ResponseFile>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}")
    suspend fun suspendGetFileInfo(
        @Path(value = "file_id") fileId: String?,
        @Query(value = "version") version: Int? = null
    ): Response<ResponseFile>


    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}")
    suspend fun getCloudFileInfo(@Path(value = "file_id") fileId: String?): app.documents.core.network.BaseResponse<CloudFile>

    /*
     * Create folder
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}")
    fun createFolder(
        @Path(value = "folder_id") folderId: String,
        @Body body: RequestCreate
    ): Observable<Response<ResponseCreateFolder>>

    /*
     * Operation batch
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/delete")
    fun deleteBatch(@Body body: RequestBatchBase): Observable<Response<ResponseOperation>>

    /*
     * Move items
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/move")
    fun move(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Copy items
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/copy")
    fun copy(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Terminate all operations
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/terminate")
    fun terminate(): Call<ResponseOperation>

    /*
     * Status operations
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops")
    fun status(): Single<ResponseOperation>

    /*
     * Rename folder
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}")
    fun renameFolder(
        @Path(value = "folder_id") folderId: String,
        @Body body: RequestTitle
    ): Observable<Response<ResponseFolder>>

    /*
     * Rename file
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}")
    fun renameFile(
        @Path(value = "file_id") folderId: String,
        @Body body: RequestRenameFile
    ): Observable<Response<ResponseFile>>

    /*
     * Get external link
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/{file_id}/sharedlink")
    fun getExternalLink(
        @Path(value = "file_id") fileId: String,
        @Body body: RequestExternal
    ): Observable<Response<ResponseExternal>>

    /*
     * Delete share setting
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/share",
        hasBody = true
    )
    fun deleteShare(@Body body: RequestDeleteShare): Call<BaseResponse>

    /*
     * Get portal
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/portal")
    fun getPortal(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Call<ResponsePortal>

    /*
     * Empty trash
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/emptytrash")
    fun emptyTrash(): Observable<Response<ResponseOperation>>

    /*
     * Connect storage
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/thirdparty")
    fun connectStorage(@Body body: RequestStorage): Observable<ResponseFolder>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/thirdparty/{folder_id}")
    fun deleteStorage(@Path(value = "folder_id") id: String): Observable<Response<ResponseBody>>

    /*
     * Download file
     * */
    @Streaming
    @GET
    fun downloadFile(
        @Url url: String,
        @Header("Cookie") cookie: String
    ): Single<Response<ResponseBody>>

    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/presigneduri")
    suspend fun getDownloadFileLink(
        @Path(value = "file_id") fileId: String
    ): app.documents.core.network.BaseResponse<String>

    @Streaming
    @GET
    suspend fun suspendDownloadFile(
        @Url url: String,
        @Header("Cookie") cookie: String
    ): Response<ResponseBody>

    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/bulkdownload")
    fun downloadFiles(@Body requestDownload: RequestDownload): Single<ResponseDownload>

    /*
     * Upload  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload")
    fun uploadFile(
        @Path(value = "folder_id") folderId: String,
        @Part part: MultipartBody.Part
    ): Call<ResponseFile>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload")
    fun uploadMultiFiles(
        @Path(value = "folder_id") folderId: String,
        @Part part: Array<MultipartBody.Part>
    ): Single<Response<ResponseBody>>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/@my/upload")
    fun uploadFileToMy(@Part part: MultipartBody.Part): Call<ResponseFile>

    /*
     * Insert  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/insert")
    fun insertFile(
        @Header(ApiContract.HEADER_AUTHORIZATION) token: String,
        @Path(value = "folder_id") folderId: String,
        @Part("title") title: String,
        @Part part: MultipartBody.Part
    ): Call<ResponseFile>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops/move")
    fun checkFiles(
        @Query("destFolderId") destFolderId: String?,
        @Query("folderIds") folderIds: List<String>?,
        @Query("fileIds") fileIds: List<String>?
    ): Single<ResponseFiles>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/favorites")
    fun addToFavorites(@Body body: RequestFavorites): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/favorites",
        hasBody = true
    )
    fun deleteFromFavorites(@Body body: RequestFavorites): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/@root")
    suspend fun getRootFolder(
        @QueryMap filterMap: Map<String, Int>,
        @QueryMap flagMap: Map<String, Boolean>
    ): ResponseCloudTree

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/openedit")
    fun openFile(
        @Path(value = "id") id: String,
        @Query("version") version: Int,
        @Query("fill") fill: Boolean? = null,
        @Query("edit") edit: Boolean? = null,
        @Query("view") view: Boolean? = null
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/openedit")
    suspend fun suspendOpenFile(
        @Path(value = "id") id: String,
        @Query("version") version: Int,
        @Query("fill") fill: Boolean? = null,
        @Query("edit") edit: Boolean? = null,
        @Query("view") view: Boolean? = null
    ): Response<ResponseBody>

    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/openedit")
    fun openFile(
        @Path(value = "id") id: String,
    ): Single<Response<ResponseBody>>

    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/openedit")
    suspend fun suspendOpenFile(
        @Path(value = "id") id: String,
    ): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/docservice")
    fun getDocService(): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/docservice")
    suspend fun suspendGetDocService(): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/checkconversion")
    fun startConversion(@Path(value = "fileId") id: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/checkconversion")
    suspend fun getConversionStatus(
        @Path(value = "fileId") id: String,
        @Query(value = "start") start: Boolean
    ): ResponseConversionStatus

    @Multipart
    @PUT("api/" + ApiContract.API_VERSION + "/files/{fileId}/update")
    fun updateDocument(
        @Path(value = "fileId") id: String,
        @Part part: MultipartBody.Part
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/${ApiContract.API_VERSION}/files/file/fillresult")
    suspend fun getFillResult(@Query(value = "fillingSessionId") fillingSessionId: String): ResponseFillResult

    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/${ApiContract.API_VERSION}/settings/version/build")
    suspend fun getSettings(): app.documents.core.network.BaseResponse<Settings>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/history")
    suspend fun getVersionHistory(
        @Path(value = "file_id") fileId: String
    ): Response<ResponseVersionHistory>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/restoreversion")
    suspend fun restoreVersion(
        @Path(value = "file_id") fileId: String,
        @Query(value = "version") version: Int
    ): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/comment")
    suspend fun updateVersionComment(
        @Path(value = "file_id") fileId: String,
        @Body body: EditCommentRequest
    ): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/deleteversion")
    suspend fun deleteVersion(
        @Body body: DeleteVersionRequest
    ): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/formroles")
    suspend fun getFillingStatus(
        @Path(value = "file_id") fileId: String,
    ): app.documents.core.network.BaseResponse<List<FormRole>>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}/manageformfilling")
    suspend fun stopFilling(
        @Path(value = "file_id") fileId: String,
        @Body body: RequestStopFilling
    ): Response<ResponseBody>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/startedit")
    suspend fun startEdit(
        @Path(value = "fileId") fileId: String,
        @Body body: RequestStartEdit = RequestStartEdit()
    ): app.documents.core.network.BaseResponse<String>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/trackeditfile")
    suspend fun trackEdit(
        @Path(value = "fileId") fileId: String,
        @Query(value = "docKeyForTrack") docKey: String,
        @Query(value = "isFinish") isFinish: Boolean = false,
    ): Response<BaseResponse>

    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @Multipart
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/saveediting")
    fun saveEditing(
        @Path(value = "fileId") id: String,
        @Part file: MultipartBody.Part
    ): Single<Response<ResponseBody>>

}