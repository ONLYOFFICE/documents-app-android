package app.documents.core.database.datasource

import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.CloudPortal

interface CloudDataSource {

    val initTimestamp: Long

    suspend fun getAccountByLogin(login: String): CloudAccount?

    suspend fun updateAccount(cloudAccount: CloudAccount): Int

    suspend fun addAccount(account: CloudAccount)

    suspend fun insertOrUpdateAccount(cloudAccount: CloudAccount)

    suspend fun getAccounts(): List<CloudAccount>

    suspend fun getAccount(id: String): CloudAccount?

    suspend fun deleteAccount(account: CloudAccount)

    suspend fun deleteAccount(id: String): Int

    suspend fun getPortal(url: String): CloudPortal?

    suspend fun getPortals(): List<String>

    suspend fun insertPortal(cloudPortal: CloudPortal)

    suspend fun insertOrUpdatePortal(cloudPortal: CloudPortal)

    suspend fun removePortal(portalId: String)
}