package app.documents.core.network.storages.dropbox.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchMetadata(
    val match_type: SearchMatchType? = null,
    val metadata: SearchInnerMeta? = null
)
