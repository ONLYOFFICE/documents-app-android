package app.editors.manager.storages.onedrive.mvp.models.response

import app.editors.manager.storages.onedrive.mvp.models.other.Link
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkResponse(
    val id: String = "",
    val roles: List<String> = emptyList(),
    val link: Link? = null
)
