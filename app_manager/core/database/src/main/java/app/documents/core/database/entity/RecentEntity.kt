package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.documents.core.database.converter.PortalProviderConverter
import app.documents.core.model.cloud.Recent

internal const val recentTableName =  "recent"

@Entity(tableName = recentTableName)
@TypeConverters(PortalProviderConverter::class)
data class RecentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileId: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = 0,
    val size: Long = 0,
    val ownerId: String? = null, // null if local
    val source: String? = null, // null if local
    val isWebdav: Boolean = false
)

fun RecentEntity.toRecent(): Recent {
    return Recent(
        id = id,
        fileId = fileId,
        path = path,
        name = name,
        date = date,
        size = size,
        ownerId = ownerId,
        source = source,
        isWebdav = isWebdav
    )
}

fun Recent.toEntity(): RecentEntity {
    return RecentEntity(
        id = id,
        fileId = fileId,
        path = path,
        name = name,
        date = date,
        size = size,
        ownerId = ownerId,
        source = source,
        isWebdav = isWebdav
    )
}