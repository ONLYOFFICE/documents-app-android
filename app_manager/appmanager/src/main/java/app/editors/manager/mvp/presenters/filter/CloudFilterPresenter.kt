package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.filter.isNotEmpty

class CloudFilterPresenter(private val folderId: String?) : BaseFilterPresenter() {

    private var fileProvider: CloudFileProvider? = null

    var filterType: FilterType = FilterType.None
    var filterAuthor: FilterAuthor = FilterAuthor()
    var resultCount = -1

    var excludeSubfolder: Boolean = false
        set(value) {
            field = value
            update()
        }

    private val filters: Map<String, String>
        get() = mutableMapOf<String, String>().apply {
            put(ApiContract.Parameters.ARG_FILTER_BY_TYPE, filterType.filterVal)
            if (filterAuthor.isNotEmpty())
                put(ApiContract.Parameters.ARG_FILTER_BY_AUTHOR, filterAuthor.id)
            if (filterType != FilterType.None) {
                put(ApiContract.Parameters.ARG_FILTER_SUBFOLDERS, (!excludeSubfolder).toString())
            }
        }

    override val hasFilter: Boolean
        get() = filterAuthor.id.isNotEmpty() || excludeSubfolder || filterType != FilterType.None

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = CloudFileProvider()
        loadFilter()
    }

    override fun loadFilter() {
        val filter = preferenceTool.filter
        filterType = filter.type
        excludeSubfolder = filter.excludeSubfolder
        filterAuthor = filter.author
    }

    override fun saveFilter() {
        preferenceTool.filter = preferenceTool.filter.copy(
            type = filterType,
            author = filterAuthor,
            excludeSubfolder = excludeSubfolder
        )
    }

    override fun update(initialCall: Boolean) {
        saveFilter()
        viewState.updateViewState(isChanged = !initialCall)
        disposable?.clear()
        fileProvider?.let { provider ->
            disposable?.add(
                provider.getFiles(folderId, filters)
                    .doOnSubscribe { viewState.onFilterProgress() }
                    .subscribe(
                        { explorer: Explorer ->
                            resultCount = explorer.count
                            viewState.onFilterResult(resultCount)
                        }, { error: Throwable -> viewState.onError(error.localizedMessage) }
                    )
            )
        }
    }

    override fun reset() {
        filterType = FilterType.None
        filterAuthor = FilterAuthor()
        excludeSubfolder = false
        viewState.onFilterReset()
        update()
    }

    fun clearAuthor() {
        filterAuthor = FilterAuthor()
        update()
    }
}