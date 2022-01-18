package app.editors.manager.dropbox.mvp.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchMetadata(
    val match_type: SearchMatchType? = null,
    val metadata: SearchInnerMeta? = null
)
