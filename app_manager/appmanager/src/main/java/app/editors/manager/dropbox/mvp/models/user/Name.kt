package app.editors.manager.dropbox.mvp.models.user

import kotlinx.serialization.Serializable

@Serializable
data class Name(
    val given_name: String = "",
    val surname: String = "",
    val familiar_name: String = "",
    val display_name: String = "",
    val abbreviated_name: String = ""
)
