package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class RequestAddTags(val names: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestAddTags

        if (!names.contentEquals(other.names)) return false

        return true
    }

    override fun hashCode(): Int {
        return names.contentHashCode()
    }
}