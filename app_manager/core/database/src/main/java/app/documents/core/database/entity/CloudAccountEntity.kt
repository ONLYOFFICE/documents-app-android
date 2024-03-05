package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.documents.core.model.cloud.CloudAccount

const val accountTableName = "account"

@Entity(tableName = "account")
internal data class CloudAccountEntity(
    @PrimaryKey
    val accountId: String,
    val login: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val socialProvider: String = "",
    val isOnline: Boolean = false,
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false
)

internal fun CloudAccountEntity.toCloudAccount(): CloudAccount {
    return CloudAccount(accountId, login, displayName, avatarUrl, socialProvider, isOnline, isAdmin, isVisitor)
}

internal fun CloudAccount.toEntity(): CloudAccountEntity {
    return CloudAccountEntity(id, login, name, avatarUrl, socialProvider, isOnline, isAdmin, isVisitor)
}