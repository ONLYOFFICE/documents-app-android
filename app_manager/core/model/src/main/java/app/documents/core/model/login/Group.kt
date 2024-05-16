package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
class Group(
    val id: String = "",
    val name: String = "",
    val manager: String? = "",
    val shared: Boolean = false
): Comparable<Group> {

    override fun compareTo(other: Group): Int = id.compareTo(other.id)
}

@Serializable
class RoomGroup(
    val id: String = "",
    val name: String = "",
    val shared: Boolean = false
)