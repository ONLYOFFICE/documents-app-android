package app.documents.core.network.room.models

import kotlinx.serialization.Serializable

@Serializable
data class DeleteVersionRequest(
    val fileId: String,
    val versions: Array<Int>
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeleteVersionRequest

        if (fileId != other.fileId) return false
        if (!versions.contentEquals(other.versions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileId.hashCode()
        result = 31 * result + versions.contentHashCode()
        return result
    }
}
