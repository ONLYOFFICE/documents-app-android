package app.editors.manager.ui.fragments.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import app.documents.core.model.cloud.FormRole
import app.editors.manager.R
import app.editors.manager.mvp.models.ui.toUi
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.views.custom.FillingStatusRoleList
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar


class FillingStatusFragment : ComposeDialogFragment() {

    companion object {

        private fun newInstance(): FillingStatusFragment {
            return FillingStatusFragment()
        }

        fun show(fragmentManager: FragmentManager) {
            newInstance().show(fragmentManager, "")
        }
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            FillingStatusScreen(onBack = ::dismiss)
        }
    }
}

@Composable
private fun FillingStatusScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = R.string.filling_form_filling_status,
                backListener = onBack,
                isClose = true
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppDescriptionItem(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                text = R.string.filling_form_filling_status_desc
            )
            FormInfoContent()
            AppDivider()
            AppHeaderItem(title = R.string.filling_form_filling_process_details)
            FillingStatusRoleList(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
                    .padding(bottom = 16.dp)
                ,
                data = FormRole.mockList.map { it.toUi(LocalContext.current) }
            )
            AppDivider()
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AppTextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    title = R.string.filling_form_stop_filling
                ) { }
                AppTextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    title = R.string.list_context_fill
                ) { }
            }
        }
    }
}

@Composable
private fun FormInfoContent(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_type_pdf_row),
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Application for leave")
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colors.colorTextTertiary)
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    text = "Stopped",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Text(
                text = "Ben Howard · 05 May 2024",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
    }
}

@Preview
@Composable
private fun FillingStatusScreenPreview() {
    ManagerTheme {
        FillingStatusScreen() {}
    }
}