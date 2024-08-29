package app.documents.core.model.login

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    override val id: String = "",
    val name: String = "",
//    val manager: String? = "",
    override val shared: Boolean = false
): Comparable<Group>, Member {

    override fun compareTo(other: Group): Int = id.compareTo(other.id)
}