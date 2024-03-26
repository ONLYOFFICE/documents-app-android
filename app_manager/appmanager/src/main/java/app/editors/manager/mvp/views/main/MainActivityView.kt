package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.mvp.models.models.OpenDataModel
import app.editors.manager.mvp.presenters.main.MainActivityState
import app.editors.manager.mvp.views.base.BaseViewExt
import com.google.android.play.core.review.ReviewInfo
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface MainActivityView : BaseViewExt {
    fun onDialogClose()
    fun onRemotePlayMarket(@StringRes title: Int, @StringRes info: Int, @StringRes accept: Int, @StringRes cancel: Int)
    fun onRemoteApp(@StringRes title: Int, @StringRes info: Int, @StringRes accept: Int, @StringRes cancel: Int)
    fun onRatingApp()
    fun onQuestionDialog(title: String, tag: String, accept: String, cancel: String, question: String?)
    fun onShowEditMultilineDialog(title: String, hint: String, accept: String, cancel: String, tag: String)
    fun onShowPlayMarket(releaseId: String)
    fun onShowInAppReview(reviewInfo: ReviewInfo)
    fun onShowApp(releaseId: String)
    fun onShowEmailClientTemplate(value: String)
    fun onShowOnBoarding()
    fun onCloseActionDialog()
    fun onRender(state: MainActivityState)
    fun openFile(account: CloudAccount, fileData: String)
    fun onCodeActivity()
    fun onSwitchAccount(data: OpenDataModel, isToken: Boolean = false)
    fun onLocaleConfirmation()
}