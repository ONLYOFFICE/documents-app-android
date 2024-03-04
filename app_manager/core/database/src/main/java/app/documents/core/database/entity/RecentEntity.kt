package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.documents.core.model.cloud.Provider
import app.documents.core.model.cloud.Recent

@Entity(tableName = "recent")
data class RecentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idFile: String = "",
    val path: String = "",
    val name: String = "",
    val date: Long = 0,
    val size: Long = 0,
    val ownerId: String? = null,
    val source: Provider? = null
)

fun RecentEntity.toRecent(): Recent {
    return Recent(id, idFile, path, name, date, size, ownerId, source)
}