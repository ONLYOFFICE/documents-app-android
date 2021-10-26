package app.editors.manager.dropbox.mvp.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val has_more: Boolean = false,
    val matches: List<SearchMetadata> = emptyList()
)
