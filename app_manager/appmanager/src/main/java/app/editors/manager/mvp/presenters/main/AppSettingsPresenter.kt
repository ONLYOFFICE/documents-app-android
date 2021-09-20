package app.editors.manager.mvp.presenters.main

import android.annotation.SuppressLint
import android.content.Context
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.mvp.views.main.AppSettingsView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.StringUtils
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class AppSettingsPresenter : MvpPresenter<AppSettingsView>() {

    companion object {
        private const val EMPTY = "0"
    }

    @Inject
    lateinit var context: Context
    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var disposable: Disposable? = null

    init {
        App.getApp().appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getCacheSize() {
        val cacheDir = context.externalCacheDir
        val internalCache = context.cacheDir
        if (cacheDir != null) {
            disposable = Single.just(FileUtils.getSize(cacheDir.absoluteFile))
                .observeOn(AndroidSchedulers.mainThread())
                .map { aLong: Long -> aLong + FileUtils.getSize(internalCache.absoluteFile) }
                .map { size: Long? ->
                    StringUtils.getFormattedSize(context, size!!)
                }
                .subscribe { s: String? -> viewState!!.onSetCacheSize(s) }
        } else {
            viewState.onSetCacheSize(EMPTY)
        }
    }


    fun initWifiState() {
        viewState.onSetWifiState(preferenceTool.uploadWifiState)
    }

    fun setWifiState(isChecked: Boolean) {
        preferenceTool.setWifiState(isChecked)
    }

    @SuppressLint("MissingPermission")
    fun clearCache() {
        FileUtils.clearCacheDir(context)
        FileUtils.clearCacheDir(context, false)

        getCacheSize()
        viewState.onMessage(context.getString(R.string.setting_cache_cleared))
    }

    fun setAnalyticState(isEnable: Boolean) {
        App.getApp().isAnalyticEnable = isEnable
        preferenceTool.isAnalyticEnable = isEnable
    }

    fun initAnalyticState() {
        viewState.onAnalyticState(preferenceTool.isAnalyticEnable)
    }

}