package app.documents.core.account

import androidx.room.Embedded
import androidx.room.Relation


data class AccountWithRecents(
    @Embedded val account: CloudAccount,
    @Relation(entity = CloudAccount::class, parentColumn = "id", entityColumn = "ownerId")
    val recents: List<Recent>
)