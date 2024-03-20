package app.documents.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.documents.core.model.cloud.CloudAccount

internal const val accountTableName = "account"

@Entity(tableName = accountTableName)
internal data class CloudAccountEntity(
    @PrimaryKey
    val accountId: String,
    val portalUrl: String,
    val login: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val socialProvider: String = "",
    val isAdmin: Boolean = false,
    val isVisitor: Boolean = false
)

internal fun CloudAccountEntity.toCloudAccount(): CloudAccount {
    return CloudAccount(
        id = accountId,
        portalUrl = portalUrl,
        login = login,
        name = displayName,
        avatarUrl = avatarUrl,
        socialProvider = socialProvider,
        isAdmin = isAdmin,
        isVisitor = isVisitor
    )
}

internal fun CloudAccount.toEntity(): CloudAccountEntity {
    return CloudAccountEntity(
        accountId = id,
        portalUrl = portalUrl,
        login = login,
        displayName = name,
        avatarUrl = avatarUrl,
        socialProvider = socialProvider,
        isAdmin = isAdmin,
        isVisitor = isVisitor
    )
}