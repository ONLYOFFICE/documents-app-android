package app.editors.manager.mvp.presenters.main

import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.documents.core.settings.NetworkSettings
import app.editors.manager.app.App
import app.editors.manager.mvp.views.main.MainPagerView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

sealed class MainPagerState {
    class VisitorState(val account: String, val version: Int) : MainPagerState()
    class PersonalState(val account: String, val version: Int) : MainPagerState()
    class CloudState(val account: String, val version: Int) : MainPagerState()
}

@InjectViewState
class MainPagerPresenter : MvpPresenter<MainPagerView>() {

    @Inject
    lateinit var networkSetting: NetworkSettings

    init {
        App.getApp().appComponent.inject(this)
    }

    fun getState(account: String?) {
        account?.let { jsonAccount ->
            Json.decodeFromString<CloudAccount>(jsonAccount).let { cloudAccount ->
                when {
                    networkSetting.getPortal().contains(ApiContract.PERSONAL_HOST) -> {
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
        } ?: run {
            throw Exception("Need account")
        }
    }
}