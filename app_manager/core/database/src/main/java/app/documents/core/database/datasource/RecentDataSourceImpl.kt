package app.documents.core.database.datasource

import android.util.Log
import androidx.room.withTransaction
import app.documents.core.database.database.RecentDatabase
import app.documents.core.database.entity.RecentEntity
import app.documents.core.database.entity.toEntity
import app.documents.core.database.entity.toRecent
import app.documents.core.model.cloud.Recent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class RecentDataSourceImpl(private val db: RecentDatabase) : RecentDataSource {

    override suspend fun getRecentList(): List<Recent> {
        return db.recentDao.getRecentList().map(RecentEntity::toRecent)
    }

    override fun getRecentListFlow(): Flow<List<Recent>> {
        return db.recentDao.getRecentListFlow()
            .onEach { Log.e("sdsd", "datasource $it" ) }
            .map { it.map(RecentEntity::toRecent) }
    }

    override suspend fun add(recent: Recent) {
        db.recentDao.add(recent.toEntity())
    }

    override suspend fun updateRecent(recent: Recent) {
        db.recentDao.update(recent.toEntity())
    }

    override suspend fun deleteRecent(vararg recent: Recent) {
        db.withTransaction {
            recent.forEach { recent -> db.recentDao.delete(recent.toEntity()) }
        }
    }

    override suspend fun addRecent(recent: Recent) {
        db.withTransaction {
            if (recent.source == null) {
                db.recentDao.getRecentByFilePath(recent.path)?.let {
                    add(recent.copy(id = it.id))
                    return@withTransaction
                }
            } else {
                db.recentDao.getRecentByFileId(recent.fileId, recent.ownerId)?.let {
                    add(recent.copy(id = it.id))
                    return@withTransaction
                }
            }
            add(recent)
        }
    }
}