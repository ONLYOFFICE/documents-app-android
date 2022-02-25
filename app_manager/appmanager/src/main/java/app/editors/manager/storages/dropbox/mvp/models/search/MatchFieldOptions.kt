package app.editors.manager.storages.dropbox.mvp.models.search

import kotlinx.serialization.Serializable

@Serializable
data class MatchFieldOptions(
    val include_highlights: Boolean = false
)
