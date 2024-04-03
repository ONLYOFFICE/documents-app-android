package app.documents.core.database.database

import androidx.room.Database
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import app.documents.core.database.dao.AccountDao
import app.documents.core.database.dao.PortalDao
import app.documents.core.database.entity.CloudAccountEntity
import app.documents.core.database.entity.CloudPortalEntity
import app.documents.core.database.entity.accountTableName
import app.documents.core.database.entity.portalTableName

@Database(
    entities = [CloudAccountEntity::class, CloudPortalEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CloudDatabase : RoomDatabase() {

    companion object {

        internal const val NAME: String = "cloud_database"
    }

    fun addObserver(onInvalidated: () -> Unit) {
        invalidationTracker.addObserver(
            object : InvalidationTracker.Observer(arrayOf(accountTableName, portalTableName)) {

                override fun onInvalidated(tables: Set<String>) {
                    onInvalidated.invoke()
                }
            }
        )
    }

    internal abstract val accountDao: AccountDao
    internal abstract val portalDao: PortalDao
}