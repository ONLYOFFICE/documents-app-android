package app.editors.manager.ui.fragments.room.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.compose.rememberNavController
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.Lifetime
import app.documents.core.network.manager.models.explorer.Watermark
import app.documents.core.network.manager.models.explorer.WatermarkInfo
import app.documents.core.network.manager.models.explorer.WatermarkTextPosition
import app.documents.core.network.manager.models.explorer.WatermarkType
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.appComponent
import app.editors.manager.managers.utils.GlideUtils
import app.editors.manager.managers.utils.StorageUtils
import app.editors.manager.mvp.models.ui.SizeUnit
import app.editors.manager.ui.dialogs.AddRoomItem
import app.editors.manager.viewModels.main.RoomSettingsLogoState
import app.editors.manager.viewModels.main.RoomSettingsState
import app.editors.manager.viewModels.main.RoomSettingsStorage
import app.editors.manager.viewModels.main.RoomSettingsWatermarkState
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.launch
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.theme.colorTextSecondary
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AnimatedVisibilityVerticalFade
import lib.compose.ui.views.AppArrowItem
import lib.compose.ui.views.AppDescriptionItem
import lib.compose.ui.views.AppListItem
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppSelectableChip
import lib.compose.ui.views.AppSwitchItem
import lib.compose.ui.views.AppTextField
import lib.compose.ui.views.AppTextFieldListItem
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.ChipList
import lib.compose.ui.views.ChipsTextField
import lib.compose.ui.views.DropdownMenuButton
import lib.compose.ui.views.DropdownMenuItem
import lib.compose.ui.views.NestedColumn
import lib.toolkit.base.managers.utils.AccountUtils
import lib.toolkit.base.managers.utils.ContentResolverUtils
import lib.toolkit.base.managers.utils.FileUtils
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.managers.utils.capitalize
import java.io.File

