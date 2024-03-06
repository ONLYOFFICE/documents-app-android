package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.documents.core.database.converter.PortalProviderConverter
import app.documents.core.database.converter.PortalSettingConverter
import app.documents.core.database.converter.PortalVersionConverter
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme

@Entity(tableName = "portal")
@TypeConverters(PortalVersionConverter::class, PortalProviderConverter::class, PortalSettingConverter::class)
data class CloudPortalEntity(
    @PrimaryKey
    val portalId: String = "",
    val accountId: String = "",
    val scheme: String = "",
    val portal: String = "",
    val version: PortalVersion,
    val provider: PortalProvider,
    val settings: PortalSettings
)

fun CloudPortalEntity.toCloudPortal(): CloudPortal {
    return CloudPortal(
        portalId = portalId,
        accountId = accountId,
        scheme = Scheme.valueOf(scheme),
        portal = portal,
        version = version,
        provider = provider,
        settings = settings
    )
}

fun CloudPortal.toEntity(): CloudPortalEntity {
    return CloudPortalEntity(
        portalId = portalId,
        accountId = accountId,
        scheme = scheme.value,
        portal = portal,
        version = version,
        provider = provider,
        settings = settings
    )
}