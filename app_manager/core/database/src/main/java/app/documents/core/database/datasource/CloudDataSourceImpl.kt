package app.documents.core.database.datasource

import androidx.room.withTransaction
import app.documents.core.database.database.CloudDatabase
import app.documents.core.database.entity.CloudAccountEntity
import app.documents.core.database.entity.toCloudAccount
import app.documents.core.database.entity.toCloudPortal
import app.documents.core.database.entity.toEntity
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal
import app.documents.core.model.cloud.PortalProvider

internal class CloudDataSourceImpl(private val db: CloudDatabase) : CloudDataSource {

    override val initTimestamp: Long
        get() = _initTimestamp

    private var _initTimestamp: Long = 0L

    init {
        // TODO: need core component scope
        // db.addObserver { _initTimestamp = System.currentTimeMillis() }
    }

    override suspend fun getAccount(id: String): CloudAccount? {
        return db.withTransaction {
            val account = db.accountDao.getAccount(id)?.toCloudAccount()
            val portal = db.portalDao.get(account?.portalUrl.orEmpty())
            account?.copy(portal = portal?.toCloudPortal() ?: CloudPortal())
        }
    }

    override suspend fun deleteAccount(account: CloudAccount) {
        db.accountDao.deleteAccount(account.toEntity())
    }

    override suspend fun deleteAccount(id: String): Int {
        return db.accountDao.deleteAccount(id)
    }

    override suspend fun getAccounts(): List<CloudAccount> {
        return db.withTransaction {
            db.accountDao.getAccounts()
                .map(CloudAccountEntity::toCloudAccount)
                .map { it.copy(portal = db.portalDao.get(it.portalUrl)?.toCloudPortal() ?: return@map it) }
        }
    }

    override suspend fun getAccountByLogin(login: String): CloudAccount? {
        return db.withTransaction {
            val account = db.accountDao.getAccountByLogin(login)?.toCloudAccount()
            val portal = db.portalDao.get(account?.portalUrl.orEmpty())
            account?.copy(portal = portal?.toCloudPortal() ?: CloudPortal())
        }
    }

    override suspend fun updateAccount(cloudAccount: CloudAccount): Int {
        return db.accountDao.updateAccount(cloudAccount.toEntity())
    }

    override suspend fun addAccount(account: CloudAccount) {
        db.accountDao.addAccount(account.toEntity())
    }

    override suspend fun insertOrUpdateAccount(cloudAccount: CloudAccount) {
        db.withTransaction {
            val exist = db.accountDao.getAccount(cloudAccount.id) != null
            val entity = cloudAccount.toEntity()
            if (exist) {
                db.accountDao.updateAccount(entity)
            } else {
                db.accountDao.addAccount(entity)
            }
        }
    }

    override suspend fun getPortal(url: String): CloudPortal? {
        return db.portalDao.get(url)?.toCloudPortal()
    }

    override suspend fun getPortals(): List<String> {
        return db.portalDao.getAll()
            .filter { it.provider == PortalProvider.Cloud.Workspace || it.provider == PortalProvider.Cloud.DocSpace }
            .map { it.url }
    }

    override suspend fun insertPortal(cloudPortal: CloudPortal) {
        db.portalDao.insertOrReplace(cloudPortal.toEntity())
    }

    override suspend fun insertOrUpdatePortal(cloudPortal: CloudPortal) {
        db.withTransaction {
            val exist = db.portalDao.get(cloudPortal.url) != null
            val entity = cloudPortal.toEntity()
            if (exist) {
                db.portalDao.update(entity)
            } else {
                db.portalDao.insert(entity)
            }
        }
    }

    override suspend fun removePortal(portalId: String) {
        db.portalDao.delete(portalId)
    }
}