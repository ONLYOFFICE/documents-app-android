package app.editors.manager.mvp.models.explorer

import kotlinx.serialization.Serializable

@Serializable
data class GoogleDriveFolder(
    var webUrl: String = ""
) : CloudFolder()
