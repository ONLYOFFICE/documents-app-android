package app.editors.manager.mvp.presenters.main

import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lib.toolkit.base.managers.utils.TimeUtils
import moxy.InjectViewState
import moxy.presenterScope

@InjectViewState
class MainPagerPresenter : BasePresenter<MainPagerView>() {

    companion object {

        const val PERSONAL_DUE_DATE = "01 09 2024"
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    private val api: ManagerService = context.api
    private val account = context.accountOnline

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState() {

        if (account?.isPersonal() == true && TimeUtils.isDateAfter(PERSONAL_DUE_DATE)) {
            viewState.onPersonalPortalEnd()
            return
        }

        presenterScope.launch(Dispatchers.IO) {
            getPortalModules().onSuccess { sections ->
                val data = if (context.accountOnline.isDocSpace) {
                    sections.filter { it.current.rootFolderType != ApiContract.SectionType.CLOUD_FAVORITES }
                } else {
                    sections
                }
                withContext(Dispatchers.Main) {
                    viewState.onFinishRequest()
                    viewState.onRender(data)
                }
            }.onFailure { error ->
                withContext(Dispatchers.Main) { fetchError(error) }
            }
        }
    }

    private suspend fun getPortalModules(): Result<List<Explorer>> {
        return runCatching {
            val response = api.getRootFolder(
                mapOf(ApiContract.Modules.FILTER_TYPE_HEADER to ApiContract.Modules.FILTER_TYPE_VALUE),
                mapOf(
                    ApiContract.Modules.FLAG_SUBFOLDERS to false,
                    ApiContract.Modules.FLAG_TRASH to false,
                    ApiContract.Modules.FLAG_ADDFOLDERS to false
                )
            )
            withContext(Dispatchers.Default) {
                val folderTypes = response.response.map { explorer -> explorer.current.rootFolderType }
                preferenceTool.setFavoritesEnable(folderTypes.contains(ApiContract.SectionType.CLOUD_FAVORITES))
                preferenceTool.isProjectDisable = !folderTypes.contains(ApiContract.SectionType.CLOUD_PROJECTS)

                return@withContext sortSections(response.response)
            }
        }
    }

    private fun sortSections(response: List<Explorer>): List<Explorer> {
        val sortedList = mutableListOf<Explorer>()

        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_USER }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_SHARE }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_FAVORITES }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_COMMON }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_PROJECTS }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM }?.let {
            sortedList.add(it)
        }
        response.firstOrNull { it.current.rootFolderType == ApiContract.SectionType.CLOUD_TRASH }?.let {
            sortedList.add(it)
        }
        return sortedList.toList()
    }

    fun onRemoveFileData() {
        preferenceTool.fileData = ""
    }
}