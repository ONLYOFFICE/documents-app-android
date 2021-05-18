package app.editors.manager.mvp.presenters.main

import android.net.Uri
import android.util.Base64
import app.documents.core.account.CloudAccount
import app.documents.core.network.ApiContract
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.managers.utils.FirebaseUtils.OnRatingApp
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainActivityView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import lib.toolkit.base.managers.utils.AccountUtils
import moxy.InjectViewState
import java.util.concurrent.TimeUnit

sealed class MainActivityState {
    object RecentState : MainActivityState()
    object OnDeviceState : MainActivityState()
    class CloudState(val account: CloudAccount? = null) : MainActivityState()
    class AccountsState(val isAccounts: Boolean = false) : MainActivityState()
}

@InjectViewState
class MainActivityPresenter : BasePresenter<MainActivityView>(), OnRatingApp {

    companion object {
        val TAG: String = MainActivityPresenter::class.java.simpleName
        const val TAG_DIALOG_REMOTE_PLAY_MARKET = "TAG_DIALOG_REMOTE_PLAY_MARKET"
        const val TAG_DIALOG_REMOTE_APP = "TAG_DIALOG_REMOTE_APP"
        const val TAG_DIALOG_RATE_FIRST = "TAG_DIALOG_RATE_FIRST"
        private const val TAG_DIALOG_RATE_SECOND = "TAG_DIALOG_RATE_SECOND"
        private const val TAG_DIALOG_RATE_FEEDBACK = "TAG_DIALOG_RATE_FEEDBACK"
        private const val DEFAULT_RATE_SESSIONS: Long = 5
    }

    init {
        App.getApp().appComponent.inject(this)
    }

    private val mDisposable = CompositeDisposable()

    private var cloudAccount: CloudAccount? = null
    private var reviewInfo: ReviewInfo? = null
    private var isAppColdStart = true

//    var isDialogOpen: Boolean = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.setUserSession()
        if (isAppColdStart) {
            isAppColdStart = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.dispose()
    }

