package app.editors.manager.storages.onedrive.mvp.models.request

import app.editors.manager.storages.onedrive.mvp.models.explorer.DriveItemParentReference
import kotlinx.serialization.Serializable

@Serializable
data class CopyItemRequest(
    val parentReference: DriveItemParentReference? = null,
    val name: String = ""
)
