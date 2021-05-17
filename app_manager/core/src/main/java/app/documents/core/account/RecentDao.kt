package app.documents.core.account

import androidx.room.*

@Dao
abstract class RecentDao {

    @Query("SELECT * FROM Recent")
    abstract suspend fun getRecents(): List<Recent>

    @Query("SELECT * FROM Recent WHERE ownerId =:ownerId")
    abstract suspend fun getRecentsByOwnerId(ownerId: String): List<Recent>

    @Query("SELECT * FROM Recent WHERE idFile =:id and ownerId = :ownerId")
    abstract suspend fun getRecentByFileId(id: String?, ownerId: String?): Recent?

    @Query("SELECT * FROM Recent WHERE path =:path")
    abstract suspend fun getRecentByFilePath(path: String): Recent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun add(recent: Recent)

    @Update
    abstract suspend fun updateRecent(recent: Recent)

    @Delete
    abstract suspend fun deleteRecent(recent: Recent)

    @Transaction
    open suspend fun addRecent(recent: Recent) {
        if (recent.isLocal) {
            getRecentByFilePath(recent.path ?: "")?.let {
                add(recent.copy(id = it.id))
                return
            }
        } else {
            getRecentByFileId(recent.idFile, recent.ownerId)?.let {
                add(recent.copy(id = it.id))
                return
            }
        }
        add(recent)
    }

    @Transaction
    open suspend fun removeAllByOwnerId(ownerId: String) {
        getRecentsByOwnerId(ownerId).forEach {
            deleteRecent(it)
        }
    }

}