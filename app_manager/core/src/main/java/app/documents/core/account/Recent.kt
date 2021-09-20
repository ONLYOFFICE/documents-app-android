package app.documents.core.account

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val ownerId: String? = null
)