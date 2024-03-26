package app.documents.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object SocialProvidersConverter {

    @TypeConverter
    fun toPortalProvider(string: String): List<String> {
        return Json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(providers: List<String>): String {
        return Json.encodeToString(providers)
    }
}