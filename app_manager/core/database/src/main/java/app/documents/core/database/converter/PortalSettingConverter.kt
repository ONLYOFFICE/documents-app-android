package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object PortalSettingConverter {

    @TypeConverter
    fun toPortalSettings(string: String): PortalSettings {
        return Json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalSettings: PortalSettings): String {
        return Json.encodeToString(portalSettings)
    }
}