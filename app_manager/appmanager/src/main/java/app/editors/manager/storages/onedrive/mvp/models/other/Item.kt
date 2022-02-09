package app.editors.manager.storages.onedrive.mvp.models.other

import app.editors.manager.storages.onedrive.managers.utils.OneDriveUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName(OneDriveUtils.KEY_CONFLICT_BEHAVIOR) val conflictBehavior: String = ""
)
