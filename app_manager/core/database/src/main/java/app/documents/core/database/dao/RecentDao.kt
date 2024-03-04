package app.documents.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.documents.core.database.entity.RecentEntity
import app.documents.core.model.cloud.Provider

@Dao
abstract class RecentDao {

    @Query("SELECT * FROM recent")
    abstract suspend fun getRecentEntity(): List<RecentEntity>

    @Query("SELECT * FROM recent WHERE idFile =:id and ownerId = :ownerId")
    abstract suspend fun getRecentEntityByFileId(id: String?, ownerId: String?): RecentEntity?

    @Query("SELECT * FROM recent WHERE path =:path")
    abstract suspend fun getRecentEntityByFilePath(path: String): RecentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun add(recent: RecentEntity)

    @Update
    abstract suspend fun updateRecentEntity(recent: RecentEntity)

    @Delete
    abstract suspend fun deleteRecentEntity(recent: RecentEntity)

    @Transaction
    open suspend fun addRecentEntity(recent: RecentEntity) {
        if (recent.source == Provider.LOCAL) {
            getRecentEntityByFilePath(recent.path)?.let {
                add(recent.copy(id = it.id))
                return
            }
        } else {
            getRecentEntityByFileId(recent.idFile, recent.ownerId)?.let {
                add(recent.copy(id = it.id))
                return
            }
        }
        add(recent)
    }
}