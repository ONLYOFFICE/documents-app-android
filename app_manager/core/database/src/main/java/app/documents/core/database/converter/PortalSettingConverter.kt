package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalSettings
import kotlinx.serialization.encodeToString

internal object PortalSettingConverter {

    @TypeConverter
    fun toPortalSettings(string: String): PortalSettings {
        return json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalSettings: PortalSettings): String {
        return json.encodeToString(portalSettings)
    }
}