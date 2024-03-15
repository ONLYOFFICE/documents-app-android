package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.documents.core.database.converter.PortalProviderConverter
import app.documents.core.database.converter.PortalSettingConverter
import app.documents.core.database.converter.PortalVersionConverter
import app.documents.core.database.converter.SocialProvidersConverter
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider
import app.documents.core.model.cloud.PortalSettings
import app.documents.core.model.cloud.PortalVersion
import app.documents.core.model.cloud.Scheme

const val portalTableName = "portal"

@Entity(tableName = portalTableName)
@TypeConverters(
    PortalVersionConverter::class,
    PortalProviderConverter::class,
    PortalSettingConverter::class,
    SocialProvidersConverter::class
)
data class CloudPortalEntity(
    @PrimaryKey
    val url: String = "",
    val scheme: String = "",
    val socialProviders: List<String> = emptyList(),
    val version: PortalVersion,
    val provider: PortalProvider,
    val settings: PortalSettings
)

fun CloudPortalEntity.toCloudPortal(): CloudPortal {
    return CloudPortal(
        scheme = Scheme.valueOf(scheme),
        url = url,
        socialProviders = socialProviders,
        version = version,
        provider = provider,
        settings = settings
    )
}

fun CloudPortal.toEntity(): CloudPortalEntity {
    return CloudPortalEntity(
        scheme = scheme.value,
        url = url,
        socialProviders = socialProviders,
        version = version,
        provider = provider,
        settings = settings
    )
}