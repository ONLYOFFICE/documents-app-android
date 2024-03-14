package app.editors.manager.mvp.presenters.main

import android.net.Uri
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.ManagerService
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.response.ResponseCloudTree
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
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.CryptUtils
import lib.toolkit.base.managers.utils.JsonUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.util.Collections

@InjectViewState
class MainPagerPresenter(private val accountJson: String?) : BasePresenter<MainPagerView>() {

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    private val api: ManagerService = context.api

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState(fileData: Uri? = null) {
        presenterScope.launch(Dispatchers.IO) {
            val sections = getPortalModules()
            val data = if (context.accountOnline.isDocSpace) {
                sections.filter { it.current.rootFolderType != ApiContract.SectionType.CLOUD_FAVORITES }
            } else {
                sections
            }
            accountJson?.let { checkFileData(Json.decodeFromString(accountJson), fileData) }
            withContext(Dispatchers.Main) {
                viewState.onFinishRequest()
                viewState.onRender(accountJson!!, data)
            }
        }
    }

    private suspend fun getPortalModules(): List<Explorer> {
        try {
            val response = if (preferenceTool.modules.isNotEmpty()) {
                JsonUtils.jsonToObject(preferenceTool.modules, ResponseCloudTree::class.java)
            } else {
                api.getRootFolder(
                    mapOf(ApiContract.Modules.FILTER_TYPE_HEADER to ApiContract.Modules.FILTER_TYPE_VALUE),
                    mapOf(
                        ApiContract.Modules.FLAG_SUBFOLDERS to false,
                        ApiContract.Modules.FLAG_TRASH to false,
                        ApiContract.Modules.FLAG_ADDFOLDERS to false
                    )
                )
            }

            preferenceTool.modules = JsonUtils.objectToJson(response)

            val folderTypes = response.response.map { explorer -> explorer.current.rootFolderType }
            preferenceTool.setFavoritesEnable(folderTypes.contains(ApiContract.SectionType.CLOUD_FAVORITES))
            preferenceTool.isProjectDisable = !folderTypes.contains(ApiContract.SectionType.CLOUD_PROJECTS)

            return response.response.apply {
                if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_USER })) {
                    Collections.swap(
                        this,
                        indexOf(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_USER }),
                        0
                    )
                }
                // Trash section
                if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_TRASH })) {
                    Collections.swap(
                        this,
                        indexOf(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_TRASH }),
                        lastIndex
                    )
                }

                //Rooms sections
                if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM })) {
                    val position =
                        if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_USER })) {
                            1
                        } else {
                            0
                        }
                    Collections.swap(
                        this,
                        indexOf(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM }),
                        position
                    )
                }
                if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM })) {
                    val position =
                        if (contains(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_TRASH })) {
                            lastIndex - 1
                        } else {
                            lastIndex
                        }
                    Collections.swap(
                        this,
                        indexOf(find { it.current.rootFolderType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM }),
                        position
                    )
                }
            }
        } catch (error: Throwable) {
            fetchError(error)
            return emptyList()
        }
    }

    private suspend fun checkFileData(account: CloudAccount, fileData: Uri?) {
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
            if (dataModel.getPortalWithoutScheme()?.equals(
                    account.portal.url,
                    ignoreCase = true
                ) == true && dataModel.email?.equals(
                    account.login,
                    ignoreCase = true
                ) == true
            ) {
                viewState.setFileData(Json.encodeToString(dataModel))
            } else {
                val isToken = checkAccountLogin(dataModel)
                preferenceTool.fileData = Json.encodeToString(dataModel)
                withContext(Dispatchers.Main) {
                    viewState.onSwitchAccount(dataModel, isToken)
                }

            }
        }
    }

    private suspend fun checkAccountLogin(data: OpenDataModel): Boolean {
        val account = cloudDataSource.getAccountByLogin(data.email?.lowercase() ?: "")
        val token = AccountUtils.getToken(context, account?.accountName.orEmpty())
        return !token.isNullOrEmpty()
    }

    fun onRemoveFileData() {
        preferenceTool.fileData = ""
    }
}