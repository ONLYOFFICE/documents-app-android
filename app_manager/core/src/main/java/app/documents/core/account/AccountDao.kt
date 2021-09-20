package app.documents.core.account

import androidx.room.*

@Dao
interface AccountDao {

    @Query("SELECT * FROM CloudAccount")
    suspend fun getAccounts() : List<CloudAccount>

    @Query("SELECT * FROM CloudAccount WHERE id = :id")
    suspend fun getAccount(id: String) : CloudAccount?

    @Query("SELECT * FROM CloudAccount WHERE isOnline = :isOnline")
    suspend fun getAccountOnline(isOnline: Boolean = true) : CloudAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccount(account: CloudAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccounts(account: List<CloudAccount>)

    @Update
    suspend fun updateAccount(account: CloudAccount)

    @Delete
    suspend fun deleteAccount(account: CloudAccount)

}