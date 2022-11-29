package app.documents.core.network.storages.dropbox.models.search

import kotlinx.serialization.Serializable

@Serializable
data class MatchFieldOptions(
    val include_highlights: Boolean = false
)
