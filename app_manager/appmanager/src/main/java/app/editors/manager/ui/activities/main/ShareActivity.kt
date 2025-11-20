@file:Suppress("FunctionName")

package app.editors.manager.ui.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.NetworkResult
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFile
import app.editors.manager.R
import app.editors.manager.app.accountOnline
import app.editors.manager.app.api
import app.editors.manager.app.roomProvider
import app.editors.manager.app.shareApi
import app.editors.manager.managers.tools.ShareData
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.compose.share.ShareDocSpaceScreen
import app.editors.manager.ui.compose.share.ShareScreen
import app.editors.manager.ui.fragments.share.link.LoadingPlaceholder
import app.editors.manager.viewModels.main.ShareFileViewModel
import lib.compose.ui.theme.BaseAppTheme
import lib.compose.ui.theme.LocalUseTabletPadding
import lib.compose.ui.views.AppScaffold
import lib.compose.ui.views.AppTopBar
import lib.compose.ui.views.PlaceholderView
import lib.toolkit.base.managers.utils.EditorsContract
import lib.toolkit.base.managers.utils.openSendTextActivity

class ShareActivity : BaseAppActivity() {

    companion object {
        private const val KEY_SHARE_DATA = "key_share_data"

        @JvmStatic
        fun show(fragment: Fragment, shareData: ShareData) {
            fragment.startActivityForResult(
                Intent(fragment.context, ShareActivity::class.java).apply {
                    putExtra(EditorsContract.EXTRA_ITEM_ID, shareData.itemId) // check editors
                    putExtra(KEY_SHARE_DATA, shareData)
                },
                REQUEST_ACTIVITY_SHARE
            )
        }
    }

    private val itemId: String by lazy {
        intent.getStringExtra(EditorsContract.EXTRA_ITEM_ID).orEmpty()
    }

    private val themeColor: Color? by lazy {
        intent?.getIntExtra(EditorsContract.EXTRA_THEME_COLOR, -1)?.let { Color(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: ShareFileViewModel = viewModel(
                factory = ShareFileViewModel.Factory(fileId = itemId)
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val color = themeColor ?: Color(getColor(lib.toolkit.base.R.color.colorPrimary))

            CompositionLocalProvider(LocalUseTabletPadding provides true) {
                BaseAppTheme(primaryColor = color) {
                    if (state is NetworkResult.Success) {
                        val file = (state as NetworkResult.Success<CloudFile>).data
                        val shareData = ShareData.from(
                            item = file,
                            roomType = ApiContract.SectionType.getRoomType(file.parentRoomType),
                        )

                        if (accountOnline.isDocSpace) {
                            ShareDocSpaceScreen(
                                roomProvider = roomProvider,
                                shareData = shareData,
                                useTabletPadding = true,
                                onClose = { finish() },
                                onSendLink = { link ->
                                    openSendTextActivity(
                                        title = getString(R.string.toolbar_menu_main_share),
                                        text = link
                                    )
                                },
                                onShowSnackbar = ::showSnackBar
                            )
                        } else {
                            ShareScreen(
                                shareData = shareData,
                                useTabletPaddings = true,
                                shareApi = shareApi,
                                managerService = api,
                                onClose = ::finish
                            )
                        }
                    } else {
                        AppScaffold(
                            useTablePaddings = true,
                            topBar = {
                                AppTopBar(
                                    title = R.string.share_title_main,
                                    backListener = ::finish,
                                )
                            }
                        ) {
                            if (state is NetworkResult.Error) {
                                PlaceholderView(
                                    image = null,
                                    title = stringResource(R.string.placeholder_connection),
                                    subtitle = ""
                                )
                            } else {
                                LoadingPlaceholder()
                            }
                        }
                    }
                }
            }
        }
    }
}