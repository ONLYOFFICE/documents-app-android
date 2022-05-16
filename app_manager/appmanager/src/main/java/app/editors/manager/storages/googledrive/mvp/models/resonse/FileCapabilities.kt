package app.editors.manager.storages.googledrive.mvp.models.resonse

import kotlinx.serialization.Serializable

@Serializable
data class FileCapabilities(
    val canDelete: Boolean = true
)