@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RoomSettingsScreen(
    isEdit: Boolean,
    canApplyChanges: Boolean,
    isRoomTypeEditable: Boolean,
    state: RoomSettingsState,
    loadingState: Boolean,
    watermarkState: RoomSettingsWatermarkState,
    logoState: RoomSettingsLogoState,
    onApply: () -> Unit,
    onSetImage: (uri: Uri?, isWatermark: Boolean) -> Unit,
    onSetName: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onSetIndexing: (Boolean) -> Unit,
    onSetRestrict: (Boolean) -> Unit,
    onSetLifetimeEnable: (Boolean) -> Unit,
    onSetLifetimeValue: (Int) -> Unit,
    onSetLifetimePeriod: (Int) -> Unit,
    onSetLifetimeAction: (Boolean) -> Unit,
    onWatermarkEnable: (Boolean) -> Unit,
    onSetWatermarkAddition: (Int) -> Unit,
    onSetWatermarkStaticText: (String) -> Unit,
    onSetWatermarkTextPosition: (Int) -> Unit,
    onSetWatermarkImageScale: (Int) -> Unit,
    onSetWatermarkImageRotate: (Int) -> Unit,
    onSetWatermarkType: (WatermarkType) -> Unit,
    onSetQuotaEnabled: (Boolean) -> Unit,
    onSetQuotaValue: (Long) -> Unit,
    onSetQuotaMeasurementUnit: (SizeUnit) -> Unit,
    onBack: () -> Unit,
    loadingRoom: Boolean = false,
    onSetOwner: () -> Unit = {},
    onLocationClick: (String) -> Unit = {},
    onCreateNewFolder: (Boolean) -> Unit = {},
    onStorageConnect: (Boolean) -> Unit = {},
    onSelectType: () -> Unit = {}
) {
    val keyboardController = LocalFocusManager.current
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    var isWatermarkSelected = remember { false }

    fun selectPhoto(isWatermark: Boolean) {
        coroutineScope.launch {
            isWatermarkSelected = isWatermark
            modalBottomSheetState.show()
        }
    }

    ModalBottomSheetLayout(
        sheetContent = {
            ChooseImageBottomView(
                onDelete = {
                    coroutineScope.launch { modalBottomSheetState.hide() }
                    onSetImage.invoke(null, isWatermarkSelected)
                }.takeIf {
                    if (!isWatermarkSelected) {
                        logoState.logoUri != null
                    } else {
                        watermarkState.imageUri != null
                    }
                },
                onSuccess = {
                    coroutineScope.launch { modalBottomSheetState.hide() }
                    onSetImage.invoke(it, isWatermarkSelected)
                }
            )
        },
        sheetState = modalBottomSheetState,
        scrimColor = if (!isSystemInDarkTheme()) {
            ModalBottomSheetDefaults.scrimColor
        } else {
            MaterialTheme.colors.background.copy(alpha = 0.60f)
        }
    ) {
        AppScaffold(topBar = {
            Column {
                AppTopBar(
                    backListener = onBack,
                    title = if (isEdit)
                        stringResource(id = R.string.list_context_edit_room) else
                        stringResource(id = R.string.dialog_create_room),
                    isClose = true,
                    actions = {
                        TextButton(
                            enabled = canApplyChanges,
                            onClick = {
                                keyboardController.clearFocus()
                                onApply()
                            }
                        ) {
                            Text(
                                text = if (isEdit)
                                    stringResource(id = lib.toolkit.base.R.string.common_done) else
                                    stringResource(id = R.string.login_create_signin_create_button).capitalize(),
                            )
                        }
                    }
                )
                if (loadingState) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }, useTablePaddings = false) {
            if (loadingRoom) {
                ActivityIndicatorView()
            } else {
                val context = LocalContext.current
                NestedColumn {
                    AddRoomItem(
                        roomType = state.type,
                        clickable = isRoomTypeEditable
                    ) {
                        onSelectType()
                    }
                    Row(
                        modifier = Modifier
                            .height(dimensionResource(id = lib.toolkit.base.R.dimen.item_two_line_height))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(40.dp)
                                .width(IntrinsicSize.Min)
                                .height(IntrinsicSize.Min)
                                .clickable(
                                    onClick = {
                                        keyboardController.clearFocus()
                                        selectPhoto(false)
                                    }
                                )
                        ) {
                            logoState.logoPreview?.let { preview ->
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    bitmap = preview.asImageBitmap(),
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                )
                            } ?: run {
                                val urlWithToken = if (!LocalView.current.isInEditMode) {
                                    GlideUtils.getCorrectLoad(
                                        context.accountOnline?.portal?.urlWithScheme + logoState.logoWebUrl,
                                        AccountUtils.getToken(
                                            context,
                                            context.appComponent.accountOnline?.accountName.orEmpty()
                                        ).orEmpty()
                                    )
                                } else {
                                    ""
                                }

                                GlideImage(
                                    modifier = Modifier.fillMaxSize(),
                                    model = urlWithToken,
                                    contentDescription = null,
                                    loading = placeholder(R.drawable.ic_empty_image),
                                    failure = placeholder(R.drawable.ic_empty_image)
                                )
                            }
                        }
                        AppTextFieldListItem(
                            modifier = Modifier
                                .height(56.dp)
                                .padding(start = 16.dp),
                            value = state.name,
                            hint = stringResource(id = R.string.room_name_hint),
                            onValueChange = { onSetName(it) }
                        )
                    }
                    ChipsTextField(
                        modifier = Modifier.padding(start = 16.dp),
                        label = stringResource(id = R.string.room_add_tag_hint),
                        chips = state.tags,
                        onChipAdd = { tag ->
                            val exists = state.tags.list.any { it == tag }
                            if (!exists) onAddTag(tag)
                        },
                        onChipDelete = onRemoveTag
                    )

                    if (isEdit) {
                        AppArrowItem(
                            title = R.string.share_access_room_owner,
                            option = state.owner.displayName,
                            onClick = onSetOwner
                        )
                    }

                    if (state.type == ApiContract.RoomType.PUBLIC_ROOM) {
                        ThirdPartyBlock(
                            isEdit = isEdit,
                            state = state.storageState,
                            roomName = state.name,
                            onLocationClick = onLocationClick::invoke,
                            onCreateNewFolder = onCreateNewFolder,
                            onStorageConnect = onStorageConnect
                        )
                    }

                    if (state.type == ApiContract.RoomType.VIRTUAL_ROOM) {
                        VdrRoomBlock(
                            state = state,
                            watermarkState = watermarkState,
                            onSelectImage = { selectPhoto(true) },
                            onDeleteImage = { onSetImage(null, true) },
                            onSetIndexing = onSetIndexing,
                            onSetRestrict = onSetRestrict,
                            onSetLifetimeEnable = onSetLifetimeEnable,
                            onSetLifetimeValue = onSetLifetimeValue,
                            onSetLifetimePeriod = onSetLifetimePeriod,
                            onSetLifetimeAction = onSetLifetimeAction,
                            onWatermarkEnable = onWatermarkEnable,
                            onSetAddition = onSetWatermarkAddition,
                            onSetStaticText = onSetWatermarkStaticText,
                            onSetTextPosition = onSetWatermarkTextPosition,
                            onSetImageScale = onSetWatermarkImageScale,
                            onSetImageRotate = onSetWatermarkImageRotate,
                            onSetWatermarkType = onSetWatermarkType,
                        )
                    }

                    AnimatedVisibilityVerticalFade(visible = state.quota.visible) {
                        QuotaBlock(
                            state = state,
                            onSetEnabled = onSetQuotaEnabled,
                            onSetValue = onSetQuotaValue,
                            onSetMeasurementUnit = onSetQuotaMeasurementUnit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThirdPartyBlock(
    isEdit: Boolean,
    state: RoomSettingsStorage?,
    roomName: String,
    onLocationClick: (String) -> Unit,
    onCreateNewFolder: (Boolean) -> Unit,
    onStorageConnect: (Boolean) -> Unit,
) {
    Column {
        val storageName = state?.providerKey?.let(StorageUtils::getStorageTitle)

        if (!isEdit) {
            AppSwitchItem(
                title = R.string.room_create_thirdparty_storage_title,
                checked = storageName != null,
                onCheck = onStorageConnect::invoke
            )
        }

        if (state != null && storageName != null) {
            AppArrowItem(
                title = stringResource(id = R.string.room_create_thirdparty_storage),
                option = stringResource(id = storageName),
                enabled = !isEdit,
                arrowVisible = !isEdit,
                onClick = { onStorageConnect.invoke(true) }
            )
            if (!isEdit) {
                AppArrowItem(
                    title = stringResource(id = R.string.room_create_thirdparty_location),
                    option = if (state.createAsNewFolder) {
                        state.location?.let { "$it$roomName" } ?: "/$roomName"
                    } else {
                        state.location
                            ?: stringResource(id = R.string.room_create_thirdparty_location_root)
                    },
                    onClick = { onLocationClick.invoke(state.id) }
                )
                AppSwitchItem(
                    title = R.string.room_create_thirdparty_new_folder,
                    checked = state.createAsNewFolder,
                    onCheck = onCreateNewFolder
                )
                AppDescriptionItem(
                    modifier = Modifier.padding(top = 8.dp),
                    text = R.string.room_create_thirdparty_desc
                )
            }
        }
    }
}


@Composable
private fun VdrRoomBlock(
    state: RoomSettingsState,
    watermarkState: RoomSettingsWatermarkState,
    onSetIndexing: (Boolean) -> Unit,
    onSetRestrict: (Boolean) -> Unit,
    onSetLifetimeEnable: (Boolean) -> Unit,
    onSetLifetimeValue: (Int) -> Unit,
    onSetLifetimePeriod: (Int) -> Unit,
    onSetLifetimeAction: (Boolean) -> Unit,
    onWatermarkEnable: (Boolean) -> Unit,
    onSetAddition: (Int) -> Unit,
    onSetStaticText: (String) -> Unit,
    onSetTextPosition: (Int) -> Unit,
    onSetImageScale: (Int) -> Unit,
    onSetImageRotate: (Int) -> Unit,
    onSetWatermarkType: (WatermarkType) -> Unit,
    onSelectImage: () -> Unit,
    onDeleteImage: () -> Unit
) {
    AppSwitchItem(
        title = stringResource(R.string.rooms_vdr_indexing_title),
        checked = state.indexing,
        onCheck = { checked -> onSetIndexing(checked) }
    )
    AppDescriptionItem(
        text = stringResource(R.string.rooms_vdr_indexing_desc),
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = R.string.file_lifetime_title,
        checked = state.lifetime.enabled,
        onCheck = { checked -> onSetLifetimeEnable(checked) }
    )
    AnimatedVisibilityVerticalFade(visible = state.lifetime.enabled) {
        LifeTimeBlock(
            lifetime = state.lifetime,
            onSetValue = onSetLifetimeValue,
            onSetPeriod = onSetLifetimePeriod,
            onSetAction = onSetLifetimeAction,
        )
    }
    AppDescriptionItem(
        text = stringResource(R.string.file_lifetime_desc),
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = stringResource(R.string.rooms_info_file_rectrict),
        checked = state.denyDownload,
        singleLine = false,
        onCheck = { checked -> onSetRestrict(checked) }
    )
    AppDescriptionItem(
        text = stringResource(R.string.rooms_vdr_file_restrict_desc),
        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
    )

    AppSwitchItem(
        title = stringResource(R.string.rooms_vdr_watermark_title),
        checked = watermarkState.watermark.enabled,
        onCheck = onWatermarkEnable
    )
    if (!watermarkState.watermark.enabled) {
        AppDescriptionItem(
            text = stringResource(R.string.rooms_vdr_watermark_desc),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
    }
    AnimatedVisibilityVerticalFade(visible = watermarkState.watermark.enabled) {
        WatermarkBlock(
            watermarkState = watermarkState,
            onSetAddition = onSetAddition,
            onSetStaticText = onSetStaticText,
            onSetTextPosition = onSetTextPosition,
            onSetImageScale = onSetImageScale,
            onSetImageRotate = onSetImageRotate,
            onSetWatermarkType = onSetWatermarkType,
            onSelectImage = onSelectImage,
            onDeleteImage = onDeleteImage,
        )
    }
}

@Composable
private fun WatermarkBlock(
    watermarkState: RoomSettingsWatermarkState,
    onSetAddition: (Int) -> Unit,
    onSetStaticText: (String) -> Unit,
    onSetTextPosition: (Int) -> Unit,
    onSetImageScale: (Int) -> Unit,
    onSetImageRotate: (Int) -> Unit,
    onSetWatermarkType: (WatermarkType) -> Unit,
    onSelectImage: () -> Unit,
    onDeleteImage: () -> Unit
) {
    Column {
        AppListItem(
            title = stringResource(R.string.rooms_vdr_watermark_type),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (watermarkState.watermark.type) {
                            WatermarkType.Image -> R.string.rooms_vdr_watermark_type_image
                            WatermarkType.ViewerInfo -> R.string.rooms_vdr_watermark_type_info
                        }
                    ),
                    state = popupVisible,
                    items = {
                        WatermarkType.values().forEach { type ->
                            DropdownMenuItem(
                                title = stringResource(
                                    when (type) {
                                        WatermarkType.Image -> R.string.rooms_vdr_watermark_type_image
                                        WatermarkType.ViewerInfo -> R.string.rooms_vdr_watermark_type_info
                                    }
                                ),
                                selected = watermarkState.watermark.type == type,
                                onClick = {
                                    onSetWatermarkType(type)
                                    popupVisible.value = false
                                }
                            )
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
        AnimatedContent(
            targetState = watermarkState.watermark.type,
            transitionSpec = {
                (fadeIn() + slideInVertically()).togetherWith(fadeOut() + slideOutVertically())
            }
        ) { type ->
            Column {
                when (type) {
                    WatermarkType.ViewerInfo -> WatermarkViewerInfoBlock(
                        watermark = watermarkState.watermark,
                        onSetAddition = onSetAddition,
                        onSetStaticText = onSetStaticText,
                        onSetTextPosition = onSetTextPosition,
                    )

                    WatermarkType.Image -> WatermarkSelectImageBlock(onClick = onSelectImage)
                }
                AppDescriptionItem(
                    text = stringResource(R.string.rooms_vdr_watermark_desc),
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                if (type == WatermarkType.Image) {
                    if (watermarkState.imagePreview != null || watermarkState.watermark.imageUrl != null) {
                        WatermarkImageBlock(
                            watermarkState = watermarkState,
                            onDeleteImage = onDeleteImage,
                            onSetImageScale = onSetImageScale,
                            onSetImageRotate = onSetImageRotate
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatermarkViewerInfoBlock(
    watermark: Watermark,
    onSetAddition: (Int) -> Unit,
    onSetStaticText: (String) -> Unit,
    onSetTextPosition: (Int) -> Unit
) {
    Column {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WatermarkInfo.values().forEach { info ->
                key(info) {
                    val selected = info in watermark.watermarkInfoList
                    AppSelectableChip(
                        selected = selected,
                        onClick = {
                            onSetAddition(
                                if (selected) {
                                    watermark.additions - info.mask
                                } else {
                                    watermark.additions + info.mask
                                }
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(
                                when (info) {
                                    WatermarkInfo.CurrentDate -> R.string.rooms_vdr_watermark_info_current_date
                                    WatermarkInfo.Email -> R.string.rooms_vdr_watermark_info_email
                                    WatermarkInfo.IpAddress -> R.string.rooms_vdr_watermark_info_ip_address
                                    WatermarkInfo.RoomName -> R.string.rooms_vdr_watermark_info_room_name
                                    WatermarkInfo.Username -> R.string.rooms_vdr_watermark_info_username
                                }
                            )
                        )
                    }
                }
            }
        }
        AppTextField(
            modifier = Modifier
                .padding(start = 16.dp)
                .padding(vertical = 8.dp),
            value = watermark.text,
            onValueChange = { value -> onSetStaticText(value) },
            keyboardType = KeyboardType.Text,
            label = R.string.rooms_vdr_watermark_static_text,
        )
        AppListItem(
            title = stringResource(R.string.rooms_vdr_watermark_position),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (watermark.textPosition) {
                            WatermarkTextPosition.Diagonal -> R.string.rooms_vdr_watermark_position_diagonal
                            WatermarkTextPosition.Horizontal -> R.string.rooms_vdr_watermark_position_horizontal
                        }
                    ),
                    state = popupVisible,
                    items = {
                        WatermarkTextPosition.values().forEach { position ->
                            DropdownMenuItem(
                                title = stringResource(
                                    when (position) {
                                        WatermarkTextPosition.Diagonal -> R.string.rooms_vdr_watermark_position_diagonal
                                        WatermarkTextPosition.Horizontal -> R.string.rooms_vdr_watermark_position_horizontal
                                    }
                                ),
                                selected = watermark.textPosition == position,
                                onClick = {
                                    onSetTextPosition(position.angle)
                                    popupVisible.value = false
                                }
                            )
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
    }
}

@Composable
private fun WatermarkSelectImageBlock(
    onClick: () -> Unit
) {
    AppListItem(
        title = stringResource(R.string.rooms_vdr_watermark_image_select),
        titleColor = MaterialTheme.colors.primary,
        onClick = onClick
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun WatermarkImageBlock(
    watermarkState: RoomSettingsWatermarkState,
    onSetImageScale: (Int) -> Unit,
    onSetImageRotate: (Int) -> Unit,
    onDeleteImage: () -> Unit,
) {
    Column {
        val previewShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .width(IntrinsicSize.Max)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .clip(previewShape)
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, colorResource(lib.toolkit.base.R.color.colorOutline), previewShape)
        ) {
            when {
                watermarkState.imagePreview != null -> {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val scale = watermarkState.watermark.imageScale
                                val rotate = watermarkState.watermark.rotate
                                if (scale > 0) {
                                    scaleX = scale / 100f
                                    scaleY = scale / 100f
                                }
                                rotationZ = -rotate.toFloat()
                            },
                        bitmap = watermarkState.imagePreview.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )
                }

                watermarkState.watermark.imageUrl != null -> {
                    GlideImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val scale = watermarkState.watermark.imageScale
                                val rotate = watermarkState.watermark.rotate
                                if (scale > 0) {
                                    scaleX = scale / 100f
                                    scaleY = scale / 100f
                                }
                                rotationZ = -rotate.toFloat()
                            },
                        model = watermarkState.watermark.imageUrl.orEmpty(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
        AppListItem(
            title = stringResource(R.string.rooms_vdr_watermark_image_select),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = "${watermarkState.watermark.imageScale.takeIf { it > 0 } ?: 100}%",
                    state = popupVisible,
                    items = {
                        listOf(
                            0,
                            200,
                            300,
                            400,
                            500
                        ).forEach { scale ->
                            DropdownMenuItem(
                                title = "${scale.takeIf { it > 0 } ?: 100}%",
                                selected = watermarkState.watermark.imageScale == scale,
                                onClick = {
                                    onSetImageScale(scale)
                                    popupVisible.value = false
                                }
                            )
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
        AppListItem(
            title = stringResource(R.string.rooms_vdr_watermark_image_rotate),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = "${watermarkState.watermark.rotate}°",
                    state = popupVisible,
                    items = {
                        listOf(
                            0,
                            30,
                            45,
                            60,
                            90
                        ).forEach { rotate ->
                            DropdownMenuItem(
                                title = "$rotate°",
                                selected = watermarkState.watermark.rotate == rotate,
                                onClick = {
                                    onSetImageRotate(rotate)
                                    popupVisible.value = false
                                }
                            )
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
        AppListItem(
            title = stringResource(R.string.list_action_delete_image),
            titleColor = MaterialTheme.colors.error,
            onClick = onDeleteImage
        )
        AppDescriptionItem(
            text = stringResource(R.string.rooms_vdr_watermark_image_desc),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun QuotaBlock(
    state: RoomSettingsState,
    onSetEnabled: (Boolean) -> Unit,
    onSetValue: (Long) -> Unit,
    onSetMeasurementUnit: (SizeUnit) -> Unit
) {
    Column {
        AppSwitchItem(
            title = stringResource(R.string.rooms_vdr_storage_quota_title),
            checked = state.quota.enabled,
            onCheck = onSetEnabled
        )
        AnimatedVisibilityVerticalFade(visible = state.quota.enabled) {
            Column {
                AppTextField(
                    modifier = Modifier.padding(start = 16.dp),
                    value = state.quota.value.toString(),
                    onValueChange = { value ->
                        if (!value.isDigitsOnly()) return@AppTextField
                        val digitValue = if (value.isEmpty()) 0 else value.toLong()
                        onSetValue(digitValue)
                    },
                    keyboardType = KeyboardType.Number,
                    label = R.string.rooms_vdr_size_quota,
                )
                AppListItem(
                    title = stringResource(R.string.rooms_vdr_measurement_unit),
                    endContent = {
                        val popupVisible = remember { mutableStateOf(false) }

                        DropdownMenuButton(
                            title = stringResource(state.quota.unit.title),
                            state = popupVisible,
                            items = {
                                SizeUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        title = stringResource(unit.title),
                                        selected = state.quota.unit == unit,
                                        onClick = {
                                            onSetMeasurementUnit(unit)
                                            popupVisible.value = false
                                        }
                                    )
                                }
                            },
                            onDismiss = { popupVisible.value = false }
                        ) { popupVisible.value = true }
                    }
                )
            }
        }
        AppDescriptionItem(
            text = stringResource(R.string.rooms_vdr_storage_quota_desc),
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun LifeTimeBlock(
    lifetime: Lifetime,
    onSetValue: (Int) -> Unit,
    onSetPeriod: (Int) -> Unit,
    onSetAction: (Boolean) -> Unit
) {
    Column {
        AppTextField(
            modifier = Modifier.padding(start = 16.dp),
            value = lifetime.value.toString(),
            onValueChange = { value ->
                if (!value.isDigitsOnly() || value.length > 3) return@AppTextField
                val digitValue = if (value.isEmpty()) 0 else value.toInt()
                onSetValue(digitValue)
            },
            keyboardType = KeyboardType.Number,
            label = R.string.file_lifetime_hint,
        )
        AppListItem(
            title = stringResource(R.string.file_lifetime_time_period),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (lifetime.period) {
                            1 -> R.string.file_lifetime_period_months
                            2 -> R.string.file_lifetime_period_years
                            else -> R.string.file_lifetime_period_days
                        }
                    ),
                    state = popupVisible,
                    items = {
                        mapOf(
                            Lifetime.PERIOD_DAYS to R.string.file_lifetime_period_days,
                            Lifetime.PERIOD_MONTHS to R.string.file_lifetime_period_months,
                            Lifetime.PERIOD_YEARS to R.string.file_lifetime_period_years,
                        ).forEach { (period, title) ->
                            DropdownMenuItem(
                                title = stringResource(title),
                                selected = lifetime.period == period
                            ) {
                                onSetPeriod(period)
                                popupVisible.value = false
                            }
                        }
                    },
                    onDismiss = { popupVisible.value = false }
                ) { popupVisible.value = true }
            }
        )
        AppListItem(
            title = stringResource(R.string.file_lifetime_action),
            endContent = {
                val popupVisible = remember { mutableStateOf(false) }

                DropdownMenuButton(
                    title = stringResource(
                        when (lifetime.deletePermanently) {
                            true -> R.string.file_lifetime_action_delete
                            else -> R.string.file_lifetime_action_move_to_trash
                        }
                    ),
                    state = popupVisible,
                    onDismiss = { popupVisible.value = false },
                    items = {
                        mapOf(
                            true to R.string.file_lifetime_action_move_to_trash,
                            false to R.string.file_lifetime_action_delete
                        ).forEach { (state, title) ->
                            DropdownMenuItem(
                                title = stringResource(title),
                                selected = lifetime.deletePermanently == state
                            ) {
                                onSetAction(state)
                                popupVisible.value = false
                            }
                        }
                    }
                ) { popupVisible.value = true }
            }
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun ChooseImageBottomView(
    onDelete: (() -> Unit)? = null,
    onSuccess: (Uri?) -> Unit,
) {
    val context = LocalContext.current
    var photo: Uri? = null

    val photoLauncher = rememberLauncherForActivityResult(
        contract = TakePicture(),
        onResult = { success ->
            if (success && photo != null) {
                onSuccess.invoke(photo)
            }
        }
    )

    val cameraPermission = rememberLauncherForActivityResult(
        contract = RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                photo?.let(photoLauncher::launch)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = onSuccess::invoke
    )

    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(MaterialTheme.colors.surface)
    ) {
        Image(
            painter = painterResource(id = lib.toolkit.base.R.drawable.ic_bottom_divider),
            contentDescription = null,
            alignment = Alignment.TopCenter,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.onSurface),
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        )
        AppListItem(
            title = stringResource(id = R.string.list_action_photo),
            startIcon = R.drawable.ic_list_action_photo,
            startIconTint = MaterialTheme.colors.colorTextSecondary,
            dividerVisible = false,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            FileUtils.createFile(
                dir = File(context.cacheDir.absolutePath),
                name = TimeUtils.fileTimeStamp,
                extension = "png"
            )?.also { tempPhoto ->
                photo = ContentResolverUtils.getFileUri(context, tempPhoto)
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
        AppListItem(
            title = stringResource(id = R.string.list_action_image_from_library),
            startIcon = lib.toolkit.base.R.drawable.ic_image,
            startIconTint = MaterialTheme.colors.colorTextSecondary,
            dividerVisible = onDelete != null,
            onClick = { galleryLauncher.launch("image/*") }
        )
        onDelete?.let {
            AppArrowItem(
                title = stringResource(id = R.string.list_action_delete_image),
                titleColor = MaterialTheme.colors.error,
                startIcon = R.drawable.ic_trash,
                startIconTint = MaterialTheme.colors.error,
                arrowVisible = false,
                dividerVisible = false,
                onClick = onDelete
            )
        }
    }
}

@Preview
@Composable
private fun TextFieldPreview() {
    ManagerTheme {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .padding(16.dp)
        ) {
            ChipsTextField(
                label = "Add tag",
                chips = ChipList(listOf("one", "two", "two", "three")),
                onChipAdd = {},
                onChipDelete = {}
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    ManagerTheme {
        RoomSettingsScreen(
            isEdit = false,
            canApplyChanges = true,
            isRoomTypeEditable = false,
            state = remember { RoomSettingsState(type = ApiContract.RoomType.VIRTUAL_ROOM) },
            logoState = remember { RoomSettingsLogoState() },
            watermarkState = remember { RoomSettingsWatermarkState() },
            loadingState = remember { false },
            onApply = {},
            onSetOwner = {},
            onLocationClick = {},
            onCreateNewFolder = {},
            onStorageConnect = {},
            onSetImage = { _, _ -> },
            onSelectType = {},
            onSetName = {},
            onAddTag = {},
            onRemoveTag = {},
            onSetIndexing = {},
            onSetRestrict = {},
            onSetLifetimeEnable = {},
            onSetLifetimeValue = {},
            onSetLifetimePeriod = {},
            onSetLifetimeAction = {},
            onWatermarkEnable = {},
            onSetWatermarkAddition = {},
            onSetWatermarkStaticText = {},
            onSetWatermarkTextPosition = {},
            onSetWatermarkImageScale = {},
            onSetWatermarkImageRotate = {},
            onSetWatermarkType = {},
            onSetQuotaEnabled = {},
            onSetQuotaValue = {},
            onSetQuotaMeasurementUnit = {},
            onBack = {}
        )
    }
}

@Preview
@Composable
private fun SelectScreenPreview() {
    ManagerTheme {
        RoomSettingsSelectRoomScreen(2, navController = rememberNavController()) {}
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview
@Composable
private fun SelectImagePreview() {
    ManagerTheme {
        Surface {
            ChooseImageBottomView(
                onDelete = {},
                onSuccess = {}
            )
        }
    }
}