package app.editors.manager.ui.fragments.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.CreatedBy
import app.documents.core.network.manager.models.explorer.FormRole
import app.editors.manager.R
import app.editors.manager.app.cloudFileProvider
import app.editors.manager.mvp.models.ui.UiFormFillingStatus
import app.editors.manager.mvp.models.ui.toUi
import app.editors.manager.ui.views.custom.FillingStatusRoleList
import app.editors.manager.ui.views.custom.FormCompleteStatus
import app.editors.manager.viewModels.main.FillingStatusEffect
import app.editors.manager.viewModels.main.FillingStatusState
import app.editors.manager.viewModels.main.FillingStatusViewModel
import lib.compose.ui.fragments.ComposeDialogFragment
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorGreen
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppDivider
import lib.compose.ui.views.AppHeaderItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTextButton
import lib.compose.ui.views.AppTopBar
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.openSendTextActivity
import lib.toolkit.base.managers.utils.putArgs
import java.util.Date


class FillingStatusFragment : ComposeDialogFragment() {

    companion object {

        private const val KEY_CLOUD_FILE = "key_cloud_file"
        private const val REQUEST_KEY = "filling_status_request"
        private const val RESULT_START_FILL_KEY = "result_start_fill_key"
        private const val RESULT_ON_CLOSE_KEY = "result_on_close_key"
        private const val KEY_SEND_FORM = "key_send_form"

        private fun newInstance(file: CloudFile, isSendForm: Boolean): FillingStatusFragment {
            return FillingStatusFragment()
                .putArgs(KEY_CLOUD_FILE to file)
                .putArgs(KEY_SEND_FORM to isSendForm)
        }

        fun show(
            activity: FragmentActivity,
            file: CloudFile,
            isSendForm: Boolean,
            onClose: () -> Unit,
            onStartFill: () -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY,
                activity
            ) { _, bundle ->
                when {
                    bundle.getBoolean(RESULT_START_FILL_KEY) == true -> onStartFill()
                    bundle.getBoolean(RESULT_ON_CLOSE_KEY) == true -> onClose()
                }
            }
            newInstance(file, isSendForm).show(activity.supportFragmentManager, "")
        }
    }

    private val cloudFile: CloudFile by lazy {
        arguments?.getSerializableExt(KEY_CLOUD_FILE) ?: CloudFile()
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val viewModel = viewModel<FillingStatusViewModel> {
                FillingStatusViewModel(
                    formInfo = cloudFile,
                    cloudFileProvider = requireContext().cloudFileProvider
                )
            }
            val state = viewModel.state.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        FillingStatusEffect.Error -> {
                            UiUtils.getSnackBar(requireActivity())
                                .setText(R.string.errors_unknown_error)
                                .show()
                        }
                    }
                }
            }

            FillingStatusScreen(
                state = state.value,
                isSendForm = remember { arguments?.getBoolean(KEY_SEND_FORM) == true },
                onBack = ::dismiss,
                onFillClick = ::onFillClick,
                onStopFillingClick = { showStopFillingQuestionDialog(viewModel::stopFilling) },
                onCopyLinkClick = {
                    KeyboardUtils.setDataToClipboard(requireContext(), state.value.formInfo.shortWebUrl)
                    UiUtils.getSnackBar(requireActivity())
                        .setText(R.string.rooms_info_create_link_complete)
                        .show()
                },
                onShareClick = {
                    requireContext().openSendTextActivity(
                        getString(R.string.toolbar_menu_main_share),
                        state.value.formInfo.shortWebUrl
                    )
                }
            )
        }
    }

    override fun onDestroyView() {
        requireActivity().supportFragmentManager
            .setFragmentResult(REQUEST_KEY, bundleOf(RESULT_ON_CLOSE_KEY to true))
        super.onDestroyView()
    }

    private fun onFillClick() {
        requireActivity().supportFragmentManager
            .setFragmentResult(REQUEST_KEY, bundleOf(RESULT_START_FILL_KEY to true))
        dismiss()
    }

    private fun showStopFillingQuestionDialog(onAccept: () -> Unit) {
        dismiss()
        UiUtils.showQuestionDialog(
            context = requireContext(),
            title = getString(R.string.filling_form_stop_filling_dialog_title),
            description = getString(R.string.filling_form_stop_filling_dialog_desc),
            acceptListener = onAccept,
            acceptTitle = getString(R.string.filling_form_stop_filling),
        )
    }
}

