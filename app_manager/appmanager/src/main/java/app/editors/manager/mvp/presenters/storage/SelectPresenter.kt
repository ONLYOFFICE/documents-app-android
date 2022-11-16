package app.editors.manager.mvp.presenters.storage

import android.content.Context
import app.documents.core.storage.account.AccountDao
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.app.Api
import app.editors.manager.app.App
import app.editors.manager.app.api
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.account.Storage
import app.editors.manager.mvp.views.storage.SelectView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import moxy.MvpPresenter
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@InjectViewState
class SelectPresenter : MvpPresenter<SelectView>() {

    companion object {
        val TAG: String = SelectPresenter::class.java.simpleName
    }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var accountDao: AccountDao

    init {
        App.getApp().appComponent.inject(this)
    }

    private var disposable: Disposable? = null
    private var storageList: List<Storage>? = null

    private val api: Api = context.api()

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun getStorages() {
        viewState.showProgress(true)
        disposable = api.thirdpartyCapabilities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { responseBody: ResponseBody -> JSONObject(responseBody.string()) }
            .map { jsonObject: JSONObject -> collectListStorage(jsonObject) }
            .subscribe({ list: ArrayList<Storage> ->
                storageList = list
                viewState.onUpdate(getNames(list))
                viewState.showProgress(false)
            }, { error: Throwable -> viewState.onError(error.message) })
    }

    @Throws(JSONException::class)
    private fun collectListStorage(jsonObject: JSONObject): ArrayList<Storage> {
        val storages = ArrayList<Storage>()
        val nextcloud = Storage(name = ApiContract.Storage.NEXTCLOUD)
        val owncloud = Storage(name = ApiContract.Storage.OWNCLOUD)
        val kDrive = Storage(name = ApiContract.Storage.KDRIVE)
        val sharePoint = Storage(name = ApiContract.Storage.SHAREPOINT)
        val yandex = Storage(name = ApiContract.Storage.YANDEX)
        val webDav = Storage(name = ApiContract.Storage.WEBDAV)
        storages.add(nextcloud)
        storages.add(owncloud)
        storages.add(kDrive)
        storages.add(sharePoint)
        val array = jsonObject.getJSONArray("response")
        for (i in 0 until array.length()) {
            val arrayString = array.getJSONArray(i)
            if (arrayString.length() == 3) {
                val storage = Storage()
                for (j in 0 until arrayString.length()) {
                    when (j) {
                        0 -> {
                            storage.name = arrayString.getString(j)
                        }
                        1 -> {
                            storage.clientId = arrayString.getString(j)
                        }
                        2 -> {
                            storage.redirectUrl = arrayString.getString(j)
                        }
                    }
                }
                storages.add(storage)
            }
        }
        storages.add(yandex)
        storages.add(webDav)
        return storages
    }

    fun connect(providerKey: String) {
        var key = providerKey
        val storage = getStorage(key)
        if (storage?.clientId != null) {
            viewState.showWebTokenFragment(storage)
        } else {
            var url = ""
            var title = "WebDav service"
            when (key) {
                ApiContract.Storage.YANDEX -> {
                    url = StorageUtils.WevDav.URL_YANDEX
                    title = context.getString(R.string.storage_select_yandex)
                }
                ApiContract.Storage.KDRIVE -> {
                    url = StorageUtils.WevDav.URL_KDRIVE
                    title = context.getString(R.string.storage_select_kdrive)
                }
                ApiContract.Storage.SHAREPOINT -> {
                    title = context.getString(R.string.storage_select_share_point)
                    key = ApiContract.Storage.WEBDAV
                }
                ApiContract.Storage.OWNCLOUD -> {
                    title = context.getString(R.string.storage_select_own_cloud)
                    key = ApiContract.Storage.WEBDAV
                }
                ApiContract.Storage.NEXTCLOUD -> {
                    title = context.getString(R.string.storage_select_next_cloud)
                    key = ApiContract.Storage.WEBDAV
                }
            }
            viewState.showWebDavFragment(key, url, title)
        }
    }

    private fun getStorage(providerKey: String): Storage? {
        if (storageList != null) {
            for (storage in storageList!!) {
                if (storage.name == providerKey) {
                    return storage
                }
            }
        }
        return null
    }

    private fun getNames(list: ArrayList<Storage>): List<String> {
        val names: MutableList<String> = ArrayList()
        for (storage in list) {
            names.add(storage.name ?: "")
        }
        return names
    }
}