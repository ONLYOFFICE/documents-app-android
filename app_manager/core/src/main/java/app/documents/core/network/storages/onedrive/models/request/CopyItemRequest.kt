package app.documents.core.network.storages.onedrive.models.request

import app.documents.core.network.storages.onedrive.models.explorer.DriveItemParentReference
import kotlinx.serialization.Serializable

@Serializable
data class CopyItemRequest(
    val parentReference: DriveItemParentReference? = null,
    val name: String = ""
)
