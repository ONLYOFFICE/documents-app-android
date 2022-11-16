package app.documents.core.storage.account

import android.database.Cursor
import androidx.room.*

@Dao
interface AccountDao {

    @Query("SELECT * FROM CloudAccount")
    suspend fun getAccounts() : List<CloudAccount>

    @Query("SELECT * FROM CloudAccount")
    fun getCursorAccounts() : Cursor?

    @Query("SELECT * FROM CloudAccount WHERE login = :login")
    fun getCursorAccountsByLogin(login: String) : Cursor?

    @Query("SELECT * FROM CloudAccount WHERE id = :id")
    suspend fun getAccount(id: String) : CloudAccount?

    @Query("SELECT * FROM CloudAccount WHERE login = :login")
    suspend fun getAccountByLogin(login: String) : CloudAccount?

    @Query("SELECT * FROM CloudAccount WHERE id = :id")
    fun getCursorAccount(id: String) : Cursor?

    @Query("SELECT * FROM CloudAccount WHERE isOnline = :isOnline")
    suspend fun getAccountOnline(isOnline: Boolean = true) : CloudAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccount(account: CloudAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCursorAccount(account: CloudAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccounts(account: List<CloudAccount>)

    @Update
    suspend fun updateAccount(account: CloudAccount)

    @Update
    fun updateCursorAccount(account: CloudAccount): Int

    @Delete
    suspend fun deleteAccount(account: CloudAccount)

    @Delete
    fun deleteCursorAccount(account: CloudAccount): Int

}