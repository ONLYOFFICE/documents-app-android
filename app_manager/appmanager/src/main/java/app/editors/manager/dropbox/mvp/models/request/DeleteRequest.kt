package app.editors.manager.dropbox.mvp.models.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteRequest(
    val path: String = ""
)
