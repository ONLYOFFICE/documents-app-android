package app.editors.manager.mvp.presenters.main

import app.documents.core.network.ApiContract
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.managers.providers.CloudFileProvider
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.views.main.DocsFilterView
import app.editors.manager.ui.activities.main.FilterType
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpPresenter
import java.lang.NullPointerException
import javax.inject.Inject

class DocsFilterPresenter(private val folderId: String?) : MvpPresenter<DocsFilterView>() {

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var fileProvider: CloudFileProvider? = null

    private var disposable = CompositeDisposable()

    var hasChanged = false

    var filterType: FilterType
        get() = preferenceTool.filterType
        set(type) {
            preferenceTool.filterType = type
            update()
        }

    var filterAuthor: String
        get() = preferenceTool.filterAuthor
        set(author) {
            preferenceTool.filterAuthor = author
            update()
        }

    var filterSubfolder: Boolean
        get() = preferenceTool.filterSubfolder
        set(value) {
            preferenceTool.filterSubfolder = value
        }

    val hasFilter: Boolean
        get() = filterAuthor.isNotEmpty() || filterSubfolder || filterType != FilterType.None

    val api: Api
        get() = fileProvider?.api ?: throw NullPointerException("File provider's api property is null")

    init {
        App.getApp().appComponent.inject(this)
        fileProvider = CloudFileProvider()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun update(initialCall: Boolean = false) {
        viewState.updateViewState()
        hasChanged = !initialCall
        fileProvider?.let { provider ->
            val filters = mapOf(ApiContract.Parameters.ARG_FILTER_BY_TYPE to filterType.filterVal)
            disposable.add(
                provider.getFiles(folderId, filters)
                    .doOnSubscribe { viewState.onFilterProgress() }
                    .subscribe(
                        { explorer: Explorer -> viewState.onFilterResult(explorer.count) },
                        { error: Throwable -> viewState.handleError(error.localizedMessage) }
                    )
            )
        }
    }

    fun reset() {
        filterType = FilterType.None
        filterAuthor = ""
        filterSubfolder = false
        viewState.onFilterReset()
        update()
    }
}