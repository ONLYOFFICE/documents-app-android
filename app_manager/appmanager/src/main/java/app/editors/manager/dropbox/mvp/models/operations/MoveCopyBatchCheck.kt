package app.editors.manager.dropbox.mvp.models.operations

import kotlinx.serialization.Serializable

@Serializable
data class MoveCopyBatchCheck(
    val async_job_id: String = ""
)
