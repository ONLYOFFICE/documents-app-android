package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.app.App
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.roomApi
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.RoomFilterTag
import app.editors.manager.mvp.models.filter.RoomFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.presenterScope

class RoomFilterPresenter(val folderId: String?) : BaseFilterPresenter() {

    var filterType: RoomFilterType = RoomFilterType.None
        set(value) {
            field = value
            update()
        }

    var filterTag: RoomFilterTag? = null
        set(value) {
            field = value
            update()
        }

    var filterAuthor: FilterAuthor = FilterAuthor()
        set(value) {
            field = value
            update()
        }

    override val hasFilter: Boolean
        get() = preferenceTool.filter.roomType != RoomFilterType.None || filterAuthor != FilterAuthor() || filterTag != null

    private val filters: Map<String, String> get() = mutableMapOf<String, String>().apply {
        put(ApiContract.Parameters.ARG_FILTER_BY_SUBJECT_ID, filterAuthor.id)
        if (filterType != RoomFilterType.None)
            put(ApiContract.Parameters.ARG_FILTER_BY_TYPE_ROOM, filterType.filterVal.toString())
        if (filterTag != null)
            put(ApiContract.Parameters.ARG_FILTER_BY_TAG_ROOM, filterTag?.value ?: "")
    }

    init {
        App.getApp().appComponent.inject(this)
        loadFilter()
    }

    override fun loadFilter() {
        val filter = preferenceTool.filter
        filterType = filter.roomType
        filterAuthor = filter.author

        getTags()
    }

    private fun getTags() {
        presenterScope.launch(Dispatchers.IO) {
            val tags = context.roomApi.getTags().tags
            if (tags.isEmpty()) return@launch

            withContext(Dispatchers.Main) {
                viewState.onTagsLoaded(tags)
            }
        }

    }

    override fun saveFilter() {
        preferenceTool.filter = preferenceTool.filter.copy(roomType = filterType, author = filterAuthor, tag = filterTag)
    }

    override fun update(initialCall: Boolean) {
        saveFilter()
        viewState.updateViewState(isChanged = !initialCall)
        disposable?.clear()
        disposable?.add(
            context.cloudFileProvider.getRooms(filters)
                .doOnSubscribe { viewState.onFilterProgress() }
                .subscribe(
                    { explorer: Explorer ->
                        viewState.onFilterResult(explorer.count)
                    }, { error: Throwable -> viewState.onError(error.localizedMessage) }
                )
        )
    }

    override fun reset() {
        filterType = RoomFilterType.None
        filterAuthor = FilterAuthor()
        filterTag = null
        viewState.onFilterReset()
    }

    fun clearAuthor() {
        filterAuthor = FilterAuthor()
    }
}