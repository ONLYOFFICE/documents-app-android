package app.documents.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.documents.core.database.entity.RecentEntity
import app.documents.core.database.entity.recentTableName
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RecentDao {

    @Query("SELECT * FROM $recentTableName")
    fun getRecentListFlow(): Flow<List<RecentEntity>>

    @Query("SELECT * FROM $recentTableName")
    suspend fun getRecentList(): List<RecentEntity>

    @Query("SELECT * FROM $recentTableName WHERE fileId =:id and ownerId = :ownerId")
    suspend fun getRecentByFileId(id: String?, ownerId: String?): RecentEntity?

    @Query("SELECT * FROM $recentTableName WHERE path =:path")
    suspend fun getRecentByFilePath(path: String): RecentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(recent: RecentEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(recent: RecentEntity)

    @Transaction
    @Delete
    suspend fun delete(recent: RecentEntity)
}