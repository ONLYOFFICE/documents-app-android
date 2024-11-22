package app.editors.manager.mvp.presenters.main

import android.net.Uri
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.CryptUtils
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

    fun getState(fileData: Uri? = null) {

        if (account?.isPersonal() == true && TimeUtils.isDateAfter(PERSONAL_DUE_DATE)) {
            viewState.onPersonalPortalEnd()
            return
        }

        presenterScope.launch(Dispatchers.IO) {
            val sections = getPortalModules()
            val data = if (context.accountOnline.isDocSpace) {
                sections.filter { it.current.rootFolderType != ApiContract.SectionType.CLOUD_FAVORITES }
            } else {
                sections
            }
            checkFileData(fileData)
            withContext(Dispatchers.Main) {
                viewState.onFinishRequest()
                viewState.onRender(data)
            }
        }
    }

    private suspend fun getPortalModules(): List<Explorer> {
        try {
            val response = api.getRootFolder(
                mapOf(ApiContract.Modules.FILTER_TYPE_HEADER to ApiContract.Modules.FILTER_TYPE_VALUE),
                mapOf(
                    ApiContract.Modules.FLAG_SUBFOLDERS to false,
                    ApiContract.Modules.FLAG_TRASH to false,
                    ApiContract.Modules.FLAG_ADDFOLDERS to false
                )
            )
            return withContext(Dispatchers.Default) {
                val folderTypes = response.response.map { explorer -> explorer.current.rootFolderType }
                preferenceTool.setFavoritesEnable(folderTypes.contains(ApiContract.SectionType.CLOUD_FAVORITES))
                preferenceTool.isProjectDisable = !folderTypes.contains(ApiContract.SectionType.CLOUD_PROJECTS)

                return@withContext sortSections(response.response)
            }
        } catch (error: Throwable) {
            withContext(Dispatchers.Main) {
                fetchError(error)
            }
            return emptyList()
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

    suspend fun checkFileData(fileData: Uri?) {
        if ((fileData?.scheme?.equals(BuildConfig.PUSH_SCHEME) == true && fileData.host.equals("openfile")) || preferenceTool.fileData.isNotEmpty()) {
            if (fileData?.queryParameterNames?.contains("push") == true) {
                viewState.setFileData(fileData.getQueryParameter("data") ?: "")
                return
            }
            val dataModel: OpenDataModel = if (preferenceTool.fileData.isNotEmpty()) {
                Json.decodeFromString(preferenceTool.fileData)
            } else {
                Json.decodeFromString(CryptUtils.decodeUri(fileData?.query))
            }
            preferenceTool.fileData = ""
            if (dataModel.share.isNotEmpty()) {
                viewState.setFileData(Json.encodeToString(dataModel))
                return
            }
            if (dataModel.getPortalWithoutScheme()?.equals(
                    account?.portal?.url,
                    ignoreCase = true
                ) == true && dataModel.email?.equals(
                    account?.login,
                    ignoreCase = true
                ) == true
            ) {
                viewState.setFileData(Json.encodeToString(dataModel))
            } else {
                preferenceTool.fileData = Json.encodeToString(dataModel)
                withContext(Dispatchers.Main) {
                    viewState.onSwitchAccount(dataModel)
                }

            }
        }
    }

    fun onRemoveFileData() {
        preferenceTool.fileData = ""
    }
}