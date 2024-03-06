package app.documents.core.database.datasource

import app.documents.core.database.database.CloudDatabase
import app.documents.core.database.entity.CloudAccountEntity
import app.documents.core.database.entity.toCloudAccount
import app.documents.core.database.entity.toCloudPortal
import app.documents.core.database.entity.toEntity
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal

internal class CloudDataSourceImpl(private val db: CloudDatabase) : CloudDataSource {

    //
    //    private val db = Room
    //        .databaseBuilder(context, CloudDatabase::class.java, CloudDatabase.TAG)
    //        .build()

    override fun addObserver(onInvalidated: () -> Unit) {
        db.addObserver(onInvalidated)
    }

    override suspend fun getAccount(id: String): CloudAccount? {
        return db.accountDao.getAccount(id)?.toCloudAccount()
    }

    override suspend fun deleteAccount(account: CloudAccount) {
        db.accountDao.deleteAccount(account.toEntity())
    }

    override suspend fun getAccounts(): List<CloudAccount> {
        return db.accountDao.getAccounts().map(CloudAccountEntity::toCloudAccount)
    }

    override suspend fun getAccountOnline(): CloudAccount? {
        val account = db.accountDao.getAccountOnline()?.toCloudAccount()
        val portal = db.portalDao.getByAccountId(account?.id.orEmpty())
        return account?.copy(portal = portal?.toCloudPortal() ?: CloudPortal())
    }

    override suspend fun getAccountByLogin(login: String): CloudAccount? {
        return db.accountDao.getAccountByLogin(login)?.toCloudAccount()
    }

    override suspend fun getPortal(accountId: String): CloudPortal? {
        return db.portalDao.getByAccountId(accountId)?.toCloudPortal()
    }

    override suspend fun insertPortal(cloudPortal: CloudPortal) {
        db.portalDao.insert(cloudPortal.toEntity())
    }

    override suspend fun updatePortal(cloudPortal: CloudPortal) {
        db.portalDao.update(cloudPortal.toEntity())
    }

    override suspend fun removePortal(portalId: String) {
        db.portalDao.delete(portalId)
    }

    override suspend fun updateAccount(cloudAccount: CloudAccount) {
        db.accountDao.updateAccount(cloudAccount.toEntity())
    }

    override suspend fun addAccount(account: CloudAccount) {
        db.accountDao.addAccount(account.toEntity())
    }
}