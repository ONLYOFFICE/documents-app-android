package app.documents.core.network.manager.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class GoogleDriveFolder(var webUrl: String = "") : CloudFolder()
