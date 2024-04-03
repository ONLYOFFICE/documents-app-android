package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalVersion
import kotlinx.serialization.encodeToString

internal object PortalVersionConverter {

    @TypeConverter
    fun toPortalVersion(string: String): PortalVersion {
        return json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalVersion: PortalVersion): String {
        return json.encodeToString(portalVersion)
    }
}