package app.documents.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString

internal object SocialProvidersConverter {

    @TypeConverter
    fun toPortalProvider(string: String): List<String> {
        return json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(providers: List<String>): String {
        return json.encodeToString(providers)
    }
}