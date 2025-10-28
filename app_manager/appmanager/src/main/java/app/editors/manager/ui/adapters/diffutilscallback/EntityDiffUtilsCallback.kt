package app.editors.manager.ui.adapters.diffutilscallback

import app.documents.core.network.manager.models.base.Entity
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.editors.manager.mvp.models.list.Footer
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.list.RecentViaLink
import app.editors.manager.mvp.models.list.Templates

class EntityDiffUtilsCallback(
    newList: List<Entity>,
    oldList: List<Entity>
) : BaseDiffUtilsCallback<Entity>(newList, oldList) {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEntity = mOldList[oldItemPosition]
        val newEntity = mNewList[newItemPosition]

        return when {
            oldEntity is Header && newEntity is Header -> true
            oldEntity is Templates && newEntity is Templates -> true
            oldEntity is RecentViaLink && newEntity is RecentViaLink -> true
            oldEntity is CloudFile && newEntity is CloudFile -> newEntity.id == oldEntity.id
            oldEntity is CloudFolder && newEntity is CloudFolder -> newEntity.id == oldEntity.id
            oldEntity is Footer && newEntity is Footer -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEntity = mOldList[oldItemPosition]
        val newEntity = mNewList[newItemPosition]

        return when {
            oldEntity is Templates && newEntity is Templates -> true
            oldEntity is RecentViaLink && newEntity is RecentViaLink -> true
            oldEntity is Header && newEntity is Header -> newEntity.title == oldEntity.title

            oldEntity is CloudFile && newEntity is CloudFile ->
                newEntity.title == oldEntity.title
                        && newEntity.version == oldEntity.version
                        && newEntity.updated == oldEntity.updated
                        && newEntity.thumbnailUrl == oldEntity.thumbnailUrl
                        && newEntity.customFilterEnabled == oldEntity.customFilterEnabled
                        && newEntity.isEditing == oldEntity.isEditing
                        && newEntity.thumbnailStatus == oldEntity.thumbnailStatus
                        && newEntity.formFillingStatusType == oldEntity.formFillingStatusType

            oldEntity is CloudFolder && newEntity is CloudFolder ->
                newEntity.title == oldEntity.title
                        && newEntity.filesCount == oldEntity.filesCount
                        && newEntity.updated == oldEntity.updated

            oldEntity is Footer && newEntity is Footer -> true

            else -> false
        }
    }
}
