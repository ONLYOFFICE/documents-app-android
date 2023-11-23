package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTags(val tags: Array<String> = emptyArray()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseTags

        if (!tags.contentEquals(other.tags)) return false

        return true
    }

    override fun hashCode(): Int {
        return tags.contentHashCode()
    }
}