@Composable
private fun FillingStatusScreen(
    state: FillingStatusState,
    isSendForm: Boolean,
    onCopyLinkClick: () -> Unit,
    onShareClick: () -> Unit,
    onStopFillingClick: () -> Unit,
    onFillClick: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            if (!isSendForm) {
                AppTopBar(
                    title = R.string.filling_form_filling_status,
                    backListener = onBack,
                    isClose = true
                )
            }
        },
        useTablePaddings = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibilityVerticalFade(visible = state.requestLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (isSendForm) {
                Box(
                    modifier = Modifier
                        .padding(top = 56.dp)
                        .size(56.dp)
                        .border(2.dp, MaterialTheme.colors.colorGreen, CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.drawable_ic_done),
                        tint = MaterialTheme.colors.colorGreen,
                        contentDescription = null
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.h6,
                    text = when (state.completeStatus) {
                        FormCompleteStatus.Waiting -> {
                            stringResource(R.string.filling_form_send_section_complete)
                        }
                        FormCompleteStatus.Complete -> {
                            stringResource(R.string.filling_form_send_finalized)
                        }
                        else -> ""
                    }
                )
            }
            if (isSendForm) {
                when (state.completeStatus) {
                    FormCompleteStatus.Waiting -> {
                        AppDescriptionItem(
                            modifier = Modifier.padding(top = 16.dp),
                            text = R.string.filling_form_send_section_complete_desc
                        )
                    }
                    FormCompleteStatus.Complete -> {
                        AppDescriptionItem(
                            modifier = Modifier.padding(top = 16.dp),
                            text = R.string.filling_form_send_finalized_desc
                        )
                    }
                    else -> Unit
                }
            } else {
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 16.dp),
                    text = R.string.filling_form_filling_status_desc
                )
            }
            FormInfoContent(
                modifier = Modifier.padding(top = 8.dp),
                formInfo = state.formInfo,
                isSendForm = isSendForm,
                onShareClick = onShareClick
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
                        if (!isSendForm) {
                            AppHeaderItem(title = R.string.filling_form_filling_process_details)
                        }
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
            if (isSendForm) {
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
                        title = R.string.rooms_fill_form_complete_back_to_room,
                        onClick = onBack
                    )
                    AppTextButton(
                        modifier = Modifier.padding(end = 8.dp),
                        title = R.string.rooms_info_copy_link,
                        onClick = onCopyLinkClick
                    )
                }
            } else if (state.completeStatus == FormCompleteStatus.Waiting && state.roles.isNotEmpty()) {
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
                        enabled = state.formInfo.security?.stopFilling == true &&
                                !state.requestLoading,
                        title = R.string.filling_form_stop_filling,
                        onClick = onStopFillingClick
                    )
                    AppTextButton(
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = state.formInfo.security?.fillForms == true &&
                                !state.requestLoading,
                        title = R.string.list_context_fill,
                        onClick = onFillClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FormInfoContent(
    modifier: Modifier = Modifier,
    formInfo: CloudFile,
    isSendForm: Boolean,
    onShareClick: () -> Unit,
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
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val formFillingStatus = UiFormFillingStatus.from(formInfo.formFillingStatus)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = StringUtils.removeExtension(formInfo.title))
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            formFillingStatus
                                .takeIf { it != UiFormFillingStatus.None }
                                ?.let { colorResource(it.colorRes) } ?: Color.Transparent
                        )
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    text = formFillingStatus
                        .takeIf { it != UiFormFillingStatus.None }
                        ?.let { stringResource(formFillingStatus.textRes) }.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onPrimary
                )
            }
            Text(
                text = "${formInfo.createdBy.displayNameFromHtml} · ${TimeUtils.formatDate(formInfo.created)}",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.colorTextSecondary
            )
        }
        if (isSendForm) {
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(lib.toolkit.base.R.drawable.ic_list_context_share),
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun FillingStatusScreenPreview() {
    ManagerTheme {
        FillingStatusScreen(
            state = FillingStatusState(
                loading = false,
                formInfo = CloudFile()
                    .apply {
                        createdBy = CreatedBy().apply {
                            displayName = "Username"
                        }
                        title = "File name.pdf"
                        created = Date()
                        formFillingStatusType = 1
                    },
                roles = FormRole.mockList
            ),
            isSendForm = true,
            onStopFillingClick = {},
            onFillClick = {},
            onCopyLinkClick = {},
            onShareClick = {}
        ) {}
    }
}