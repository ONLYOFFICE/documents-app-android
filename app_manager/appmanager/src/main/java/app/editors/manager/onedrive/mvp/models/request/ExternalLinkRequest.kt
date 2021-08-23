package app.editors.manager.onedrive.mvp.models.request

import kotlinx.serialization.Serializable


@Serializable
data class ExternalLinkRequest(
    val type: String = "",
    val scope: String = ""
)

