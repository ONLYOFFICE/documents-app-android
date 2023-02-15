package app.documents.core.network.storages.onedrive.models.other

import app.documents.core.network.common.utils.OneDriveUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName(OneDriveUtils.KEY_CONFLICT_BEHAVIOR) val conflictBehavior: String = ""
)
