package app.editors.manager.ui.fragments.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CreatedBy
import app.editors.manager.R
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.mvp.models.ui.UiFormFillingStatus
import app.editors.manager.mvp.models.ui.toUi
import app.editors.manager.ui.dialogs.fragments.ComposeDialogFragment
import app.editors.manager.ui.views.custom.FillingStatusRoleList
import app.editors.manager.ui.views.custom.FormCompleteStatus
import app.editors.manager.viewModels.main.FillingStatusState
import app.editors.manager.viewModels.main.FillingStatusViewModel
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import java.util.Date


class FillingStatusFragment : ComposeDialogFragment() {

    companion object {

        private const val CLOUD_FILE = "cloud_file"

        private fun newInstance(file: CloudFile): FillingStatusFragment {
            return FillingStatusFragment()
                .putArgs(CLOUD_FILE to file)
        }

        fun show(fragmentManager: FragmentManager, file: CloudFile) {
            newInstance(file).show(fragmentManager, "")
        }
    }

    private val cloudFile: CloudFile by lazy {
        arguments?.getSerializableExt(CLOUD_FILE) ?: CloudFile()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val viewModel = viewModel<FillingStatusViewModel> {
                FillingStatusViewModel(
                    fileId = cloudFile.id,
                    cloudFileProvider = requireContext().cloudFileProvider
                )
            }
            val state = viewModel.state.collectAsState()

            FillingStatusScreen(
                state = state.value,
                cloudFile = cloudFile,
                onBack = ::dismiss
            )
        }
    }
}

@Composable
private fun FillingStatusScreen(
    cloudFile: CloudFile,
    state: FillingStatusState,
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
            FormInfoContent(
                formFillingStatus = UiFormFillingStatus.from(cloudFile.formFillingStatus),
                fileTitle = StringUtils.removeExtension(cloudFile.title),
                owner = cloudFile.createdBy.displayNameFromHtml,
                date = cloudFile.created
            )
            AppDivider()
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = state.loading,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) }
            ) { loading ->
                if (loading) {
                    ActivityIndicatorView()
                } else {
                    Column {
                        AppHeaderItem(title = R.string.filling_form_filling_process_details)
                        FillingStatusRoleList(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            data = state.roles.map { it.toUi(LocalContext.current) },
                            completeStatus = state.completeStatus
                        )
                    }
                }
            }
            if (state.completeStatus == FormCompleteStatus.Waiting) {
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
                        enabled = !state.loading && cloudFile.security?.stopFilling == true,
                        title = R.string.filling_form_stop_filling
                    ) { }
                    AppTextButton(
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !state.loading && cloudFile.security?.fill == true,
                        title = R.string.list_context_fill
                    ) { }
                }
            }
        }
    }
}

@Composable
private fun FormInfoContent(
    modifier: Modifier = Modifier,
    formFillingStatus: UiFormFillingStatus,
    fileTitle: String,
    owner: String,
    date: Date
) {
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
                Text(text = fileTitle)
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorResource(formFillingStatus.colorRes))
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    text = stringResource(formFillingStatus.textRes),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Text(
                text = "$owner · ${TimeUtils.formatDate(date)}",
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
        FillingStatusScreen(
            cloudFile = CloudFile().apply {
                createdBy = CreatedBy().apply {
                    displayName = "Username"
                }
                title = "File name.pdf"
                formFillingStatusType = 1
            },
            state = FillingStatusState(loading = true)
        ) {}
    }
}