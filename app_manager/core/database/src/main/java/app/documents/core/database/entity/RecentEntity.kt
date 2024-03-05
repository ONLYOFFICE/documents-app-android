package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.documents.core.database.converter.PortalProviderConverter
import app.documents.core.model.cloud.Recent

@Entity(tableName = "recent")
@TypeConverters(PortalProviderConverter::class)
data class RecentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileId: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = 0,
    val size: Long = 0,
    val ownerId: String? = null,
    val source: String? = null,
    val isWebdav: Boolean = false
)

fun RecentEntity.toRecent(): Recent {
    return Recent(id, fileId, path, name, date, size, ownerId, source, isWebdav)
}