package app.documents.core.network.storages.dropbox.models.request

import app.documents.core.network.storages.dropbox.models.search.MatchFieldOptions
import app.documents.core.network.storages.dropbox.models.search.Options
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String = "",
    val options: Options? = null,
    val match_field_options: MatchFieldOptions? = null
)
