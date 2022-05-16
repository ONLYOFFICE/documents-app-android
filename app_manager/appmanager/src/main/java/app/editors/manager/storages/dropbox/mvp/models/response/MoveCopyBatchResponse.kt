package app.editors.manager.storages.dropbox.mvp.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyBatchResponse(
    @SerialName(".tag") val tag: String = "",
    val async_job_id: String = ""
)
