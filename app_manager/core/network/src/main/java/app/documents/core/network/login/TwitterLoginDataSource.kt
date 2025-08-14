package app.documents.core.network.login

import app.documents.core.model.login.response.AccessTokenResponse
import app.documents.core.model.login.response.RequestTokenResponse
import app.documents.core.network.TWITTER_BASE_URL
import app.documents.core.network.VALUE_CONTENT_TYPE
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.URLEncoder
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private interface TwitterLoginApi {

    @POST("oauth/request_token")
    suspend fun getRequestToken(
        @Header("Authorization") authHeader: String
    ): String

    @FormUrlEncoded
    @POST("oauth/access_token")
    suspend fun getAccessToken(
        @Field("oauth_token") oauthToken: String,
        @Field("oauth_verifier") oauthVerifier: String
    ): String
}

interface TwitterLoginDataSource {
    suspend fun getRequestToken(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String
    ): RequestTokenResponse

    suspend fun getAccessToken(oauthToken: String, oauthVerifier: String): AccessTokenResponse
}

internal class TwitterLoginDataSourceImpl(json: Json, okHttpClient: OkHttpClient) :
    TwitterLoginDataSource {

    private val api: TwitterLoginApi = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(TWITTER_BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(VALUE_CONTENT_TYPE)))
        .build()
        .create(TwitterLoginApi::class.java)

    override suspend fun getAccessToken(
        oauthToken: String,
        oauthVerifier: String
    ): AccessTokenResponse {
        val response = api.getAccessToken(oauthToken, oauthVerifier)
        return parseAccessTokenResponse(response)
    }

    override suspend fun getRequestToken(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String
    ): RequestTokenResponse {
        val header = generateOAuthHeader(consumerKey, consumerSecret, callbackUrl)
        return parseRequestTokenResponse(api.getRequestToken(header))
    }

    fun generateOAuthHeader(
        consumerKey: String,
        consumerSecret: String,
        callbackUrl: String
    ): String {
        val nonce = UUID.randomUUID().toString().replace("-", "")
        val timestamp = (System.currentTimeMillis() / 1000).toString()

        val params = sortedMapOf(
            OAUTH_CALLBACK to callbackUrl,
            OAUTH_CONSUMER_KEY to consumerKey,
            OAUTH_NONCE to nonce,
            OAUTH_SIGNATURE_METHOD to SIGNATURE_METHOD,
            OAUTH_TIMESTAMP to timestamp,
            OAUTH_VERSION to VERSION
        )

        val baseString = "${HTTP_METHOD}&" +
                URLEncoder.encode(REQUEST_TOKEN_URL, CHARSET) + "&" +
                URLEncoder.encode(params.toQueryString(), CHARSET)

        val signingKey = URLEncoder.encode(consumerSecret, CHARSET) + "&"
        val signature = baseString.hmacSha1(signingKey).base64Encode()
        val headerParams = params.toMutableMap()
        headerParams[OAUTH_SIGNATURE] = signature

        return OAUTH_PREFIX + headerParams.entries.joinToString(", ") {
            "${it.key}=\"${URLEncoder.encode(it.value, CHARSET)}\""
        }
    }

    private fun Map<String, String>.toQueryString(): String =
        entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, CHARSET)}" }

    private fun String.hmacSha1(key: String): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key.toByteArray(), HMAC_ALGORITHM))
        return mac.doFinal(toByteArray())
    }

    private fun ByteArray.base64Encode(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

    private fun parseAccessTokenResponse(response: String): AccessTokenResponse {
        val params = response.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }

        return AccessTokenResponse(
            oauthToken = params["oauth_token"] ?: "",
            oauthTokenSecret = params["oauth_token_secret"] ?: "",
            userId = params["user_id"] ?: "",
            screenName = params["screen_name"] ?: ""
        )
    }

    private fun parseRequestTokenResponse(raw: String): RequestTokenResponse {
        val params = raw.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }

        return RequestTokenResponse(
            oauthToken = params["oauth_token"] ?: "",
            oauthTokenSecret = params["oauth_token_secret"] ?: "",
            callbackConfirmed = params["oauth_callback_confirmed"]?.toBoolean() == true
        )
    }
    
    companion object {
        const val SIGNATURE_METHOD = "HMAC-SHA1"
        const val VERSION = "1.0"
        const val REQUEST_TOKEN_URL = "https://api.x.com/oauth/request_token"
        const val HTTP_METHOD = "POST"
        const val CHARSET = "UTF-8"
        const val HMAC_ALGORITHM = "HmacSHA1"
        const val OAUTH_PREFIX = "OAuth "

        const val OAUTH_CALLBACK = "oauth_callback"
        const val OAUTH_CONSUMER_KEY = "oauth_consumer_key"
        const val OAUTH_NONCE = "oauth_nonce"
        const val OAUTH_SIGNATURE_METHOD = "oauth_signature_method"
        const val OAUTH_TIMESTAMP = "oauth_timestamp"
        const val OAUTH_VERSION = "oauth_version"
        const val OAUTH_SIGNATURE = "oauth_signature"
    }
}
