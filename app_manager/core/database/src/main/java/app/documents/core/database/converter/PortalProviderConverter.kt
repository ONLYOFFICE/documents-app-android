package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object PortalProviderConverter {

    @TypeConverter
    fun toPortalProvider(string: String): PortalProvider {
        return Json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalProvider: PortalProvider): String {
        return Json.encodeToString(portalProvider)
    }
}