package app.editors.manager.mvp.presenters.filter

import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.user.Thirdparty
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.app.roomProvider
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterProvider
import app.editors.manager.mvp.models.filter.RoomFilterTag
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.models.filter.joinToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.presenterScope

class RoomFilterPresenter(private val section: Int?) : BaseFilterPresenter() {

    var filterType: RoomFilterType = RoomFilterType.None
        set(value) {
            field = value
            update()
        }

    var filterTags: List<RoomFilterTag> = emptyList()
        set(value) {
            field = value
            update()
        }

    var filterAuthor: FilterAuthor = FilterAuthor()
        set(value) {
            field = value
            update()
        }

    var filterProvider: FilterProvider? = null
        set(value) {
            field = value
            update()
        }

    private val isTemplatesSection: Boolean
        get() = ApiContract.SectionType.isTemplates(section)

    override val hasFilter: Boolean
        get() = filterManager.getFilter(section).roomType != RoomFilterType.None ||
                filterAuthor != FilterAuthor() ||
                filterTags.isNotEmpty() ||
                filterProvider != null

    private val filters: Map<String, String>
        get() = mutableMapOf<String, String>().apply {
            put(ApiContract.Parameters.ARG_FILTER_BY_SUBJECT_ID, filterAuthor.id)
            if (!filterProvider?.storage?.filterValue.isNullOrEmpty()) {
                put(
                    ApiContract.Parameters.ARG_FILTER_BY_PROVIDER_ROOM,
                    filterProvider?.storage?.filterValue.orEmpty()
                )
            }
            if (filterType != RoomFilterType.None) {
                put(ApiContract.Parameters.ARG_FILTER_BY_TYPE_ROOM, filterType.filterVal.toString())
            }
            if (filterTags.isNotEmpty()) {
                put(ApiContract.Parameters.ARG_FILTER_BY_TAG_ROOM, filterTags.joinToString())
            }
        }

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun loadFilter() {
        val filter = filterManager.getFilter(section)
        filterType = filter.roomType
        filterAuthor = filter.author
        filterProvider = filter.provider?.let { FilterProvider(it) }
        filterTags = filter.tags

        getTags()
        getThirdParty()
    }

    private fun getThirdParty() {
        presenterScope.launch {
            val thirdPartyList = context.api
                .getThirdPartyList()
                .response
                .map(Thirdparty::providerKey)
            if (thirdPartyList.isEmpty()) return@launch

            val uniqueList = mutableListOf<String>()
            thirdPartyList.forEach { if (!uniqueList.contains(it) && it != null) uniqueList.add(it) }

            withContext(Dispatchers.Main) {
                viewState.onThirdPartyLoaded(uniqueList)
            }
        }
    }

    private fun getTags() {
        presenterScope.launch(Dispatchers.IO) {
            context.roomProvider.getTags()
                .onSuccess { tags ->
                    if (tags.isEmpty()) return@launch
                    withContext(Dispatchers.Main) {
                        viewState.onTagsLoaded(tags)
                    }
                }
                .onFailure { e -> fetchError(e) }
        }
    }

    override fun saveFilter() {
        filterManager.saveFilter(section) { filter ->
            filter.copy(
                roomType = filterType,
                author = filterAuthor,
                tags = filterTags,
                provider = filterProvider?.storage
            )
        }
    }

    override fun update(initialCall: Boolean) {
        saveFilter()
        viewState.updateViewState(isChanged = !initialCall)
        disposable?.clear()
        val apiCall = if (isTemplatesSection) {
            context.cloudFileProvider.getRoomTemplates(filters)
        } else {
            context.cloudFileProvider.getRooms(filters) {
                section == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM
            }
        }
        disposable?.add(
            apiCall.doOnSubscribe { viewState.onFilterProgress() }
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
        filterTags = emptyList()
        filterProvider = null
        viewState.onFilterReset()
    }

    fun clearAuthor() {
        filterAuthor = FilterAuthor()
    }
}