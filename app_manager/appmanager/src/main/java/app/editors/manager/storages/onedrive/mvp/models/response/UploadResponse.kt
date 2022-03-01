package app.editors.manager.storages.onedrive.mvp.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    @SerialName("@odata.context") val context: String = "",
    val expirationDateTime: String = "",
    val nextExpectedRanges: List<String> = emptyList(),
    val uploadUrl: String = ""
)
