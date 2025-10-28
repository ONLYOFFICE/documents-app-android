package app.editors.manager.ui.fragments.template.createroom

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.CloudFolderLogo
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.RoomUtils
import app.editors.manager.managers.utils.StringUtils
import app.editors.manager.ui.fragments.template.rememberAccountContext
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.RoomFromTemplateViewModel
import app.editors.manager.viewModels.main.TemplateListState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextTertiary
import lib.compose.ui.views.AppMultilineArrowItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.UiUtils
import java.util.Date

@Composable
fun RoomFromTemplateScreen(
    viewModel: RoomFromTemplateViewModel,
    onTemplateClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_create_from_template),
                backListener = onBack,
                isClose = UiUtils.isTablet(context)
            )
        },
        useTablePaddings = false
    ) {
        RoomFromTemplateScreenContent(
            loadingResult = state,
            onTemplateClick = onTemplateClick
        )
    }
}

@Composable
private fun RoomFromTemplateScreenContent(
    loadingResult: NetworkResult<TemplateListState>,
    onTemplateClick: (String) -> Unit
) {
    Crossfade(loadingResult) { state ->
        when (state) {
            NetworkResult.Loading -> LoadingPlaceholder()
            is NetworkResult.Success -> {
                RoomFromTemplateScreenContent(
                    state = state.data,
                    onTemplateClick = onTemplateClick
                )
            }

            is NetworkResult.Error -> {
                PlaceholderView(
                    image = null,
                    title = stringResource(R.string.placeholder_connection),
                    subtitle = stringResource(R.string.placeholder_connection_desc)
                )
            }
        }
    }
}

@Composable
private fun RoomFromTemplateScreenContent(
    state: TemplateListState,
    onTemplateClick: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(state.templates, key = CloudFolder::id) { item ->
            TemplateItem(
                template = item,
                sortBy = state.sortBy,
                onClick = onTemplateClick
            )
        }
    }
}

@Composable
private fun TemplateItem(
    template: CloudFolder,
    sortBy: String?,
    onClick: (String) -> Unit
) {
    val context = LocalContext.current
    val separator = stringResource(R.string.placeholder_point)
    val owner = if (!LocalView.current.isInEditMode) {
        StringUtils.getItemOwner(
            context = context,
            item = template,
            userId = context.accountOnline?.id
        )
    } else {
        stringResource(R.string.item_owner_self)
    }

    val subtitle = StringUtils.getRoomInfo(
        roomType = template.roomType,
        context = context,
        date = TimeUtils.getWeekDate(template.updated),
        owner = owner,
        sortBy = sortBy,
        isGridView = false
    ).joinToString(separator)

    AppMultilineArrowItem(
        title = template.title,
        description = subtitle,
        icon = {
            TemplateLogo(
                name = template.title,
                logo = template.logo
            )
        },
        singleLine = true,
        onClick = { onClick(template.id) }
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun TemplateLogo(
    name: String,
    logo: CloudFolderLogo?
) {
    val accountContext = rememberAccountContext()
    val frameColor = logo?.color?.let { color -> Color("#$color".toColorInt()) }
        ?: MaterialTheme.colors.colorTextTertiary

    Box(
        modifier = Modifier.padding(start = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.room_template_logo),
            contentDescription = null,
            tint = frameColor,
            modifier = Modifier.size(36.dp)
        )
        if (logo?.large?.isNotEmpty() == true) {
            GlideImage(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp)),
                model = GlideUtils.getCorrectLoad(
                    token = accountContext.token,
                    portal = accountContext.portal.urlWithScheme,
                    url = logo.large
                ),
                contentDescription = null
            )
        } else {
            Text(
                text = RoomUtils.getRoomInitials(name).orEmpty(),
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                color = frameColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomFromTemplateScreenContentPreview() {
    ManagerTheme {
        RoomFromTemplateScreenContent(
            loadingResult = NetworkResult.Success(
                TemplateListState(
                    templates = listOf(
                        CloudFolder().apply {
                            id = "1"
                            title = "Template 1"
                            created = Date()
                        },
                        CloudFolder().apply {
                            id = "2"
                            title = "Second Template"
                            created = Date()
                        }
                    ),
                    sortBy = ""
                )
            ),
            onTemplateClick = {}
        )
    }
}
