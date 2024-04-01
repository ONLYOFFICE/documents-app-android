package app.documents.core.network.login

import app.documents.core.network.HEADER_AUTHORIZATION
import app.documents.core.network.HEADER_DEPTH
import app.documents.core.network.HTTP_METHOD_GET
import app.documents.core.network.HTTP_METHOD_PROPFIND
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Url

interface WebdavLoginDataSource {

    suspend fun capability(url: String)

    suspend fun capabilities(url: String, credentials: String)
}

private interface WebdavLoginApi {

    @HTTP(method = HTTP_METHOD_GET)
    suspend fun capability(@Url url: String)

    @Headers("$HEADER_DEPTH: 0")
    @HTTP(method = HTTP_METHOD_PROPFIND)
    suspend fun capabilities(
        @Url url: String,
        @Header(HEADER_AUTHORIZATION) credentials: String
    )
}

internal class WebdavLoginDataSourceImpl(okHttpClient: OkHttpClient) : WebdavLoginDataSource {

    private val api: WebdavLoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://stub")
        .build()
        .create(WebdavLoginApi::class.java)

    override suspend fun capability(url: String) {
        api.capability(url)
    }

    override suspend fun capabilities(url: String, credentials: String) {
        api.capabilities(url, credentials)
    }


}