package app.documents.core.database.converter

import androidx.room.TypeConverter
import app.documents.core.model.cloud.PortalProvider
import kotlinx.serialization.encodeToString

internal object PortalProviderConverter {

    @TypeConverter
    fun toPortalProvider(string: String): PortalProvider {
        return json.decodeFromString(string)
    }

    @TypeConverter
    fun toString(portalProvider: PortalProvider): String {
        return json.encodeToString(portalProvider)
    }
}