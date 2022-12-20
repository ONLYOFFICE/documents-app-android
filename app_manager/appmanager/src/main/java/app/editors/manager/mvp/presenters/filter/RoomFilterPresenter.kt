package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.RoomFilterType

class RoomFilterPresenter(val folderId: String?) : BaseFilterPresenter() {

    private var fileProvider: CloudFileProvider? = null

    var filterType: RoomFilterType = RoomFilterType.None
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
        get() = preferenceTool.filter.roomType != RoomFilterType.None || filterAuthor != FilterAuthor()

    private val filters: Map<String, String> get() = mutableMapOf<String, String>().apply {
        put(ApiContract.Parameters.ARG_FILTER_BY_SUBJECT_ID, filterAuthor.id)
        if (filterType != RoomFilterType.None)
            put(ApiContract.Parameters.ARG_FILTER_BY_TYPE_ROOM, filterType.filterVal.toString())
    }

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = CloudFileProvider()
        loadFilter()
    }

    override fun loadFilter() {
        val filter = preferenceTool.filter
        filterType = filter.roomType
        filterAuthor = filter.author
    }

    override fun saveFilter() {
        preferenceTool.filter = preferenceTool.filter.copy(roomType = filterType, author = filterAuthor)
    }

    override fun update(initialCall: Boolean) {
        saveFilter()
        viewState.updateViewState(isChanged = !initialCall)
        disposable?.clear()
        fileProvider?.let { provider ->
            disposable?.add(
                provider.getRooms(filters)
                    .doOnSubscribe { viewState.onFilterProgress() }
                    .subscribe(
                        { explorer: Explorer ->
                            viewState.onFilterResult(explorer.count)
                        }, { error: Throwable -> viewState.onError(error.localizedMessage) }
                    )
            )
        }
    }

    override fun reset() {
        filterType = RoomFilterType.None
        filterAuthor = FilterAuthor()
        viewState.onFilterReset()
    }

    fun clearAuthor() {
        filterAuthor = FilterAuthor()
    }
}