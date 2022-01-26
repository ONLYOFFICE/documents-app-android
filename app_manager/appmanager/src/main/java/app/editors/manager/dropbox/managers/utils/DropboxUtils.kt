package app.editors.manager.dropbox.managers.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.CharacterEscapes
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

}