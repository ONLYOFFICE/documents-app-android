package app.editors.manager.app;

import java.util.concurrent.TimeUnit;

import app.editors.manager.managers.retrofit.ConverterFactory;
import app.editors.manager.managers.retrofit.WebDavInterceptor;
import app.editors.manager.mvp.models.explorer.WebDavModel;
import io.reactivex.Observable;
import lib.toolkit.base.managers.http.UnsafeClient;
import lib.toolkit.base.managers.tools.RetrofitTool;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface WebDavApi {

    enum Providers {
        NextCloud("/remote.php/dav/files/"),
        OwnCloud("/remote.php/dav/files/"),
        Yandex("/"),
        WebDav("/");

        private String mPath;

        Providers(String path) {
            this.mPath = path;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            this.mPath = path;
        }

    }

    static WebDavApi getApi(String baseUrl, Boolean isSslState) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl.concat("/");
        }

        OkHttpClient client;
        if (!isSslState) {
            client = getUnsafeClient();
        } else {
            client = getClient();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new ConverterFactory())
                .client(client)
                .build();
        return retrofit.create(WebDavApi.class);
    }

    static OkHttpClient getUnsafeClient() {
        return UnsafeClient.getUnsafeBuilder()
                .readTimeout(RetrofitTool.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(RetrofitTool.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(RetrofitTool.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new WebDavInterceptor())
                .build();
    }

    static OkHttpClient getClient() {
        return new OkHttpClient().newBuilder()
                .readTimeout(RetrofitTool.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(RetrofitTool.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(RetrofitTool.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new WebDavInterceptor())
                .build();
    }


    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_DESTINATION = "Destination";
    String HEADER_OVERWRITE = "Overwrite";
    String HEADER_DEPTH_0 = "Depth: 0";
    String HEADER_DEPTH_1 = "Depth: 1";

    @HTTP(method = "GET")
    Call<ResponseBody> capability(@Url String url);

    @ConverterFactory.Xml
    @Headers({HEADER_DEPTH_0})
    @HTTP(method = "PROPFIND")
    Observable<Response<ResponseBody>> capabilities(@Header(HEADER_AUTHORIZATION) String auth, @Url String path);

    @ConverterFactory.Xml
    @Headers({HEADER_DEPTH_1})
    @HTTP(method = "PROPFIND")
    Call<WebDavModel> propfind(@Url String path);

    @ConverterFactory.Xml
    @HTTP(method = "MKCOL")
    Call<ResponseBody> createFolder(@Url String path);

    @ConverterFactory.Xml
    @HTTP(method = "DELETE")
    Call<ResponseBody> delete(@Url String path);

    @ConverterFactory.Xml
    @HTTP(method = "MOVE")
    Call<ResponseBody> move(@Header(HEADER_DESTINATION) String newFile, @Url String oldFile, @Header(HEADER_OVERWRITE) String overwrite);

    @ConverterFactory.Xml
    @HTTP(method = "COPY")
    Call<ResponseBody> copy(@Header(HEADER_DESTINATION) String newFile, @Url String oldFile, @Header(HEADER_OVERWRITE) String overwrite);

    @ConverterFactory.Xml
    @HTTP(method = "LOCK")
    Call<ResponseBody> lock(@Url String path);

    @ConverterFactory.Xml
    @HTTP(method = "UNLOCK")
    Call<ResponseBody> unlock(@Url String path);

    @ConverterFactory.Xml
    @PUT()
    Call<ResponseBody> upload(@Body RequestBody body, @Url String name);

    @ConverterFactory.Xml
    @Streaming
    @GET
    Call<ResponseBody> download(@Url() String path);


}
