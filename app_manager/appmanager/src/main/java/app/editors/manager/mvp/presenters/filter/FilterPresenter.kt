package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.ApiContract
import app.editors.manager.app.App
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.filter.FilterView
import io.reactivex.disposables.CompositeDisposable

class FilterPresenter(private val folderId: String?) : BasePresenter<FilterView>() {

    private var fileProvider: CloudFileProvider? = null
    private var disposable: CompositeDisposable? = CompositeDisposable()

    var hasChanged = false
    var filterType: FilterType = FilterType.None
    var filterAuthor: FilterAuthor = FilterAuthor()
    var resultCount = -1

    var excludeSubfolder: Boolean = false
        set(value) {
            field = value
            saveFilter()
        }

    private val filters: Map<String, String>
        get() = mapOf(
            ApiContract.Parameters.ARG_FILTER_BY_TYPE to filterType.filterVal,
            ApiContract.Parameters.ARG_FILTER_BY_AUTHOR to filterAuthor.id
        )

    val hasFilter: Boolean
        get() = filterAuthor.id.isNotEmpty() || excludeSubfolder || filterType != FilterType.None

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = CloudFileProvider()
        getFilter()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun getFilter() {
        val filter = preferenceTool.filter
        filterType = filter.type
        excludeSubfolder = filter.excludeSubfolder
        filterAuthor = filter.author
    }

    private fun saveFilter() {
        preferenceTool.filter = Filter(filterType, filterAuthor, excludeSubfolder)
    }

    fun update(initialCall: Boolean = false) {
        viewState.updateViewState()
        hasChanged = !initialCall
        disposable?.clear()
        saveFilter()
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

    fun reset() {
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