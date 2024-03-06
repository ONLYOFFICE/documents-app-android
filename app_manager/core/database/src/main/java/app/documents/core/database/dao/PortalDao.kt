package app.documents.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.documents.core.database.entity.CloudPortalEntity

@Dao
internal interface PortalDao {

    @Query("SELECT * FROM portal WHERE :accountId = accountId")
    suspend fun getByAccountId(accountId: String): CloudPortalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portal: CloudPortalEntity)

    @Query("DELETE FROM portal WHERE :portalId = portalId")
    suspend fun delete(portalId: String)

    @Update
    suspend fun update(portal: CloudPortalEntity)
}