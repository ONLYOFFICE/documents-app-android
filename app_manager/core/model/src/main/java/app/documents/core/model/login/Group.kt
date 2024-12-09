package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    override val id: String = "",
    val name: String = "",
    override val shared: Boolean = false,
    val memberCount: Int = 0,
    val isLDAP: Boolean = false,
): Comparable<Group>, Member {

    override fun compareTo(other: Group): Int = id.compareTo(other.id)
}