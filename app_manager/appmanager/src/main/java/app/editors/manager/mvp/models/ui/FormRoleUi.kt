package app.editors.manager.mvp.models.ui

import android.content.Context
import app.documents.core.model.login.User
import app.documents.core.network.manager.models.explorer.FormRole
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

        const val FORM_ROLE_HISTORY_OPEN = 0
        const val FORM_ROLE_HISTORY_SUBMIT = 1
        const val FORM_ROLE_HISTORY_COMPLETE = 2

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
            return when (event) {
                FORM_ROLE_HISTORY_OPEN -> context.getString(R.string.filling_form_history_opened)
                FORM_ROLE_HISTORY_SUBMIT -> context.getString(R.string.filling_form_history_submitted)
                FORM_ROLE_HISTORY_COMPLETE -> if (stoppedBy != null) {
                    context.getString(
                        R.string.filling_form_history_stopped,
                        stoppedBy.displayNameFromHtml
                    )
                } else {
                    context.getString(R.string.filling_form_history_complete)
                }

                else -> ""
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
        history = history?.mapNotNull { entry ->
            if (stoppedBy != null &&
                entry.key.toInt() != FormRoleHistory.FORM_ROLE_HISTORY_COMPLETE
            ) {
                return@mapNotNull null
            }
            FormRoleHistory.get(entry, stoppedBy, context)
        },
        roleName = roleName,
        roleColor = roleColor,
        roleStatus = FormRoleStatus.get(roleStatus),
        sequence = sequence,
        submitted = submitted,
        stoppedBy = stoppedBy,
        user = user
    )
}