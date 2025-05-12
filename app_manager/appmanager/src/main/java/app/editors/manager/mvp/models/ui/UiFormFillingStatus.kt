package app.editors.manager.mvp.models.ui

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import lib.toolkit.base.R.color as RColor


sealed class UiFormFillingStatus(
    @StringRes val textRes: Int,
    @ColorRes val colorRes: Int
) {
    data object None : UiFormFillingStatus(0, 0)
    data object Draft : UiFormFillingStatus(R.string.form_status_draft, RColor.colorFormDraft)
    data object YourTurn : UiFormFillingStatus(R.string.form_status_your_turn, RColor.colorFormYourTurn)
    data object InProgress : UiFormFillingStatus(R.string.form_status_in_progress, RColor.colorFormInProgress)
    data object Complete : UiFormFillingStatus(R.string.form_status_complete, RColor.colorFormComplete)
    data object Stopped : UiFormFillingStatus(R.string.form_status_stopped, RColor.colorFormStopped)

    companion object {
        fun from(status: ApiContract.FormFillingStatus): UiFormFillingStatus = when (status) {
            is ApiContract.FormFillingStatus.None -> None
            is ApiContract.FormFillingStatus.Draft -> Draft
            is ApiContract.FormFillingStatus.YourTurn -> YourTurn
            is ApiContract.FormFillingStatus.InProgress -> InProgress
            is ApiContract.FormFillingStatus.Complete -> Complete
            is ApiContract.FormFillingStatus.Stopped -> Stopped
        }
    }
}