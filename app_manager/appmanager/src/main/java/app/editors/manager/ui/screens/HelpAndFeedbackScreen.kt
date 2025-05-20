package app.editors.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.editors.manager.R
import lib.compose.ui.views.AppArrowItem
import lib.toolkit.base.managers.utils.ActivitiesUtils

@Composable
fun HelpAndFeedbackScreen(
    modifier: Modifier = Modifier,
    showIconsAndDividers: Boolean = false,
    onWhatsNewClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        val context = LocalContext.current
        AppArrowItem(
            title = R.string.help_and_feedback_suggest_a_feature,
            startIcon = lib.toolkit.base.R.drawable.ic_suggest.takeIf { showIconsAndDividers },
            dividerVisible = showIconsAndDividers,
            onClick = {
                ActivitiesUtils.showBrowser(
                    context = context,
                    url = "" // TODO: add link
                )
            }
        )
        AppArrowItem(
            title = R.string.help_and_feedback_contact_support,
            startIcon = lib.editors.gbase.R.drawable.ic_support.takeIf { showIconsAndDividers },
            dividerVisible = showIconsAndDividers,
            onClick = {
                ActivitiesUtils.sendFeedbackEmail(context, "")
            }
        )
        AppArrowItem(
            title = R.string.help_and_feedback_help_center,
            startIcon = lib.editors.gbase.R.drawable.ic_help.takeIf { showIconsAndDividers },
            dividerVisible = showIconsAndDividers,
            onClick = {
                ActivitiesUtils.showBrowser(
                    context = context,
                    url = "" // TODO: add link
                )
            }
        )
        onWhatsNewClick?.let { callback ->
            AppArrowItem(
                title = R.string.whats_new_title,
                dividerVisible = false,
                onClick = callback
            )
        }
    }
}