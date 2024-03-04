package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalVersion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object PortalVersionConverter {

    @TypeConverter
    fun toPortalVersion(string: String): PortalVersion {
        return Json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalVersion: PortalVersion): String {
        return Json.encodeToString(portalVersion)
    }
}