package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.app.App
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.models.filter.isNotEmpty

class CloudFilterPresenter(private val folderId: String?, private val section: Int?) : BaseFilterPresenter() {

    var filterType: FilterType = FilterType.None
    var filterAuthor: FilterAuthor = FilterAuthor()
    var resultCount = -1
    var location = 0

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
            if (location > 0) {
                put(ApiContract.Parameters.ARG_FILTER_LOCATION, location.toString())
            }
        }

    override val hasFilter: Boolean
        get() = filterAuthor.id.isNotEmpty() || excludeSubfolder || filterType != FilterType.None || location > 0

    init {
        App.getApp().appComponent.inject(this)
        loadFilter()
    }

    override fun loadFilter() {
        val filter = filterManager.getFilter(section)
        filterType = filter.type
        excludeSubfolder = filter.excludeSubfolder
        filterAuthor = filter.author
        location = filter.location
    }

    override fun saveFilter() {
        filterManager.saveFilter(section) { filter ->
            filter.copy(
                type = filterType,
                author = filterAuthor,
                excludeSubfolder = excludeSubfolder,
                location = location
            )
        }
    }

    override fun update(initialCall: Boolean) {
        saveFilter()
        viewState.updateViewState(isChanged = !initialCall)
        disposable?.clear()
        disposable?.add(
            with(context.cloudFileProvider) {
                if (section == ApiContract.SectionType.CLOUD_RECENT) {
                    getRecentViaLink(filters).toObservable()
                } else {
                    getFiles(folderId, filters)
                }
            }
                .doOnSubscribe { viewState.onFilterProgress() }
                .subscribe(
                    { explorer: Explorer ->
                        resultCount = explorer.count
                        viewState.onFilterResult(resultCount)
                    }, { error: Throwable -> viewState.onError(error.localizedMessage) }
                )
        )
    }

    override fun reset() {
        filterType = FilterType.None
        filterAuthor = FilterAuthor()
        excludeSubfolder = false
        location = 0
        viewState.onFilterReset()
        update()
    }

    fun clearAuthor() {
        filterAuthor = FilterAuthor()
        update()
    }
}