    fun init() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                cloudAccount = it
                setNetworkSetting(it)
                withContext(Dispatchers.Main) {
                    checkToken(it)
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    viewState.onRender(MainActivityState.CloudState())
                }
            }
        }
    }

    private fun checkToken(cloudAccount: CloudAccount) {
        if (cloudAccount.isWebDav) {
            viewState.onRender(MainActivityState.CloudState(cloudAccount))
        } else {
            AccountUtils.getToken(
                context = context,
                accountName = cloudAccount.getAccountName()
            )?.let {
                viewState.onRender(MainActivityState.CloudState(cloudAccount))
            } ?: run {
                viewState.onRender(MainActivityState.CloudState())
            }
        }
    }

    private fun setNetworkSetting(cloudAccount: CloudAccount) {
        networkSettings.setBaseUrl(cloudAccount.portal ?: "")
        networkSettings.setScheme(cloudAccount.scheme ?: ApiContract.SCHEME_HTTPS)
        networkSettings.setSslState(cloudAccount.isSslState)
        networkSettings.setCipher(cloudAccount.isSslCiphers)
        networkSettings.serverVersion = cloudAccount.serverVersion
    }

    override fun onRatingApp(isRating: Boolean) {
        if (isRating) {
            if (preferenceTool.isRateOn && preferenceTool.userSession % DEFAULT_RATE_SESSIONS == 0L) {
                viewState.onRatingApp()
            }
        }
    }

    fun getRemoteConfigRate() {
        if (!BuildConfig.DEBUG) {
            mDisposable.add(
                Observable.just(1)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { FirebaseUtils.checkRatingConfig(this@MainActivityPresenter) })
        }
    }

    private fun getReviewInfo() {
        ReviewManagerFactory.create(context)
            .requestReviewFlow()
            .addOnCompleteListener { task: Task<ReviewInfo?> ->
                if (task.isSuccessful) {
                    reviewInfo = task.result
                }
            }
    }

    fun setAccount() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                cloudAccount = it
            }
        }
    }

    fun onAcceptClick(value: String?, tag: String?) {
        tag?.let {
            when (tag) {
                TAG_DIALOG_REMOTE_PLAY_MARKET -> {
                    viewState.onShowPlayMarket(BuildConfig.RELEASE_ID)
                    viewState.onDialogClose()
                }
                TAG_DIALOG_REMOTE_APP -> {
                    viewState.onShowApp(BuildConfig.RELEASE_ID)
                    viewState.onDialogClose()
                }
                TAG_DIALOG_RATE_FIRST -> {
                    getReviewInfo()
                    viewState.onQuestionDialog(
                        context.getString(R.string.dialogs_question_rate_second_info), TAG_DIALOG_RATE_SECOND,
                        context.getString(R.string.dialogs_question_accept_sure),
                        context.getString(R.string.dialogs_question_accept_no_thanks), null
                    )
                }
                TAG_DIALOG_RATE_SECOND -> {
                    preferenceTool.isRateOn = false
                    if (reviewInfo != null) {
                        viewState.onShowInAppReview(reviewInfo!!)
                    } else {
                        viewState.onShowPlayMarket(BuildConfig.RELEASE_ID)
                    }
                    viewState.onDialogClose()
                }
                TAG_DIALOG_RATE_FEEDBACK -> {
                    if (value != null) {
                        viewState.onShowEmailClientTemplate(value)
                    }
                    viewState.onDialogClose()
                }
            }
        }
    }

    fun onCancelClick(tag: String?) {
        tag?.let {
            when (tag) {
                TAG_DIALOG_RATE_FIRST -> {
                    preferenceTool.isRateOn = false
                    viewState.onShowEditMultilineDialog(
                        context.getString(R.string.dialogs_edit_feedback_rate_title),
                        context.getString(R.string.dialogs_edit_feedback_rate_hint),
                        context.getString(R.string.dialogs_edit_feedback_rate_accept),
                        context.getString(R.string.dialogs_question_accept_no_thanks), TAG_DIALOG_RATE_FEEDBACK
                    )
                    return
                }
                TAG_DIALOG_RATE_SECOND -> {
                    preferenceTool.isRateOn = false
                    viewState.onDialogClose()
                }
            }
        }
    }

    fun checkOnBoarding() {
        if (!preferenceTool.onBoarding) {
            viewState.onShowOnBoarding()
        }
    }

    fun navigationItemClick(itemId: Int) {
        when (itemId) {
            R.id.menu_item_recent -> viewState.onRender(MainActivityState.RecentState)
            R.id.menu_item_cloud -> {
                CoroutineScope(Dispatchers.Default).launch {
                    accountDao.getAccountOnline()?.let {
                        cloudAccount = it
                    } ?: run {
                        cloudAccount = null
                    }
                    withContext(Dispatchers.Main) {
                        viewState.onRender(MainActivityState.CloudState(cloudAccount))
                    }
                }
            }
            R.id.menu_item_on_device -> viewState.onRender(
                MainActivityState.OnDeviceState
            )
            R.id.menu_item_setting -> {
                CoroutineScope(Dispatchers.Default).launch {
                    if (accountDao.getAccounts().isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            viewState.onRender(MainActivityState.AccountsState(true))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            viewState.onRender(MainActivityState.AccountsState(false))
                        }

                    }
                }
            }
        }
    }

    fun clear() {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let {
                accountDao.updateAccount(
                    it.copy(
                        isOnline = false
                    )
                )
            }
            networkSettings.setDefault()
            withContext(Dispatchers.Main) {
                viewState.onRender(MainActivityState.CloudState())
            }
        }
    }

    fun checkFileData(fileData: Uri) {
        CoroutineScope(Dispatchers.Default).launch {
            accountDao.getAccountOnline()?.let { account ->
                Json.decodeFromString<OpenDataModel>(decodeUri(fileData.query)).let { data ->
                    withContext(Dispatchers.Main) {
                        viewState.openFile(Json.encodeToString(data))
                    }
//                    if (data.portal?.equals(account.portal) == true && data.email?.equals(account.login) == true) {
//                        withContext(Dispatchers.Main) {
//                            viewState.openFile(Json.encodeToString(data))
//                        }
//                    } else {
//                        withContext(Dispatchers.Main) {
//                            viewState.onError(context.getString(R.string.error_recent_enter_account))
//                        }
//                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    viewState.onError(context.getString(R.string.error_recent_enter_account))
                }
            }
        }
    }

    private fun decodeUri(path: String?): String {
        path?.let { string ->
            return if (string.contains("data=")) {
                val buffer = Base64.decode(string.replace("data=", ""), Base64.DEFAULT)
                String(buffer, charset("utf-8"))
            } else {
                val buffer = Base64.decode(string, Base64.DEFAULT)
                String(buffer, charset("utf-8"))
            }
        }?: run {
            return ""
        }
    }

}