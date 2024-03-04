package app.documents.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

internal data class CloudAccountAndPortal(
    @Embedded val cloudAccountEntity: CloudAccountEntity,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "accountId"
    )
    val cloudPortalEntity: CloudPortalEntity
)