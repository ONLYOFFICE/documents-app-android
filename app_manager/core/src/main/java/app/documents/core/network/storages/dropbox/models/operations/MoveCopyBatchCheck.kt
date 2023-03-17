package app.documents.core.network.storages.dropbox.models.operations

import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyBatchCheck(
    val async_job_id: String = ""
)
