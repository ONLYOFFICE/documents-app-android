package app.editors.manager.mvp.models.ui

import android.content.Context
import app.documents.core.model.cloud.FormRole
import app.documents.core.model.login.User
import app.documents.core.utils.displayNameFromHtml
import app.editors.manager.R
import kotlinx.serialization.Serializable
import lib.toolkit.base.managers.utils.TimeUtils

sealed interface FormRoleStatus {
    data object Waiting : FormRoleStatus
    data object YourTurn : FormRoleStatus
    data object Filling : FormRoleStatus
    data object Filled : FormRoleStatus
    data object Stopped : FormRoleStatus

    companion object {

        fun get(value: Int): FormRoleStatus {
            return when (value) {
                1 -> Waiting
                2 -> YourTurn
                3 -> Filling
                4 -> Filled
                5 -> Stopped
                else -> Waiting
            }
        }
    }
}

@Serializable
data class FormRoleHistory(
    val message: String,
    val date: String
) {

    companion object {

        fun get(
            entry: Map.Entry<String, String>,
            stoppedBy: User?,
            context: Context
        ): FormRoleHistory {
            val (event, date) = entry
            return FormRoleHistory(
                message = getMessage(event.toInt(), stoppedBy, context),
                date = TimeUtils.formatDate(TimeUtils.parseDate(date))
            )
        }

        private fun getMessage(event: Int, stoppedBy: User?, context: Context): String {
            return if (stoppedBy != null) {
                context.getString(
                    R.string.filling_form_history_stopped,
                    stoppedBy.displayNameFromHtml
                )
            } else {
                context.getString(
                    when (event) {
                        0 -> R.string.filling_form_history_opened
                        1 -> R.string.filling_form_history_submitted
                        2 -> R.string.filling_form_history_complete
                        else -> return ""
                    }
                )
            }
        }
    }
}

data class FormRoleUi(
    val roleName: String,
    val roleColor: String,
    val roleStatus: FormRoleStatus,
    val sequence: Int,
    val submitted: Boolean,
    val history: List<FormRoleHistory>?,
    val user: User,
    val stoppedBy: User?
)

fun FormRole.toUi(context: Context): FormRoleUi {
    return FormRoleUi(
        history = history?.map { entry -> FormRoleHistory.get(entry, stoppedBy, context) },
        roleName = roleName,
        roleColor = roleColor,
        roleStatus = FormRoleStatus.get(roleStatus),
        sequence = sequence,
        submitted = submitted,
        stoppedBy = stoppedBy,
        user = user
    )
}