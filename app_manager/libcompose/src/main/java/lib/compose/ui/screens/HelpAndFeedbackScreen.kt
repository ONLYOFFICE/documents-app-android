package lib.compose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import lib.compose.ui.views.AppArrowItem
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.ActivitiesUtils

@Composable
fun HelpAndFeedbackScreen(
    modifier: Modifier = Modifier,
    showIconsAndDividers: Boolean = false,
    whatsNewContent: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        val context = LocalContext.current
        AppArrowItem(
            title = R.string.help_and_feedback_suggest_a_feature,
            startIcon = R.drawable.ic_suggest.takeIf { showIconsAndDividers },
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
            startIcon = R.drawable.ic_support.takeIf { showIconsAndDividers },
            dividerVisible = showIconsAndDividers,
            onClick = {
                ActivitiesUtils.sendFeedbackEmail(context, "")
            }
        )
        AppArrowItem(
            title = R.string.help_and_feedback_help_center,
            startIcon = R.drawable.ic_help.takeIf { showIconsAndDividers },
            dividerVisible = showIconsAndDividers,
            onClick = {
                ActivitiesUtils.showBrowser(
                    context = context,
                    url = "" // TODO: add link
                )
            }
        )
        whatsNewContent()
    }
}