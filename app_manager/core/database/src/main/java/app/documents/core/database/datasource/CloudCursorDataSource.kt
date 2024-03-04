package app.documents.core.database.datasource

import android.database.Cursor
import app.documents.core.database.database.CloudDatabase
import app.documents.core.database.entity.toCloudAccount
import app.documents.core.database.entity.toEntity
import app.documents.core.model.cloud.CloudAccount
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CloudCursorDataSource @Inject constructor(private val db: CloudDatabase) {

    var dbTimestamp: Long = 0L

    init {

        db.addObserver { dbTimestamp = System.currentTimeMillis() }
    }

    fun getCursorAccountsByLogin(login: String): Cursor? {
        return db.accountDao.getCursorAccountsByLogin(login)
    }

    fun getCursorAccounts(): Cursor? {
        return db.accountDao.getCursorAccounts()
    }

    fun getCursorAccount(id: String): Cursor? {
        return db.accountDao.getCursorAccount(id)
    }

    suspend fun addAccount(account: CloudAccount) {
        db.accountDao.addAccount(account.toEntity())
    }

    suspend fun getAccount(id: String): CloudAccount? {
        return db.accountDao.getAccount(id)?.toCloudAccount()
    }

    fun deleteCursorAccount(account: CloudAccount): Int {
        return db.accountDao.deleteCursorAccount(account.toEntity())
    }

    fun updateCursorAccount(account: CloudAccount): Int {
        return db.accountDao.updateCursorAccount(account.toEntity())
    }

    fun getAccountOnline(): CloudAccount? {
        return runBlocking { db.accountDao.getAccountOnline()?.toCloudAccount() }
    }
}