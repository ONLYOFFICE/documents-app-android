package app.documents.core.network.manager.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class ExportIndexOperation(
    val id: String = "",
    val error: String = "",
    val isCompleted: Boolean = false,
    val percentage: Int = 0,
    val resultFileId: String = "",
    val resultFileName: String = "",
    val resultFileUrl: String = "",
    val status: Int = 0,
)