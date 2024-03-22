package app.documents.core.migration

import androidx.room.Dao
import androidx.room.Query

@Dao
internal interface OldAccountDao {

    @Query("SELECT * FROM CloudAccount")
    suspend fun getAccounts(): List<OldCloudAccount>
}