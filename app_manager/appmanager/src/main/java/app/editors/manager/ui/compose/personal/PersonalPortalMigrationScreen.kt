package app.editors.manager.ui.compose.personal

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.editors.manager.R
import app.editors.manager.ui.activities.login.SignInActivity
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.UiUtils


@Composable
fun PersonalMigrationScreen(onClose: (() -> Unit)? = null) {
    AppScaffold(
        useTablePaddings = false,
        topBar = {
            onClose?.let {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    elevation = 0.dp,
                    actions = {

                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = lib.toolkit.base.R.drawable.ic_close),
                                contentDescription = null
                            )
                        }
                    },
                    title = {}
                )
            }
        }
    ) {
        val context = LocalContext.current
        if (UiUtils.isLandscape(context) && !UiUtils.isTablet(context)) {
            Row(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(all = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageBlock(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextBlock()
                    AppButton(
                        title = stringResource(id = R.string.personal_portal_end_button),
                        onClick = { SignInActivity.showPortalCreate(context) }
                    )
                }
            }
        } else {
            NestedColumn(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(all = 16.dp)
                    .width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImageBlock()
                TextBlock()
                AppButton(
                    modifier = Modifier.padding(top = 32.dp),
                    title = stringResource(id = R.string.personal_portal_end_button),
                    onClick = { SignInActivity.showPortalCreate(context) }
                )
            }
        }
    }
}

@Composable
private fun ImageBlock(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier
            .padding(bottom = 24.dp)
            .width(IntrinsicSize.Max),
        imageVector = ImageVector.vectorResource(id = R.drawable.image_personal_to_docspace),
        contentDescription = null
    )
}

@Composable
private fun TextBlock(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .padding(horizontal = 32.dp)
                .padding(bottom = 24.dp)
        ) {

            val annotatedString = buildAnnotatedString {
                appendInlineContent(id = "logo")
                append(" ")
                append(stringResource(id = R.string.personal_portal_end_title))
            }
            val inlineContentMap = mapOf(
                "logo" to InlineTextContent(
                    Placeholder(24.sp, 24.sp, PlaceholderVerticalAlign.TextCenter)
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_storage_onlyoffice),
                        contentDescription = null
                    )
                }
            )
            Text(
                text = annotatedString,
                inlineContent = inlineContentMap,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
        }
        Text(
            text = stringResource(id = R.string.personal_portal_end_text),
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.colorTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
@Preview
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
private fun MigrationScreenPreview() {
    ManagerTheme {
        PersonalMigrationScreen()
    }
}
