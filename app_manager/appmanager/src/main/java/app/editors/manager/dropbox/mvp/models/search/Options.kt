package app.editors.manager.dropbox.mvp.models.search

import kotlinx.serialization.Serializable


@Serializable
data class Options(
    val path: String = "",
    val max_result: Int = 0,
    val file_status: String = "",
    val filename_only: Boolean = false
)
