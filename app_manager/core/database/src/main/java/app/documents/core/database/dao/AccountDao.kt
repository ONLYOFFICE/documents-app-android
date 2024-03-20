package app.documents.core.database.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.documents.core.database.entity.CloudAccountEntity
import app.documents.core.database.entity.accountTableName

@Dao
internal interface AccountDao {

    @Query("SELECT * FROM $accountTableName")
    suspend fun getAccounts(): List<CloudAccountEntity>

    @Query("SELECT * FROM $accountTableName")
    fun getCursorAccounts(): Cursor?

    @Query("SELECT * FROM $accountTableName WHERE login = :login")
    fun getCursorAccountsByLogin(login: String): Cursor?

    @Query("SELECT * FROM $accountTableName WHERE accountId = :id")
    suspend fun getAccount(id: String): CloudAccountEntity?

    @Query("SELECT * FROM $accountTableName WHERE login = :login")
    suspend fun getAccountByLogin(login: String): CloudAccountEntity?

    @Query("SELECT * FROM $accountTableName WHERE accountId = :id")
    fun getCursorAccount(id: String): Cursor?

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