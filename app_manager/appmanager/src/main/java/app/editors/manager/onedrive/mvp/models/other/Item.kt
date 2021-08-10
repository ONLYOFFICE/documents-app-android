package app.editors.manager.onedrive.mvp.models.other

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("@microsoft.graph.conflictBehavior") val conflictBehavior: String = ""
)
