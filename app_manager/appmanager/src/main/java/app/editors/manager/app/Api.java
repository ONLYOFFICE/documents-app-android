package app.editors.manager.app;

import java.util.List;
import java.util.Map;

import app.documents.core.network.ApiContract;
import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.request.RequestBatchBase;
import app.editors.manager.mvp.models.request.RequestBatchOperation;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestDeleteShare;
import app.editors.manager.mvp.models.request.RequestDownload;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestFavorites;
import app.editors.manager.mvp.models.request.RequestRenameFile;
import app.editors.manager.mvp.models.request.RequestStorage;
import app.editors.manager.mvp.models.request.RequestTitle;
import app.editors.manager.mvp.models.request.RequestUser;
import app.editors.manager.mvp.models.response.ResponseCount;
import app.editors.manager.mvp.models.response.ResponseCreateFile;
import app.editors.manager.mvp.models.response.ResponseCreateFolder;
import app.editors.manager.mvp.models.response.ResponseDownload;
import app.editors.manager.mvp.models.response.ResponseExplorer;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseFile;
import app.editors.manager.mvp.models.response.ResponseFiles;
import app.editors.manager.mvp.models.response.ResponseFolder;
import app.editors.manager.mvp.models.response.ResponseModules;
import app.editors.manager.mvp.models.response.ResponseOperation;
import app.editors.manager.mvp.models.response.ResponsePortal;
import app.editors.manager.mvp.models.response.ResponseThirdparty;
import app.editors.manager.mvp.models.response.ResponseUser;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface Api {

    final class Parameters {
        public static final String ARG_ACTION = "action";
        public static final String ARG_COUNT = "count";
        public static final String ARG_START_INDEX = "startIndex";
        public static final String ARG_SORT_BY = "sortBy";
        public static final String ARG_SORT_ORDER = "sortOrder";
        public static final String ARG_FILTER_BY = "filterBy";
        public static final String ARG_FILTER_OP = "filterOp";
        public static final String ARG_FILTER_VALUE = "filterValue";
        public static final String ARG_UPDATED_SINCE = "updatedSince";

        public static final String VAL_ACTION_VIEW = "view";
        public static final String VAL_SORT_ORDER_ASC = "ascending";
        public static final String VAL_SORT_ORDER_DESC = "descending";
        public static final String VAL_FILTER_OP_CONTAINS = "contains";
        public static final String VAL_FILTER_OP_EQUALS = "equals";
        public static final String VAL_FILTER_OP_STARTS_WITH = "startsWith";
        public static final String VAL_FILTER_OP_PRESENT = "present";
        public static final String VAL_FILTER_BY = "title";
        public static final String VAL_SORT_BY_UPDATED = "DateAndTime";
        public static final String VAL_SORT_BY_CREATED = "created";
        public static final String VAL_SORT_BY_TITLE = "title";
        public static final String VAL_SORT_BY_TYPE = "type";
        public static final String VAL_SORT_BY_SIZE = "size";
        public static final String VAL_SORT_BY_OWNER = "Author";
    }

    final class Extension {
        public static final String DOCX = "DOCX";
        public static final String XLSX = "XLSX";
        public static final String PPTX = "PPTX";
    }

    final class SectionType {
        public static final int UNKNOWN = 0;
        public static final int CLOUD_COMMON = 1;
        public static final int CLOUD_BUNCH = 2;
        public static final int CLOUD_TRASH = 3;
        public static final int CLOUD_USER = 5;
        public static final int CLOUD_SHARE = 6;
        public static final int CLOUD_PROJECTS = 8;
        public static final int DEVICE_DOCUMENTS = 9;
    }

    final class Storage {
        public static final String BOXNET = "Box";
        public static final String DROPBOX = "DropboxV2";
        public static final String GOOGLEDRIVE = "GoogleDrive";
        public static final String ONEDRIVE = "OneDrive";
        public static final String SKYDRIVE = "SkyDrive";
        public static final String GOOGLE = "Google";
        public static final String SHAREPOINT = "SharePoint";
        public static final String YANDEX = "Yandex";
        public static final String OWNCLOUD = "OwnCloud";
        public static final String NEXTCLOUD = "Nextcloud";
        public static final String WEBDAV = "WebDav";
    }

    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty/capabilities" + ApiContract.RESPONSE_FORMAT)
    Single<ResponseBody> getThirdpartyCapabilities();

    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/thirdparty" + ApiContract.RESPONSE_FORMAT)
    Observable<ResponseThirdparty> getThirdPartyList();

    /*
     * Counts of users
     * */
    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/portal/userscount" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseCount> getCountUsers();

    /*
     * Users info
     * */
    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/people/@self" + ApiContract.RESPONSE_FORMAT)
    Observable<ResponseUser> getUserInfo();

    /*
     * Get folder/files by id
     * */
    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/{item_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseExplorer>> getItemById(@Path(value = "item_id") String folderId,
                                                       @QueryMap Map<String, String> options);

    /*
     * Create docs file
     * */
    @Headers({ApiContract.HEADER_CONTENT_TYPE + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/file" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseCreateFile>> createDocs(@Path(value = "folder_id") String folderId,
                                                        @Body RequestCreate body);

    /*
     * Get file info
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/file/{file_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseFile>> getFileInfo(@Path(value = "file_id") String fileId);

    /*
     * Create folder
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @POST("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseCreateFolder>> createFolder(@Path(value = "folder_id") String folderId,
                                                            @Body RequestCreate body);

    /*
     * Operation batch
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/delete" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseOperation> deleteBatch(@Body RequestBatchBase body);

    /*
     * Move items
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/move" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseOperation>> move(@Body RequestBatchOperation body);

    /*
     * Copy items
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/copy" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseOperation>> copy(@Body RequestBatchOperation body);

    /*
     * Terminate all operations
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/terminate" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseOperation> terminate();

    /*
     * Status operations
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops" + ApiContract.RESPONSE_FORMAT)
    Single<ResponseOperation> status();

    /*
     * Rename folder
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/folder/{folder_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseFolder>> renameFolder(@Path(value = "folder_id") String folderId,
                                                      @Body RequestTitle body);

    /*
     * Rename file
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/file/{file_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseFile>> renameFile(@Path(value = "file_id") String folderId,
                                                  @Body RequestRenameFile body);

    /*
     * Get external link
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/{file_id}/sharedlink" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseExternal>> getExternalLink(@Path(value = "file_id") String fileId,
                                                           @Body RequestExternal body);

    /*
     * Delete share setting
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @HTTP(method = "DELETE", path = "api/" + ApiContract.API_VERSION + "/files/share" + ApiContract.RESPONSE_FORMAT, hasBody = true)
    Call<Base> deleteShare(@Body RequestDeleteShare body);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/people/{user_id}" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseUser> updateUser(@Path(value = "user_id") String userId,
                                  @Body RequestUser body);

    /*
     * Get portal
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/portal" + ApiContract.RESPONSE_FORMAT)
    Call<ResponsePortal> getPortal(@Header(ApiContract.HEADER_AUTHORIZATION) String token);

    /*
     * Empty trash
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/emptytrash" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseOperation>> emptyTrash();

    /*
     * Connect storage
     * */
    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @POST("api/" + ApiContract.API_VERSION + "/files/thirdparty" + ApiContract.RESPONSE_FORMAT)
    Observable<ResponseFolder> connectStorage(@Body RequestStorage body);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @DELETE("api/" + ApiContract.API_VERSION + "/files/thirdparty/{folder_id}" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<ResponseBody>> deleteStorage(@Path(value = "folder_id") String id);

    /*
     * Download file
     * */
    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Url String url, @Header("Cookie") String cookie);

    @PUT("api/" + ApiContract.API_VERSION + "/files/fileops/bulkdownload" + ApiContract.RESPONSE_FORMAT)
    Single<ResponseDownload> downloadFiles(@Body RequestDownload requestDownload);

    /*
     * Upload  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseFile> uploadFile(@Path(value = "folder_id") String folderId,
                                  @Part MultipartBody.Part part);

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/upload" + ApiContract.RESPONSE_FORMAT)
    Single<Response<ResponseBody>> uploadMultiFiles(@Path(value = "folder_id") String folderId,
                                                    @Part MultipartBody.Part[] part);

    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/@my/upload" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseFile> uploadFileToMy(@Part MultipartBody.Part part);

    /*
     * Insert  file
     * */
    @Multipart
    @POST("api/" + ApiContract.API_VERSION + "/files/{folder_id}/insert" + ApiContract.RESPONSE_FORMAT)
    Call<ResponseFile> insertFile(@Header(ApiContract.HEADER_AUTHORIZATION) String token,
                                  @Path(value = "folder_id") String folderId,
                                  @Part("title") String title,
                                  @Part MultipartBody.Part part);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/files/fileops/move" + ApiContract.RESPONSE_FORMAT)
    Single<ResponseFiles> checkFiles(@Query("destFolderId") String destFolderId,
                                     @Query("folderIds") List<String> folderIds,
                                     @Query("fileIds") List<String> fileIds);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @GET("api/" + ApiContract.API_VERSION + "/settings/security" + ApiContract.RESPONSE_FORMAT)
    Single<ResponseModules> getModules(@Query("ids") List<String> modulesIds);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @POST("api/" + ApiContract.API_VERSION + "/files/favorites" + ApiContract.RESPONSE_FORMAT)
    Observable<Response<Base>> addToFavorites(@Body RequestFavorites body);

    @Headers({ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_CONTENT_TYPE,
            ApiContract.HEADER_ACCEPT + ": " + ApiContract.VALUE_ACCEPT})
    @HTTP(method = "DELETE", path = "api/" + ApiContract.API_VERSION + "/files/favorites" + ApiContract.RESPONSE_FORMAT, hasBody = true)
    Observable<Response<Base>> deleteFromFavorites(@Body RequestFavorites body);

}
