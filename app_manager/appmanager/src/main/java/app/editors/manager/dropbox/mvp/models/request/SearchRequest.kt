package app.editors.manager.dropbox.mvp.models.request

import app.editors.manager.dropbox.mvp.models.search.MatchFieldOptions
import app.editors.manager.dropbox.mvp.models.search.Options
import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String = "",
    val options: Options? = null,
    val match_field_options: MatchFieldOptions? = null
)
