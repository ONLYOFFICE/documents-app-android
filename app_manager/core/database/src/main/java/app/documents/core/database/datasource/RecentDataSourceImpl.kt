package app.documents.core.database.datasource

import androidx.room.withTransaction
import app.documents.core.database.database.RecentDatabase
import app.documents.core.database.entity.RecentEntity
import app.documents.core.database.entity.toEntity
import app.documents.core.database.entity.toRecent
import app.documents.core.model.cloud.Recent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RecentDataSourceImpl(private val db: RecentDatabase) : RecentDataSource {

    override suspend fun getRecentList(): List<Recent> {
        return db.recentDao.getRecentList().map(RecentEntity::toRecent)
    }

    override fun getRecentListFlow(): Flow<List<Recent>> {
        return db.recentDao.getRecentListFlow()
            .map { it.map(RecentEntity::toRecent) }
    }

    override suspend fun insertOrUpdate(recent: Recent) {
        db.withTransaction {
            val entity = if (recent.isLocal) {
                db.recentDao.getRecentByFilePath(recent.path)
            } else {
                db.recentDao.getRecentByFileId(recent.fileId, recent.ownerId)
            }

            if (entity != null) {
                db.recentDao.update(entity.copy(name = recent.name, date = recent.date))
            } else {
                db.recentDao.add(recent.toEntity())
            }
        }
    }

    override suspend fun deleteRecent(vararg recent: Recent) {
        db.withTransaction {
            recent.forEach { recent -> db.recentDao.delete(recent.toEntity()) }
        }
    }
}