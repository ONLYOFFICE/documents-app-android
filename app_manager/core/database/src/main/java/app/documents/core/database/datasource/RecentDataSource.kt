package app.documents.core.database.datasource

import app.documents.core.model.cloud.Recent
import kotlinx.coroutines.flow.Flow


interface RecentDataSource {

    fun getRecentListFlow(): Flow<List<Recent>>

    suspend fun getRecentList(): List<Recent>

    suspend fun add(recent: Recent)

    suspend fun updateRecent(recent: Recent)

    suspend fun deleteRecent(vararg recent: Recent)

    suspend fun addRecent(recent: Recent)
}
