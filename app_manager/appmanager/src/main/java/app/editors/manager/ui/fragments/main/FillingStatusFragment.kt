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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
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
import app.editors.manager.ui.fragments.main.FillingStatusMode.None
import app.editors.manager.ui.fragments.main.FillingStatusMode.SendForm
import app.editors.manager.ui.fragments.main.FillingStatusMode.StartFilling
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

enum class FillingStatusMode {

    None, SendForm, StartFilling
}

class FillingStatusFragment : ComposeDialogFragment() {

    companion object {

        private const val REQUEST_KEY = "filling_status_request"
        private const val RESULT_START_FILL_KEY = "result_start_fill_key"
        private const val RESULT_ON_CLOSE_KEY = "result_on_close_key"
        private const val KEY_FILLING_STATUS_MODE = "key_filling_status_mode"
        private const val KEY_FORM_ID = "key_form_id"

        private fun newInstance(
            formId: String,
            fillingStatusMode: FillingStatusMode
        ): FillingStatusFragment {
            return FillingStatusFragment()
                .putArgs(KEY_FORM_ID to formId)
                .putArgs(KEY_FILLING_STATUS_MODE to fillingStatusMode)
        }

        fun show(
            activity: FragmentActivity,
            formId: String,
            fillingStatusMode: FillingStatusMode,
            onClose: () -> Unit,
            onStartFill: () -> Unit
        ) {
            activity.supportFragmentManager.setFragmentResultListener(
                REQUEST_KEY,
                activity
            ) { _, bundle ->
                when {
                    bundle.getBoolean(RESULT_START_FILL_KEY) -> onStartFill()
                    bundle.getBoolean(RESULT_ON_CLOSE_KEY) -> onClose()
                }
            }
            newInstance(formId, fillingStatusMode).show(activity.supportFragmentManager, "")
        }
    }

    private val fillingStatusMode: FillingStatusMode by lazy {
        arguments?.getSerializableExt(KEY_FILLING_STATUS_MODE) ?: None
    }

    @Composable
    override fun Content() {
        ManagerTheme {
            val viewModel = viewModel<FillingStatusViewModel> {
                FillingStatusViewModel(
                    formId = arguments?.getString(KEY_FORM_ID).orEmpty(),
                    cloudFileProvider = requireContext().cloudFileProvider
                )
            }
            FillingStatusRoute(
                fillingStatusMode = fillingStatusMode,
                viewModel = viewModel,
                onBack = ::dismiss,
                onFillClick = ::onFillClick,
                onStopFillingClick = { showStopFillingQuestionDialog(viewModel::stopFilling) },
                onSnackBar = {
                    UiUtils.getSnackBar(requireActivity())
                        .setText(it)
                        .show()
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
fun FillingStatusRoute(
    viewModel: FillingStatusViewModel,
    fillingStatusMode: FillingStatusMode,
    onSnackBar: (Int) -> Unit,
    onFillClick: () -> Unit,
    onBack: () -> Unit,
    onStopFillingClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FillingStatusEffect.Error -> { onSnackBar(R.string.errors_unknown_error) }
            }
        }
    }

    FillingStatusScreen(
        state = state.value,
        fillingStatusMode = fillingStatusMode,
        onBack = onBack,
        onFillClick = onFillClick,
        onStopFillingClick = onStopFillingClick,
        onCopyLinkClick = {
            KeyboardUtils.setDataToClipboard(
                context,
                state.value.formInfo.shortWebUrl
            )
            onSnackBar(R.string.rooms_info_create_link_complete)
        },
        onShareClick = {
            context.openSendTextActivity(
                context.getString(R.string.toolbar_menu_main_share),
                state.value.formInfo.shortWebUrl
            )
        }
    )
}

@Composable
private fun FillingStatusScreen(
    state: FillingStatusState,
    fillingStatusMode: FillingStatusMode,
    onCopyLinkClick: () -> Unit,
    onShareClick: () -> Unit,
    onStopFillingClick: () -> Unit,
    onFillClick: () -> Unit,
    onBack: () -> Unit
) {
    AppScaffold(
        topBar = {
            if (fillingStatusMode == None) {
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
            if (fillingStatusMode in arrayOf(StartFilling, SendForm)) {
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
                    text = when (fillingStatusMode) {
                        SendForm -> {
                            when (state.completeStatus) {
                                FormCompleteStatus.Waiting -> {
                                    stringResource(R.string.filling_form_send_section_complete)
                                }

                                FormCompleteStatus.Complete -> {
                                    stringResource(R.string.filling_form_send_finalized)
                                }

                                else -> ""
                            }
                        }

                        StartFilling -> {
                            stringResource(R.string.start_filling_ready_for_filling)
                        }

                        else -> ""
                    }
                )
            }
            when (fillingStatusMode) {
                SendForm -> {
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
                }

                StartFilling -> {
                    AppDescriptionItem(
                        modifier = Modifier.padding(top = 16.dp),
                        text = R.string.start_filling_ready_for_filling_desc
                    )
                }

                None -> {
                    AppDescriptionItem(
                        modifier = Modifier.padding(top = 16.dp),
                        text = R.string.filling_form_filling_status_desc
                    )
                }
            }
            FormInfoContent(
                modifier = Modifier.padding(top = 8.dp),
                formInfo = state.formInfo,
                canBeShared = fillingStatusMode in arrayOf(StartFilling, SendForm),
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
                        if (fillingStatusMode == None) {
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
            AppDivider()
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                when (fillingStatusMode) {
                    SendForm -> {
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

                    StartFilling -> {
                        AppTextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            title = R.string.rooms_fill_form_complete_back_to_room,
                            onClick = onBack
                        )
                        AppTextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            enabled = state.formInfo.security?.fillForms == true &&
                                    !state.requestLoading,
                            title = R.string.list_context_fill,
                            onClick = onFillClick
                        )
                    }

                    else -> {
                        if (state.completeStatus == FormCompleteStatus.Waiting && state.roles.isNotEmpty()) {
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
    }
}

@Composable
private fun FormInfoContent(
    modifier: Modifier = Modifier,
    formInfo: CloudFile,
    canBeShared: Boolean,
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
                Text(
                    modifier = Modifier.weight(1f, false),
                    text = StringUtils.removeExtension(formInfo.title),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
        if (canBeShared) {
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
            fillingStatusMode = FillingStatusMode.StartFilling,
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
            onStopFillingClick = {},
            onFillClick = {},
            onCopyLinkClick = {},
            onShareClick = {}
        ) {}
    }
}