package app.documents.core.network.storages.onedrive.models.explorer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DriveItemCloudTree(
    @SerialName("@odata.context") val context: String = "",
    @SerialName("@odata.count") val count: String = "",
    val value: List<DriveItemValue> = emptyList()
)
