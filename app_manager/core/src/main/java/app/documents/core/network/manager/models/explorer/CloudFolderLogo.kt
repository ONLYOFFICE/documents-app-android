package app.documents.core.network.manager.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class CloudFolderLogo(
    val original: String? = null,
    val large: String? = null,
    val medium: String? = null,
    val small: String? = null
)