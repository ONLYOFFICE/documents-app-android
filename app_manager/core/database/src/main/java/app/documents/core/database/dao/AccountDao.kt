package app.documents.core.database.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.documents.core.database.entity.CloudAccountAndPortal
import app.documents.core.database.entity.CloudAccountEntity

@Dao
internal interface AccountDao {

    @Transaction
    @Query("SELECT * FROM account")
    fun getAccountsAndPortals(): List<CloudAccountAndPortal>

    @Transaction
    @Query("SELECT * FROM account WHERE :accountId = accountId")
    suspend fun getAccountAndPortalByAccountId(accountId: String): CloudAccountAndPortal

    @Query("SELECT * FROM account")
    suspend fun getAccounts(): List<CloudAccountEntity>

    @Query("SELECT * FROM account")
    fun getCursorAccounts(): Cursor?

    @Query("SELECT * FROM account WHERE login = :login")
    fun getCursorAccountsByLogin(login: String): Cursor?

    @Query("SELECT * FROM account WHERE accountId = :id")
    suspend fun getAccount(id: String): CloudAccountEntity?

    @Query("SELECT * FROM account WHERE login = :login")
    suspend fun getAccountByLogin(login: String): CloudAccountEntity?

    @Query("SELECT * FROM account WHERE accountId = :id")
    fun getCursorAccount(id: String): Cursor?

    @Query("SELECT * FROM account WHERE isOnline = 1")
    suspend fun getAccountOnline(): CloudAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccount(account: CloudAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCursorAccount(account: CloudAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAccounts(account: List<CloudAccountEntity>)

    @Update
    suspend fun updateAccount(account: CloudAccountEntity)

    @Update
    fun updateCursorAccount(account: CloudAccountEntity): Int

    @Delete
    suspend fun deleteAccount(account: CloudAccountEntity)

    @Delete
    fun deleteCursorAccount(account: CloudAccountEntity): Int

}