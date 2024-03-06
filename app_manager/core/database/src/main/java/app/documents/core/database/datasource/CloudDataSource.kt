package app.documents.core.database.datasource

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal

interface CloudDataSource {

    fun addObserver(onInvalidated: () -> Unit)

    suspend fun getAccountOnline(): CloudAccount?

    suspend fun getAccountByLogin(login: String): CloudAccount?

    suspend fun getPortal(portalId: String): CloudPortal?

    suspend fun insertPortal(cloudPortal: CloudPortal)

    suspend fun updatePortal(cloudPortal: CloudPortal)

    suspend fun removePortal(portalId: String)

    suspend fun updateAccount(cloudAccount: CloudAccount)

    suspend fun addAccount(account: CloudAccount)

    suspend fun getAccounts(): List<CloudAccount>

    suspend fun getAccount(id: String): CloudAccount?

    suspend fun deleteAccount(account: CloudAccount)

}