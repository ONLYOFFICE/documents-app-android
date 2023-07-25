package app.documents.core.network.webdav


import app.documents.core.network.webdav.models.WebDavModel
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface WebDavService {

    enum class Providers(val path: String) {
        NextCloud(path = "/remote.php/dav/files/"),
        OwnCloud(path = "/remote.php/dav/files/"),
        KDrive(path = "/"),
        Yandex(path = "/"),
        WebDav(path = "/");
    }

    companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_DESTINATION = "Destination"
        const val HEADER_OVERWRITE = "Overwrite"
        const val HEADER_DEPTH_0 = "Depth: 0"
        const val HEADER_DEPTH_1 = "Depth: 1"
    }

    @HTTP(method = "GET")
    fun capability(@Url url: String): Observable<Response<ResponseBody>>

    @ConverterFactory.Xml
    @Headers(HEADER_DEPTH_0)
    @HTTP(method = "PROPFIND")
    fun capabilities(
        @Header(HEADER_AUTHORIZATION) auth: String,
        @Url path: String?
    ): Observable<Response<ResponseBody>>

    @ConverterFactory.Xml
    @Headers(HEADER_DEPTH_1)
    @HTTP(method = "PROPFIND")
    fun propfind(@Url path: String): Call<WebDavModel>

    @ConverterFactory.Xml
    @HTTP(method = "MKCOL")
    fun createFolder(@Url path: String): Call<ResponseBody>

    @ConverterFactory.Xml
    @HTTP(method = "DELETE")
    fun delete(@Url path: String): Call<ResponseBody>

    @ConverterFactory.Xml
    @HTTP(method = "MOVE")
    fun move(
        @Header(HEADER_DESTINATION) newFile: String,
        @Url oldFile: String,
        @Header(HEADER_OVERWRITE) overwrite: String
    ): Call<ResponseBody>

    @ConverterFactory.Xml
    @HTTP(method = "COPY")
    fun copy(
        @Header(HEADER_DESTINATION) newFile: String,
        @Url oldFile: String,
        @Header(HEADER_OVERWRITE) overwrite: String
    ): Call<ResponseBody>

    @ConverterFactory.Xml
    @HTTP(method = "LOCK")
    fun lock(@Url path: String): Call<ResponseBody>

    @ConverterFactory.Xml
    @HTTP(method = "UNLOCK")
    fun unlock(@Url path: String): Call<ResponseBody>

    @ConverterFactory.Xml
    @PUT
    fun upload(@Body body: RequestBody, @Url name: String): Call<ResponseBody>

    @ConverterFactory.Xml
    @Streaming
    @GET
    fun download(@Url path: String): Single<Response<ResponseBody>>

}