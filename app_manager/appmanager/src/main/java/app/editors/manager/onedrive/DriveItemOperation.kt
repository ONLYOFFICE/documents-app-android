package app.editors.manager.onedrive

import android.app.Application
import kotlinx.serialization.Serializable

@Serializable
data class DriveItemOperation(
    val application: DriveItemUtil? = null,
    val user: DriveItemUtil
)
