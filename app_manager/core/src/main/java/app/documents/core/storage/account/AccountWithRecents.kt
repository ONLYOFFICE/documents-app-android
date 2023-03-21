package app.documents.core.storage.account

import androidx.room.Embedded
import androidx.room.Relation
import app.documents.core.storage.recent.Recent


data class AccountWithRecents(
    @Embedded val account: CloudAccount,
    @Relation(entity = CloudAccount::class, parentColumn = "id", entityColumn = "ownerId")
    val recents: List<Recent>
)