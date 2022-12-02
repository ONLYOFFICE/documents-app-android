package app.editors.manager.mvp.presenters.main

import android.net.Uri
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.BuildConfig
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.CryptUtils
import moxy.InjectViewState
import moxy.presenterScope
import java.util.*
import javax.inject.Inject

@InjectViewState
class MainPagerPresenter(private val accountJson: String?) : BasePresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null

    private val api: Api = context.api()

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState(fileData: Uri? = null) {
        disposable = getPortalModules().subscribe({ sections ->
            viewState.onFinishRequest()
            accountJson?.let { account ->
                viewState.onRender(account, if (networkSetting.isDocSpace) {
                    sections?.filter { it.current.rootFolderType != ApiContract.SectionType.CLOUD_FAVORITES }
                } else {
                    sections
                })
                checkFileData(Json.decodeFromString(account), fileData)
            }
        }) { throwable: Throwable ->
            fetchError(throwable)
        }
    }


    private fun getPortalModules(): Observable<List<Explorer>?> {
        return api.getRootFolder(
            mapOf(ApiContract.Modules.FILTER_TYPE_HEADER to ApiContract.Modules.FILTER_TYPE_VALUE),
            mapOf(
                ApiContract.Modules.FLAG_SUBFOLDERS to false,
                ApiContract.Modules.FLAG_TRASH to false,
                ApiContract.Modules.FLAG_ADDFOLDERS to false
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { cloudTree ->
                val folderTypes = cloudTree.response.map { explorer -> explorer?.current?.rootFolderType }
                preferenceTool.setFavoritesEnable(folderTypes.contains(ApiContract.SectionType.CLOUD_FAVORITES))
                preferenceTool.isProjectDisable = !folderTypes.contains(ApiContract.SectionType.CLOUD_PROJECTS)
                return@map cloudTree.response.apply {
                    // My section
                    Collections.swap(this, this.indexOf(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_USER }), 0)
                    // Trash section
                    Collections.swap(
                        this,
                        this.indexOf(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_TRASH }),
                        this.lastIndex
                    )
                    //Rooms sections
                    if (this.contains(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM })) {
                        Collections.swap(
                            this,
                            this.indexOf(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_VIRTUAL_ROOM }),
                            1
                        )
                    }
                    if (this.contains(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM })) {
                        Collections.swap(
                            this,
                            this.indexOf(this.find { it.current?.rootFolderType == ApiContract.SectionType.CLOUD_ARCHIVE_ROOM }),
                            this.lastIndex - 1
                        )
                    }
                }
            }
    }

    private fun checkFileData(account: CloudAccount, fileData: Uri?) {
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
            if (dataModel.portal?.equals(account.portal, ignoreCase = true) == true && dataModel.email?.equals(
                    account.login,
                    ignoreCase = true
                ) == true
            ) {
                viewState.setFileData(Json.encodeToString(dataModel))
            } else {
                presenterScope.launch {
                    val isToken = checkAccountLogin(dataModel)
                    preferenceTool.fileData = Json.encodeToString(dataModel)
                    withContext(Dispatchers.Main) {
                        viewState.onSwitchAccount(dataModel, isToken)
                    }
                }
            }
        }
    }

    private suspend fun checkAccountLogin(data: OpenDataModel): Boolean {
        val account = accountDao.getAccountByLogin(data.email?.lowercase() ?: "")
        return account?.token != null && account.token.isNotEmpty()
    }

    fun onRemoveFileData() {
        preferenceTool.fileData = ""
    }
}