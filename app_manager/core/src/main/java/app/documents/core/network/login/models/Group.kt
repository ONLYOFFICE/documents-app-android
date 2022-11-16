package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
class Group(
    val id: String = "",
    val name: String = "",
    val manager: String? = ""
): Comparable<app.documents.core.network.login.models.Group> {

    override fun compareTo(other: app.documents.core.network.login.models.Group): Int = id.compareTo(other.id)
}