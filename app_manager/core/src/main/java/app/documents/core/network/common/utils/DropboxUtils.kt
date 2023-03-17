package app.documents.core.network.common.utils

import app.documents.core.network.storages.dropbox.login.DropboxResponse
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.CharacterEscapes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import retrofit2.Response
import java.io.ByteArrayOutputStream

object DropboxUtils {

    const val DROPBOX_PORTAL = "www.dropbox.com"
    const val DROPBOX_ROOT = "/ "
    const val DROPBOX_ROOT_TITLE = "root"
    const val DROPBOX_API_ARG_HEADER = "Dropbox-API-Arg"
    const val DROPBOX_CONTINUE_CURSOR = "cursor"
    const val DROPBOX_SEARCH_CURSOR = "search_cursor"
    const val DROPBOX_ACCESS_TOKEN_NAME = "access_token"
    const val DROPBOX_ACCOUNT_ID_NAME = "account_id"

    const val DROPBOX_BASE_URL = "https://api.dropboxapi.com/"
    const val DROPBOX_BASE_URL_CONTENT = "https://content.dropboxapi.com/"

    const val DROPBOX_ERROR_EMAIL_NOT_VERIFIED = "email_not_verified"
    const val DROPBOX_EXPIRED_ACCESS_TOKEN = "expired_access_token"
    const val DROPBOX_INVALID_ACCESS_TOKEN = "invalid_access_token"

    @JvmStatic
    fun encodeUnicodeSymbolsDropbox(title: String): String {
        val factory = JsonFactory()
        val stream = ByteArrayOutputStream()
        val generator = factory.createGenerator(stream)
        generator.apply {
            setHighestNonEscapedChar(0x7E)
            characterEscapes = object : CharacterEscapes() {
                override fun getEscapeCodesForAscii(): IntArray {
                    val esc = standardAsciiEscapesForJSON()
                    esc[0x7E] = ESCAPE_STANDARD
                    return esc
                }

                override fun getEscapeSequence(ch: Int): SerializableString {
                    TODO("Not yet implemented")
                }

            }
            writeStartObject()
            writeStringField("path", title)
            writeEndObject()
            close()
        }
        return stream.toString().split(":")[1].split("\"")[1]
    }

    fun getErrorMessage(response: Response<*>): DropboxResponse.Error {
        val message = response.errorBody()?.string()
        return if (!message.isNullOrEmpty()) {
            val errorResponse = Gson().fromJson(message, DropboxError::class.java)
            DropboxResponse.Error(Throwable(errorResponse.error.message))
        } else {
            DropboxResponse.Error(HttpException(response))
        }
    }

    private data class DropboxError(
        @SerializedName("error")
        val error: ErrorField
    ) {
        data class ErrorField(
            @SerializedName(".tag")
            val message: String
        )
    }
}