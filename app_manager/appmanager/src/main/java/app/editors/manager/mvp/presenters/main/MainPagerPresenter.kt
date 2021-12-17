package app.editors.manager.mvp.presenters.main

import android.net.Uri
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.managers.utils.Constants
import app.editors.manager.mvp.models.explorer.Explorer
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainPagerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.CryptUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TabFragmentDictionary
import moxy.InjectViewState
import java.util.*
import javax.inject.Inject

sealed class MainPagerState {
    class VisitorState(val account: String, val version: Int) : MainPagerState()
    class PersonalState(val account: String, val version: Int) : MainPagerState()
    class CloudState(val account: String, val version: Int) : MainPagerState()
}

@InjectViewState
class MainPagerPresenter(private val accountJson: String?) : BasePresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null
    private var sections: List<Explorer>? = null

    private val api: Api = context.api()

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getState(fileData: Uri? = null) {
        if (StringUtils.convertServerVersion(networkSetting.serverVersion) < 11 || networkSetting.getBaseUrl()
                .contains(ApiContract.PERSONAL_SUBDOMAIN)
        ) {
            accountJson?.let { jsonAccount ->
                viewState.onFinishRequest()
                Json.decodeFromString<CloudAccount>(jsonAccount).let { cloudAccount ->
                    render(cloudAccount, jsonAccount)
                    checkFileData(cloudAccount, fileData)
                }
            } ?: run {
                throw Exception("Need account")
            }
        } else {
            disposable = getPortalModules().subscribe({
                viewState.onFinishRequest()
                accountJson?.let { account ->
                    viewState.onRender(account, sections)
                }
            }) { throwable: Throwable -> fetchError(throwable) }
        }
    }

    private fun render(cloudAccount: CloudAccount, jsonAccount: String) {
        when {
            networkSetting.getPortal().contains(ApiContract.PERSONAL_SUBDOMAIN) -> {
                viewState.onRender(
                    MainPagerState.PersonalState(
                        jsonAccount,
                        StringUtils.convertServerVersion(
                            networkSetting.serverVersion
                        )
                    )
                )


            }
            cloudAccount.isVisitor -> {
                viewState.onRender(
                    MainPagerState.VisitorState(
                        jsonAccount,
                        StringUtils.convertServerVersion(
                            networkSetting.serverVersion
                        )
                    )
                )


            }
            else -> {
                viewState.onRender(
                    MainPagerState.CloudState(
                        jsonAccount,
                        StringUtils.convertServerVersion(networkSetting.serverVersion)
                    )
                )
            }
        }
    }

    private fun getPortalModules(): Observable<Boolean> {
        return Observable.zip(
            api.getRootFolder(
                mapOf("filterType" to 2),
                mapOf(
                    "withsubfolders" to false,
                    "withoutTrash" to false,
                    "withoutAdditionalFolder" to false
                )
            ), api.getModules(listOf(Constants.Modules.PROJECT_ID))
        ) { cloudTree, modules ->
            if (cloudTree.response != null && modules.response != null) {
                preferenceTool.isProjectDisable = !modules.response[0].isEnable
                sections = cloudTree.response
                for (folder in cloudTree.response) {
                    if (TabFragmentDictionary.Favorites.contains(folder.current.title)) {
                        preferenceTool.setFavoritesEnable(true)
                        break
                    } else {
                        preferenceTool.setFavoritesEnable(false)
                    }
                }
            }
            return@zip true
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun checkFileData(account: CloudAccount, fileData: Uri?) {
        fileData?.let { data ->
            if (data.scheme?.equals("oodocuments") == true && data.host.equals("openfile")) {
                val dataModel = Json.decodeFromString<OpenDataModel>(CryptUtils.decodeUri(data.query))
                if (dataModel.portal?.equals(account.portal, ignoreCase = true) == true && dataModel.email?.equals(
                        account.login,
                        ignoreCase = true
                    ) == true
                ) {
                    viewState.setFileData(Json.encodeToString(dataModel))
                } else {
                    viewState.onError(R.string.error_recent_enter_account)
                }
            }
        }
    }
}