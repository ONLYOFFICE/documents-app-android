package app.documents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.documents.core.database.entity.CloudPortalEntity

@Dao
internal interface PortalDao {

    @Query("SELECT * FROM portal WHERE :url = url")
    suspend fun get(url: String): CloudPortalEntity?

    @Query("SELECT * FROM portal")
    suspend fun getAll(): List<CloudPortalEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(portal: CloudPortalEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(portal: CloudPortalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(portal: CloudPortalEntity)

    @Query("DELETE FROM portal WHERE :url = url")
    suspend fun delete(url: String)
}