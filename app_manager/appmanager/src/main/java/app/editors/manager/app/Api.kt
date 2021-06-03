package app.editors.manager.app

import app.documents.core.network.ApiContract
import app.editors.manager.mvp.models.base.Base
import app.editors.manager.mvp.models.request.*
import app.editors.manager.mvp.models.response.*
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface Api {

    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty/capabilities" + ApiContract.RESPONSE_FORMAT)
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun thirdpartyCapabilities(): Single<ResponseBody>

    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty" + ApiContract.RESPONSE_FORMAT)
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun thirdPartyList(): Observable<ResponseThirdparty>

    /*
     * Counts of users
     * */
    @GET("api/" + ApiContract.API_VERSION + "/portal/userscount" + ApiContract.RESPONSE_FORMAT)
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun countUsers(): Call<ResponseCount>

    /*
     * Users info
     * */
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" + ApiContract.RESPONSE_FORMAT)
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    fun userInfo(): Observable<ResponseUser>

    /*
     * Get folder/files by id
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/{item_id}" + ApiContract.RESPONSE_FORMAT)
    fun getItemById(
        @Path(value = "item_id") folderId: String,
        @QueryMap options: Map<String, String>?
    ): Observable<Response<ResponseExplorer>>

    /*
     * Create docs file
     * */
    @Headers(
        ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/file" + ApiContract.RESPONSE_FORMAT)
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
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}" + ApiContract.RESPONSE_FORMAT)
    fun getFileInfo(@Path(value = "file_id") fileId: String): Observable<Response<ResponseFile>>

    /*
     * Create folder
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}" + ApiContract.RESPONSE_FORMAT)
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
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/delete" + ApiContract.RESPONSE_FORMAT)
    fun deleteBatch(@Body body: RequestBatchBase): Call<ResponseOperation>

    /*
     * Move items
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/move" + ApiContract.RESPONSE_FORMAT)
    fun move(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Copy items
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/copy" + ApiContract.RESPONSE_FORMAT)
    fun copy(@Body body: RequestBatchOperation): Observable<Response<ResponseOperation>>

    /*
     * Terminate all operations
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/terminate" + ApiContract.RESPONSE_FORMAT)
    fun terminate(): Call<ResponseOperation>

    /*
     * Status operations
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops" + ApiContract.RESPONSE_FORMAT)
    fun status(): Single<ResponseOperation>

    /*
     * Rename folder
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}" + ApiContract.RESPONSE_FORMAT)
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
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}" + ApiContract.RESPONSE_FORMAT)
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
    @PUT("api/" + ApiContract.API_VERSION + "/files/{file_id}/sharedlink" + ApiContract.RESPONSE_FORMAT)
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
        path = "api/" + ApiContract.API_VERSION + "/files/share" + ApiContract.RESPONSE_FORMAT,
        hasBody = true
    )
    fun deleteShare(@Body body: RequestDeleteShare): Call<Base>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/people/{user_id}" + ApiContract.RESPONSE_FORMAT)
    fun updateUser(
        @Path(value = "user_id") userId: String,
        @Body body: RequestUser
    ): Call<ResponseUser>

    /*
     * Get portal
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/portal" + ApiContract.RESPONSE_FORMAT)
    fun getPortal(@Header(ApiContract.HEADER_AUTHORIZATION) token: String): Call<ResponsePortal>

    /*
     * Empty trash
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/emptytrash" + ApiContract.RESPONSE_FORMAT)
    fun emptyTrash(): Observable<Response<ResponseOperation>>

    /*
     * Connect storage
     * */
    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/thirdparty" + ApiContract.RESPONSE_FORMAT)
    fun connectStorage(@Body body: RequestStorage): Observable<ResponseFolder>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @DELETE("api/" + ApiContract.API_VERSION + "/files/thirdparty/{folder_id}" + ApiContract.RESPONSE_FORMAT)
    fun deleteStorage(@Path(value = "folder_id") id: String): Observable<Response<ResponseBody>>

    /*
     * Download file
     * */
    @Streaming
    @GET
    fun downloadFile(@Url url: String, @Header("Cookie") cookie: String): Call<ResponseBody>

    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/bulkdownload" + ApiContract.RESPONSE_FORMAT)
    fun downloadFiles(@Body requestDownload: RequestDownload): Single<ResponseDownload>

    /*
     * Upload  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload" + ApiContract.RESPONSE_FORMAT)
    fun uploadFile(
        @Path(value = "folder_id") folderId: String,
        @Part part: MultipartBody.Part
    ): Call<ResponseFile>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload" + ApiContract.RESPONSE_FORMAT)
    fun uploadMultiFiles(
        @Path(value = "folder_id") folderId: String,
        @Part part: Array<MultipartBody.Part>
    ): Single<Response<ResponseBody>>

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/@my/upload" + ApiContract.RESPONSE_FORMAT)
    fun uploadFileToMy(@Part part: MultipartBody.Part): Call<ResponseFile>

    /*
     * Insert  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/insert" + ApiContract.RESPONSE_FORMAT)
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
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops/move" + ApiContract.RESPONSE_FORMAT)
    fun checkFiles(
        @Query("destFolderId") destFolderId: String,
        @Query("folderIds") folderIds: List<String>,
        @Query("fileIds") fileIds: List<String>
    ): Single<ResponseFiles>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @GET("api/" + ApiContract.API_VERSION + "/settings/security" + ApiContract.RESPONSE_FORMAT)
    fun getModules(@Query("ids") modulesIds: List<String>): Single<ResponseModules>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @POST("api/" + ApiContract.API_VERSION + "/files/favorites" + ApiContract.RESPONSE_FORMAT)
    fun addToFavorites(@Body body: RequestFavorites): Observable<Response<Base>>

    @Headers(
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
        ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT
    )
    @HTTP(
        method = "DELETE",
        path = "api/" + ApiContract.API_VERSION + "/files/favorites" + ApiContract.RESPONSE_FORMAT,
        hasBody = true
    )
    fun deleteFromFavorites(@Body body: RequestFavorites): Observable<Response<Base>>
}