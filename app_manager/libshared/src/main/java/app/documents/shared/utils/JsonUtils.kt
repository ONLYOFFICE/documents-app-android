package app.documents.shared.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

internal inline fun <reified T> Json.Default.decodeFromString(
    string: String,
    decodeUrl: Boolean
): T? {
    val decodedJson = if (decodeUrl)
        URLDecoder.decode(string, Charsets.UTF_8.toString()) else
        string
    return runCatching { Json.decodeFromString<T>(decodedJson) }.getOrNull()
}

internal inline fun <reified T> Json.Default.encodeToString(obj: T, encodeUrl: Boolean): String? {
    val json = runCatching { Json.encodeToString(obj) }.getOrElse { return null }
    return if (encodeUrl) {
        URLEncoder.encode(json, Charsets.UTF_8.toString())
    } else {
        json
    }
}