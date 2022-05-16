package app.editors.manager.storages.dropbox.mvp.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val has_more: Boolean = false,
    val cursor: String = "",
    val matches: List<SearchMetadata> = emptyList()
)
