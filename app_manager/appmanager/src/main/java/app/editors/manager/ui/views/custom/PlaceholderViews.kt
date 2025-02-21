package app.editors.manager.ui.views.custom

import android.view.View
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.IncludePlaceholdersTextBinding
import app.editors.manager.ui.compose.personal.PersonalMigrationScreen
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.PlaceholderView

class PlaceholderViews(val view: View?) {

    enum class Type {
        NONE, CONNECTION, EMPTY, EMPTY_ROOM, EMPTY_FORM_FILLING_ROOM, EMPTY_VIRTUAL_ROOM, VISITOR_EMPTY_ROOM, SEARCH, SHARE, ACCESS,
        SUBFOLDER, USERS, GROUPS, COMMON, MEDIA, LOAD, LOAD_GROUPS, LOAD_USERS,
        OTHER_ACCOUNTS, EMPTY_TRASH, EMPTY_ARCHIVE, NO_ROOMS, VISITOR_NO_ROOMS,
        EMPTY_RECENT_VIA_LINK, PAYMENT_REQUIRED, PERSONAL_PORTAL_END, EXTERNAL_STORAGE
    }

    interface OnClickListener {
        fun onRetryClick() {}
    }

    private val requiredView: View = checkNotNull(view) { "View can not be null" }
    private var binding = IncludePlaceholdersTextBinding.bind(requiredView)
    private var viewForHide: View? = null

    var type: Type = Type.NONE
        private set

    init {
        binding.root.isVisible = false
        binding.composeView.isVisible = false
    }

    fun setVisibility(isVisible: Boolean) {
        binding.root.isVisible = isVisible
        viewForHide?.isVisible = !isVisible
    }

    fun setViewForHide(viewForHide: View?) {
        this.viewForHide = viewForHide
    }

    fun setTemplatePlaceholder(type: Type, onClick: () -> Unit = {}) {
        this.type = type
        binding.composeView.isVisible = false
        val title = when (type) {
            Type.NONE -> {
                setVisibility(false)
                return
            }
            Type.EMPTY, Type.LOAD, Type.EMPTY_ROOM, Type.SEARCH, Type.EMPTY_TRASH,
            Type.EMPTY_ARCHIVE, Type.VISITOR_EMPTY_ROOM, Type.NO_ROOMS, Type.VISITOR_NO_ROOMS,
            Type.EMPTY_RECENT_VIA_LINK, Type.PAYMENT_REQUIRED, Type.EMPTY_FORM_FILLING_ROOM,
            Type.EMPTY_VIRTUAL_ROOM, Type.EXTERNAL_STORAGE -> {
                setVisibility(true)
                with(binding.composeView) {
                    isVisible = true
                    setContent {
                        ManagerTheme {
                            PlaceholderView(type = type, onClick = onClick)
                        }
                    }
                }
                return
            }
            Type.PERSONAL_PORTAL_END -> {
                setVisibility(true)
                with(binding.composeView) {
                    isVisible = true
                    setContent {
                        ManagerTheme {
                            PersonalMigrationScreen()
                        }
                    }
                }
                return
            }
            Type.CONNECTION -> R.string.placeholder_connection
            Type.SHARE -> R.string.placeholder_share
            Type.ACCESS -> R.string.placeholder_access_denied
            Type.SUBFOLDER -> R.string.placeholder_no_subfolders
            Type.USERS -> R.string.placeholder_no_users
            Type.GROUPS -> R.string.placeholder_no_groups
            Type.COMMON -> R.string.placeholder_no_users_groups
            Type.LOAD_USERS -> R.string.placeholder_loading_users
            Type.LOAD_GROUPS -> R.string.placeholder_loading_groups
            Type.OTHER_ACCOUNTS -> R.string.placeholder_other_accounts
            Type.MEDIA -> R.string.placeholder_media_error
        }
        setTitle(title)
        setVisibility(true)
    }

    @Composable
    private fun PlaceholderView(type: Type, onClick: () -> Unit = {}) {
        val context = LocalContext.current
        when (type) {
            Type.LOAD -> ActivityIndicatorView(title = context.getString(R.string.placeholder_loading_files))
            else -> {
                val image: Int?
                val title: Int
                val subtitle: Int?

                when (type) {
                    Type.EMPTY -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_empty_folder
                        subtitle = null
                    }
                    Type.VISITOR_EMPTY_ROOM -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_empty_folder
                        subtitle = R.string.placeholder_empty_room_visitor_desc
                    }
                    Type.EMPTY_ROOM -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.room_placeholder_created_room_title
                        subtitle = R.string.room_placeholder_created_room_subtitle
                    }
                    Type.EMPTY_FORM_FILLING_ROOM -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.room_placeholder_created_filling_form_room_title
                        subtitle = R.string.room_placeholder_created_filling_form_room_subtitle
                    }
                    Type.EMPTY_VIRTUAL_ROOM -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_virtual_room
                        title = R.string.room_placeholder_created_virtual_room_title
                        subtitle = R.string.room_placeholder_created_virtual_room_subtitle
                    }
                    Type.SEARCH -> {
                        image = lib.toolkit.base.R.drawable.placeholder_no_search_result
                        title = R.string.placeholder_no_search_result
                        subtitle = R.string.placeholder_no_search_result_desc
                    }
                    Type.EMPTY_TRASH -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_trash
                        title = R.string.placeholder_empty_folder
                        subtitle = R.string.placeholder_empty_trash_desc
                    }
                    Type.EMPTY_ARCHIVE -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_empty_archive
                        subtitle = R.string.placeholder_empty_archive_desc
                    }
                    Type.NO_ROOMS -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_no_rooms
                        subtitle = R.string.placeholder_no_rooms_desc
                    }
                    Type.VISITOR_NO_ROOMS -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_no_rooms
                        subtitle = R.string.placeholder_no_rooms_visitor_desc
                    }
                    Type.EMPTY_RECENT_VIA_LINK -> {
                        image = lib.toolkit.base.R.drawable.placeholder_empty_folder
                        title = R.string.placeholder_empty_folder
                        subtitle = R.string.placeholder_empty_recent_via_link_desc
                    }
                    Type.PAYMENT_REQUIRED -> {
                        image = lib.toolkit.base.R.drawable.placeholder_payment_required
                        title = R.string.placeholder_payment_required
                        subtitle = R.string.placeholder_payment_required_desc
                    }
                    Type.EXTERNAL_STORAGE -> {
                        image = null
                        title = R.string.app_manage_files_title
                        subtitle = R.string.app_manage_files_description
                    }
                    else -> error("${type.name} is invalid type")
                }

                PlaceholderView(
                    image = image,
                    title = context.getString(title),
                    subtitle = subtitle?.let(context::getString).orEmpty()
                ) {
                    if (type == Type.PAYMENT_REQUIRED) {
                        AppButton(
                            title = R.string.placeholder_payment_required_button,
                            onClick = onClick
                        )
                    }
                    if (type == Type.EXTERNAL_STORAGE) {
                        AppButton(
                            title = R.string.settings_item_title,
                            onClick = onClick
                        )
                    }
                }
            }
        }
    }

    private fun setTitle(@StringRes resId: Int) {
        binding.placeholderText.setText(resId)
    }

}
