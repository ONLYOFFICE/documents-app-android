package app.editors.manager.app;

import java.util.List;
import java.util.Map;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.request.RequestBatchBase;
import app.editors.manager.mvp.models.request.RequestBatchOperation;
import app.editors.manager.mvp.models.request.RequestCreate;
import app.editors.manager.mvp.models.request.RequestDeleteShare;
import app.editors.manager.mvp.models.request.RequestDownload;
import app.editors.manager.mvp.models.request.RequestExternal;
import app.editors.manager.mvp.models.request.RequestNumber;
import app.editors.manager.mvp.models.request.RequestRegister;
import app.editors.manager.mvp.models.request.RequestRenameFile;
import app.editors.manager.mvp.models.request.RequestShare;
import app.editors.manager.mvp.models.request.RequestSignIn;
import app.editors.manager.mvp.models.request.RequestStorage;
import app.editors.manager.mvp.models.request.RequestTitle;
import app.editors.manager.mvp.models.request.RequestUser;
import app.editors.manager.mvp.models.request.RequestValidatePortal;
import app.editors.manager.mvp.models.response.ResponseCapabilities;
import app.editors.manager.mvp.models.response.ResponseCount;
import app.editors.manager.mvp.models.response.ResponseCreateFile;
import app.editors.manager.mvp.models.response.ResponseCreateFolder;
import app.editors.manager.mvp.models.response.ResponseDownload;
import app.editors.manager.mvp.models.response.ResponseExplorer;
import app.editors.manager.mvp.models.response.ResponseExternal;
import app.editors.manager.mvp.models.response.ResponseFile;
import app.editors.manager.mvp.models.response.ResponseFiles;
import app.editors.manager.mvp.models.response.ResponseFolder;
import app.editors.manager.mvp.models.response.ResponseGroups;
import app.editors.manager.mvp.models.response.ResponseModules;
import app.editors.manager.mvp.models.response.ResponseOperation;
import app.editors.manager.mvp.models.response.ResponsePortal;
import app.editors.manager.mvp.models.response.ResponseRegisterPersonalPortal;
import app.editors.manager.mvp.models.response.ResponseRegisterPortal;
import app.editors.manager.mvp.models.response.ResponseSettings;
import app.editors.manager.mvp.models.response.ResponseShare;
import app.editors.manager.mvp.models.response.ResponseSignIn;
import app.editors.manager.mvp.models.response.ResponseThirdparty;
import app.editors.manager.mvp.models.response.ResponseUser;
import app.editors.manager.mvp.models.response.ResponseUsers;
import app.editors.manager.mvp.models.response.ResponseValidatePortal;
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

    final class Errors {
        public static final String AUTH = "User authentication failed";
        public static final String PORTAL_EXIST = "Portal already exist";
        public static final String SMS_TO_MANY = "You have sent too many text messages";
        public static final String DISK_SPACE_QUOTA = "Disk space quota exceeded";
    }

    final class HttpCodes {
        public static final int NONE = -1;
        public static final int SUCCESS = 200;
        public static final int REDIRECTION = 300;
        public static final int CLIENT_ERROR = 400;
        public static final int CLIENT_UNAUTHORIZED = 401;
        public static final int CLIENT_PAYMENT_REQUIRED = 402;
        public static final int CLIENT_FORBIDDEN = 403;
        public static final int CLIENT_NOT_FOUND = 404;
        public static final int SERVER_ERROR = 500;
    }

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

    final class ShareType {
        public static final String NONE = "None";
        public static final String READ_WRITE = "ReadWrite";
        public static final String READ = "Read";
        public static final String RESTRICT = "Restrict";
        public static final String VARIES = "Varies";
        public static final String REVIEW = "Review";
        public static final String COMMENT = "Comment";
        public static final String FILL_FORMS = "FillForms";

        public static int getCode(final String type) {
            switch (type) {
                case NONE:
                    return ShareCode.NONE;
                case READ_WRITE:
                    return ShareCode.READ_WRITE;
                case READ:
                    return ShareCode.READ;
                case RESTRICT:
                    return ShareCode.RESTRICT;
                case VARIES:
                    return ShareCode.VARIES;
                case REVIEW:
                    return ShareCode.REVIEW;
                case COMMENT:
                    return ShareCode.COMMENT;
                case FILL_FORMS:
                    return ShareCode.FILL_FORMS;
                default:
                    return ShareCode.NONE;
            }
        }
    }

    final class ShareCode {
        public static final int NONE = 0;
        public static final int READ_WRITE = 1;
        public static final int READ = 2;
        public static final int RESTRICT = 3;
        public static final int VARIES = 4;
        public static final int REVIEW = 5;
        public static final int COMMENT = 6;
        public static final int FILL_FORMS = 7;

        public static String getType(final int code) {
            switch (code) {
                case NONE:
                    return ShareType.NONE;
                case READ_WRITE:
                    return ShareType.READ_WRITE;
                case READ:
                    return ShareType.READ;
                case RESTRICT:
                    return ShareType.RESTRICT;
                case VARIES:
                    return ShareType.VARIES;
                case REVIEW:
                    return ShareType.REVIEW;
                default:
                    return ShareType.NONE;
            }
        }
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

    final class FileStatus {
        public static final int NONE = 0x0;
        public static final int IS_EDITING = 0x1;
        public static final int IS_NEW = 0x2;
        public static final int IS_CONVERTING = 0x4;
        public static final int IS_ORIGINAL = 0x8;
        public static final int BACKUP = 0x10;
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

    final class Operation {
        public static final int SKIP = 0;
        public static final int OVERWRITE = 1;
        public static final int DUPLICATE = 2;
    }

    final class Social {
        public static final String TWITTER = "twitter";
        public static final String FACEBOOK = "facebook";
        public static final String GOOGLE = "google";
    }

    /*
     * Api constants
     * */
    String API_VERSION = "2.0";
    String SCHEME_HTTPS = "https://";
    String SCHEME_HTTP = "http://";
    String API_SUBDOMAIN = "api-system";
    String PERSONAL_SUBDOMAIN = "personal";
    String DEFAULT_HOST = "onlyoffice.com";
    String PERSONAL_HOST = PERSONAL_SUBDOMAIN + "." + DEFAULT_HOST;
    String RESPONSE_FORMAT = ".json";
    String COOKIE_HEADER = "asc_auth_key=";

    /*
     * Portals versions
     * */
    String PORTAL_VERSION_10 = "10.0.0.297";

    /*
     * Headers
     * */
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_HOST = "Host";
    String HEADER_CONTENT_TYPE = "Content-OperationType";
    String HEADER_ACCEPT = "Accept";
    String HEADER_AGENT = "User-Agent";
    String HEADER_CACHE = "Cache-Control";

    String VALUE_CONTENT_TYPE = "application/json";
    String VALUE_ACCEPT = "application/json";
    String VALUE_CACHE = "no-cache";

    String DOWNLOAD_ZIP_NAME = "download.zip";

    /*
     * Sign in
     * */
    @Headers({HEADER_CACHE + ":" + VALUE_CACHE,
            HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/authentication" + RESPONSE_FORMAT)
    Call<ResponseSignIn> signIn(@Body RequestSignIn body);

    /*
     * Check portal
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/capabilities" + RESPONSE_FORMAT)
    Call<ResponseCapabilities> capabilities();

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/thirdparty/capabilities" + RESPONSE_FORMAT)
    Single<ResponseBody> getThirdpartyCapabilities(@Header(HEADER_AUTHORIZATION) String token);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/thirdparty" + RESPONSE_FORMAT)
    Observable<ResponseThirdparty> getThirdPartyList(@Header(HEADER_AUTHORIZATION) String token);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/settings/version/build" + RESPONSE_FORMAT)
    Observable<ResponseSettings> getSettings();

    /*
     * Auth with SMS code
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/authentication/{sms_code}" + RESPONSE_FORMAT)
    Call<ResponseSignIn> smsSignIn(@Body RequestSignIn body, @Path(value = "sms_code") String smsCode);

    /*
     * Resend SMS code
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/authentication/sendsms" + RESPONSE_FORMAT)
    Call<ResponseSignIn> sendSms(@Body RequestSignIn body);

    /*
     * Change number
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/authentication/setphone" + RESPONSE_FORMAT)
    Call<ResponseSignIn> changeNumber(@Body RequestNumber body);

    /*
     * Validate portal
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE})
    @POST("/api/portal/validateportalname")
    Call<ResponseValidatePortal> validatePortal(@Body RequestValidatePortal body);

    /*
     * Register portal
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE})
    @POST("/api/portal/register")
    Call<ResponseRegisterPortal> registerPortal(@Body RequestRegister body);

    /*
     * Register personal portal
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE})
    @POST("api/" + API_VERSION + "/authentication/register" + RESPONSE_FORMAT)
    Call<ResponseRegisterPersonalPortal> registerPersonalPortal(@Body RequestRegister body);

    /*
     * Counts of users
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/portal/userscount" + RESPONSE_FORMAT)
    Call<ResponseCount> getCountUsers(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Users info
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/people/@self" + RESPONSE_FORMAT)
    Call<ResponseUser> getUserInfo(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Get folder/files by id
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/{item_id}" + RESPONSE_FORMAT)
    Call<ResponseExplorer> getItemById(@Header(HEADER_AUTHORIZATION) String token,
                                       @Path(value = "item_id") String folderId,
                                       @QueryMap Map<String, String> options);

    /*
     * Create docs file
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/files/{folder_id}/file" + RESPONSE_FORMAT)
    Call<ResponseCreateFile> createDocs(@Header(HEADER_AUTHORIZATION) String token,
                                        @Path(value = "folder_id") String folderId,
                                        @Body RequestCreate body);

    /*
     * Get file info
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/file/{file_id}" + RESPONSE_FORMAT)
    Call<ResponseFile> getFileInfo(@Header(HEADER_AUTHORIZATION) String token,
                                   @Path(value = "file_id") String fileId);

    /*
     * Create folder
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/files/folder/{folder_id}" + RESPONSE_FORMAT)
    Call<ResponseCreateFolder> createFolder(@Header(HEADER_AUTHORIZATION) String token,
                                            @Path(value = "folder_id") String folderId,
                                            @Body RequestCreate body);

    /*
     * Operation batch
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/fileops/delete" + RESPONSE_FORMAT)
    Call<ResponseOperation> deleteBatch(@Header(HEADER_AUTHORIZATION) String token,
                                        @Body RequestBatchBase body);

    /*
     * Move items
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/fileops/move" + RESPONSE_FORMAT)
    Call<ResponseOperation> move(@Header(HEADER_AUTHORIZATION) String token,
                                 @Body RequestBatchOperation body);

    /*
     * Copy items
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/fileops/copy" + RESPONSE_FORMAT)
    Call<ResponseOperation> copy(@Header(HEADER_AUTHORIZATION) String token,
                                 @Body RequestBatchOperation body);

    /*
     * Terminate all operations
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/fileops/terminate" + RESPONSE_FORMAT)
    Call<ResponseOperation> terminate(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Status operations
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/fileops" + RESPONSE_FORMAT)
    Single<ResponseOperation> status(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Rename folder
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/folder/{folder_id}" + RESPONSE_FORMAT)
    Call<ResponseFolder> renameFolder(@Header(HEADER_AUTHORIZATION) String token,
                                      @Path(value = "folder_id") String folderId,
                                      @Body RequestTitle body);

    /*
     * Rename file
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/file/{file_id}" + RESPONSE_FORMAT)
    Call<ResponseFile> renameFile(@Header(HEADER_AUTHORIZATION) String token,
                                  @Path(value = "file_id") String folderId,
                                  @Body RequestRenameFile body);

    /*
     * Get share folder
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/folder/{folder_id}/share" + RESPONSE_FORMAT)
    Call<ResponseShare> getShareFolder(@Header(HEADER_AUTHORIZATION) String token,
                                       @Path(value = "folder_id") String folderId);

    /*
     * Get share file
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/file/{file_id}/share" + RESPONSE_FORMAT)
    Call<ResponseShare> getShareFile(@Header(HEADER_AUTHORIZATION) String token,
                                     @Path(value = "file_id") String fileId);

    /*
     * Get external link
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/{file_id}/sharedlink" + RESPONSE_FORMAT)
    Call<ResponseExternal> getExternalLink(@Header(HEADER_AUTHORIZATION) String token,
                                           @Path(value = "file_id") String fileId,
                                           @Body RequestExternal body);

    /*
     * Set access for folder
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/folder/{folder_id}/share" + RESPONSE_FORMAT)
    Call<ResponseShare> setFolderAccess(@Header(HEADER_AUTHORIZATION) String token,
                                        @Path(value = "folder_id") String fileId,
                                        @Body RequestShare body);

    /*
     * Set access for file
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/file/{file_id}/share" + RESPONSE_FORMAT)
    Call<ResponseShare> setFileAccess(@Header(HEADER_AUTHORIZATION) String token,
                                      @Path(value = "file_id") String fileId,
                                      @Body RequestShare body);

    /*
     * Delete share setting
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @HTTP(method = "DELETE", path = "api/" + API_VERSION + "/files/share" + RESPONSE_FORMAT, hasBody = true)
    Call<Base> deleteShare(@Header(HEADER_AUTHORIZATION) String token,
                           @Body RequestDeleteShare body);

    /*
     * Get groups
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/group" + RESPONSE_FORMAT)
    Call<ResponseGroups> getGroups(@Header(HEADER_AUTHORIZATION) String token,
                                   @QueryMap Map<String, String> options);

    /*
     * Get users
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/people" + RESPONSE_FORMAT)
    Call<ResponseUsers> getUsers(@Header(HEADER_AUTHORIZATION) String token,
                                 @QueryMap Map<String, String> options);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/people/{user_id}" + RESPONSE_FORMAT)
    Call<ResponseUser> updateUser(@Header(HEADER_AUTHORIZATION) String token,
                                  @Path(value = "user_id") String userId,
                                  @Body RequestUser body);

    /*
     * Get portal
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/portal" + RESPONSE_FORMAT)
    Call<ResponsePortal> getPortal(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Empty trash
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @PUT("api/" + API_VERSION + "/files/fileops/emptytrash" + RESPONSE_FORMAT)
    Call<ResponseOperation> emptyTrash(@Header(HEADER_AUTHORIZATION) String token);

    /*
     * Connect storage
     * */
    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @POST("api/" + API_VERSION + "/files/thirdparty" + RESPONSE_FORMAT)
    Call<ResponseFolder> connectStorage(@Header(HEADER_AUTHORIZATION) String token,
                                        @Body RequestStorage body);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @DELETE("api/" + API_VERSION + "/files/thirdparty/{folder_id}" + RESPONSE_FORMAT)
    Call<ResponseBody> deleteStorage(@Header(HEADER_AUTHORIZATION) String token,
                                 @Path(value = "folder_id") String id);

    /*
     * Download file
     * */
    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Header(HEADER_AUTHORIZATION) String token, @Url String url, @Header("Cookie") String cookie);

    @PUT("api/" + API_VERSION + "/files/fileops/bulkdownload" + RESPONSE_FORMAT)
    Single<ResponseDownload> downloadFiles(@Header(HEADER_AUTHORIZATION) String token, @Body RequestDownload requestDownload);

    /*
     * Upload  file
     * */
    @Multipart
    @POST("api/" + API_VERSION + "/files/{folder_id}/upload" + RESPONSE_FORMAT)
    Call<ResponseFile> uploadFile(@Header(HEADER_AUTHORIZATION) String token,
                                    @Path(value = "folder_id") String folderId,
                                    @Part MultipartBody.Part part);

    @Multipart
    @POST("api/" + API_VERSION + "/files/{folder_id}/upload" + RESPONSE_FORMAT)
    Single<Response<ResponseBody>> uploadMultiFiles(@Header(HEADER_AUTHORIZATION) String token,
                                      @Path(value = "folder_id") String folderId,
                                      @Part MultipartBody.Part[] part);

    @Multipart
    @POST("api/" + API_VERSION + "/files/@my/upload" + RESPONSE_FORMAT)
    Call<ResponseFile> uploadFileToMy(@Header(HEADER_AUTHORIZATION) String token,
                                        @Part MultipartBody.Part part);

    /*
     * Insert  file
     * */
    @Multipart
    @POST("api/" + API_VERSION + "/files/{folder_id}/insert" + RESPONSE_FORMAT)
    Call<ResponseFile> insertFile(@Header(HEADER_AUTHORIZATION) String token,
                                  @Path(value = "folder_id") String folderId,
                                  @Part("title") String title,
                                  @Part MultipartBody.Part part);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/files/fileops/move" + RESPONSE_FORMAT)
    Single<ResponseFiles> checkFiles(@Header(HEADER_AUTHORIZATION) String token,
                                     @Query("destFolderId") String destFolderId,
                                     @Query("folderIds") List<String> folderIds,
                                     @Query("fileIds") List<String> fileIds);

    @Headers({HEADER_CONTENT_TYPE + ": " + VALUE_CONTENT_TYPE,
            HEADER_ACCEPT + ": " + VALUE_ACCEPT})
    @GET("api/" + API_VERSION + "/settings/security" + RESPONSE_FORMAT)
    Single<ResponseModules> getModules(@Header(HEADER_AUTHORIZATION) String token, @Query("ids") List<String> modulesIds);
}
