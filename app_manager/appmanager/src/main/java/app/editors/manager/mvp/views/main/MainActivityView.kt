package app.editors.manager.mvp.views.main

import androidx.annotation.StringRes
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
    fun onLocaleConfirmation()
}