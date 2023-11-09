package app.documents.core.network.manager

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.common.models.BaseResponse
import app.documents.core.network.manager.models.request.RequestBatchBase
import app.documents.core.network.manager.models.request.RequestBatchOperation
import app.documents.core.network.manager.models.request.RequestCreate
import app.documents.core.network.manager.models.request.RequestDeleteShare
import app.documents.core.network.manager.models.request.RequestDownload
import app.documents.core.network.manager.models.request.RequestExternal
import app.documents.core.network.manager.models.request.RequestFavorites
import app.documents.core.network.manager.models.request.RequestRenameFile
import app.documents.core.network.manager.models.request.RequestStorage
import app.documents.core.network.manager.models.request.RequestTitle
import app.documents.core.network.manager.models.request.RequestUser
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
import app.documents.core.network.manager.models.response.ResponseFolder
import app.documents.core.network.manager.models.response.ResponseModules
import app.documents.core.network.manager.models.response.ResponseOperation
import app.documents.core.network.manager.models.response.ResponsePortal
import app.documents.core.network.manager.models.response.ResponseThirdparty
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
    fun thirdPartyList(): Observable<ResponseThirdparty>

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
     * Users info
     * */
    @GET("api/" + ApiContract.API_VERSION + "/people/@self")
    @Headers(
        ApiContract.HEADER_CONTENT_OPERATION_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun userInfo(): Observable<app.documents.core.network.login.models.response.ResponseUser>

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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}")
    fun getFileInfo(@Path(value = "file_id") fileId: String?): Observable<Response<ResponseFile>>

    /*
     * Create folder
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/delete")
    fun deleteBatch(@Body body: RequestBatchBase): Observable<Response<ResponseOperation>>

    /*
     * Move items
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/move")
    fun move(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Copy items
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/copy")
    fun copy(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Terminate all operations
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/terminate")
    fun terminate(): Call<ResponseOperation>

    /*
     * Status operations
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops")
    fun status(): Single<ResponseOperation>

    /*
     * Rename folder
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/share",
        hasBody = true
    )
    fun deleteShare(@Body body: RequestDeleteShare): Call<BaseResponse>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/people/{user_id}")
    fun updateUser(
        @Path(value = "user_id") userId: String,
        @Body body: RequestUser
    ): Call<app.documents.core.network.login.models.response.ResponseUser>

    /*
     * Get portal
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/portal")
    fun getPortal(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Call<ResponsePortal>

    /*
     * Empty trash
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/emptytrash")
    fun emptyTrash(): Observable<Response<ResponseOperation>>

    /*
     * Connect storage
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/thirdparty")
    fun connectStorage(@Body body: RequestStorage): Observable<ResponseFolder>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/thirdparty/{folder_id}")
    fun deleteStorage(@Path(value = "folder_id") id: String): Observable<Response<ResponseBody>>

    /*
     * Download file
     * */
    @Streaming
    @GET
    fun downloadFile(@Url url: String, @Header("Cookie") cookie: String): Single<Response<ResponseBody>>

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
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops/move")
    fun checkFiles(
        @Query("destFolderId") destFolderId: String?,
        @Query("folderIds") folderIds: List<String>?,
        @Query("fileIds") fileIds: List<String>?
    ): Single<ResponseFiles>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/security")
    fun getModules(@Query("ids") modulesIds: List<String>): Observable<ResponseModules>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/favorites")
    fun addToFavorites(@Body body: RequestFavorites): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/favorites",
        hasBody = true
    )
    fun deleteFromFavorites(@Body body: RequestFavorites): Observable<Response<BaseResponse>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/@root")
    fun getRootFolder(
        @QueryMap filterMap: Map<String, Int>,
        @QueryMap flagMap: Map<String, Boolean>
    ): Observable<ResponseCloudTree>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/settings/push/docregisterdevice")
    fun registerDevice(
        @Body body: app.documents.core.network.login.models.RequestDeviceToken,
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/settings/push/docsubscribe")
    fun subscribe(
        @Body body: app.documents.core.network.login.models.RequestPushSubscribe
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{id}/openedit")
    fun openFile(
        @Path(value = "id")id: String,
        @Query("version") version: Int
    ): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/docservice")
    fun getDocService(): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/checkconversion")
    fun startConversion(@Path(value = "fileId") id: String): Single<Response<ResponseBody>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{fileId}/checkconversion")
    suspend fun getConversionStatus(
        @Path(value = "fileId") id: String,
        @Query(value = "start") start: Boolean
    ): ResponseConversionStatus

}