package app.documents.core.database.database

import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import app.documents.core.database.dao.AccountDao
import app.documents.core.database.dao.PortalDao
import app.documents.core.database.entity.CloudAccountEntity
import app.documents.core.database.entity.CloudPortalEntity
import app.documents.core.model.cloud.CloudAccount

@Database(entities = [CloudAccountEntity::class, CloudPortalEntity::class], version = 1)
abstract class CloudDatabase : RoomDatabase() {

    companion object {

        val TAG: String = CloudDatabase::class.java.simpleName
    }

    fun addObserver(onInvalidated: () -> Unit) {
        invalidationTracker.addObserver(
            object : InvalidationTracker.Observer(arrayOf(CloudAccount::class.java.simpleName)) {

                override fun onInvalidated(tables: Set<String>) {
                    onInvalidated.invoke()
                }
            }
        )
    }

    internal abstract val accountDao: AccountDao
    internal abstract val portalDao: PortalDao
}