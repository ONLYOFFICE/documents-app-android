package app.documents.core.storage.recent

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import app.documents.core.storage.account.CloudAccount

@Entity(
    foreignKeys = [ForeignKey(
        entity = CloudAccount::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Recent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val idFile: String?,
    val path: String?,
    val name: String,
    val date: Long,
    val isLocal: Boolean,
    val isWebDav: Boolean,
    val size: Long,
    val ownerId: String? = null,
    val source: String? = null
)