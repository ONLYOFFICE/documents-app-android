package app.documents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.documents.core.database.entity.CloudPortalEntity
import app.documents.core.database.entity.portalTableName

@Dao
internal interface PortalDao {

    @Query("SELECT * FROM $portalTableName WHERE :url = url")
    suspend fun get(url: String): CloudPortalEntity?

    @Query("SELECT * FROM $portalTableName")
    suspend fun getAll(): List<CloudPortalEntity>

    @Query("DELETE FROM $portalTableName WHERE :url = url")
    suspend fun delete(url: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(portal: CloudPortalEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(portal: CloudPortalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(portal: CloudPortalEntity)
}