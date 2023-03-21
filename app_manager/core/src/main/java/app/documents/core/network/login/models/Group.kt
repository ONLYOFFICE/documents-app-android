package app.documents.core.network.login.models

import kotlinx.serialization.Serializable

@Serializable
class Group(
    val id: String = "",
    val name: String = "",
    val manager: String? = ""
): Comparable<Group> {

    override fun compareTo(other: Group): Int = id.compareTo(other.id)
}