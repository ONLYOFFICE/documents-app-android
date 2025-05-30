package app.editors.manager.mvp.presenters.main

import android.os.Build
import app.documents.core.account.AccountPreferences
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.app.appComponent
import app.editors.manager.mvp.models.filter.Filter
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.main.MainActivityView
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lib.toolkit.base.managers.utils.FileUtils
import moxy.InjectViewState
import moxy.presenterScope
import javax.inject.Inject

@InjectViewState
class MainActivityPresenter : BasePresenter<MainActivityView>() {

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

    @Inject
    lateinit var accountPreferences: AccountPreferences

    private val disposable = CompositeDisposable()

    private var reviewInfo: ReviewInfo? = null
    private var isAppColdStart = true

    var isDialogOpen: Boolean = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        preferenceTool.setUserSession()
        preferenceTool.filter = Filter()
        checkSdk()
    }

    private fun checkSdk() {
        presenterScope.launch(Dispatchers.IO) {
            val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }

            if (preferenceTool.appVersion != BuildConfig.VERSION_NAME + "." + version) {
                context.externalCacheDir?.let(FileUtils::deletePath)
                context.cacheDir?.let(FileUtils::deletePath)
            }
            preferenceTool.appVersion = BuildConfig.VERSION_NAME + "." + version
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    fun init(isPortal: Boolean = false) {
        presenterScope.launch {
            context.accountOnline?.let { account ->
                if (isAppColdStart) {
                    App.getApp().refreshLoginComponent(account.portal)

                    App.getApp()
                        .loginComponent
                        .cloudLoginRepository
                        .updateCloudAccount { isDowngradeToGuest ->
                            if (isDowngradeToGuest) {
                                viewState.onDowngradeToGuestDialog()
                            }
                        }

                    isAppColdStart = false
                }
            }
        }
        checkAppLocale()
    }

    private fun checkAppLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.appComponent.appLocaleHelper.checkAppLocale(true)) {
                viewState.onLocaleConfirmation()
            }
        }
    }

    fun getRemoteConfigRate() {
        if (!BuildConfig.DEBUG) {
            if (preferenceTool.isRateOn && preferenceTool.userSession % DEFAULT_RATE_SESSIONS == 0L) {
                viewState.onRatingApp()
            }
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
                    viewState.onDialogClose()
                    reviewInfo?.let {
                        viewState.onShowInAppReview(it)
                    } ?: run {
                        viewState.onShowPlayMarket(BuildConfig.RELEASE_ID)
                    }
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
                    viewState.onDialogClose()
                    viewState.onShowEditMultilineDialog(
                        context.getString(R.string.dialogs_edit_feedback_rate_title),
                        context.getString(R.string.dialogs_edit_feedback_rate_hint),
                        context.getString(R.string.dialogs_edit_feedback_rate_accept),
                        context.getString(R.string.dialogs_question_accept_no_thanks), TAG_DIALOG_RATE_FEEDBACK
                    )
                }

                TAG_DIALOG_RATE_SECOND -> {
                    preferenceTool.isRateOn = false
                    viewState.onDialogClose()
                }
            }
        }
    }

    fun clear() {
        accountPreferences.onlineAccountId = null
    }

    fun onRateOff() {
        preferenceTool.isRateOn = false
    }

    fun checkOnBoardingShowed() {
        if (!preferenceTool.onBoarding) {
            viewState.onShowOnBoarding()
        }
    }
}