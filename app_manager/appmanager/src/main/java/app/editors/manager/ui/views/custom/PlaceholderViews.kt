package app.editors.manager.ui.views.custom

import android.view.View
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.IncludePlaceholdersTextBinding
import app.editors.manager.ui.compose.personal.PersonalMigrationScreen
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.AppButton
import lib.compose.ui.views.PlaceholderView

data class PlaceholderConfig(
    val image: Int?,
    val titleRes: Int,
    val subtitleRes: Int? = null,
    val buttonTitleRes: Int? = null
)

class PlaceholderViews(val view: View?) {

    enum class Type {
        NONE,

        // Compose-based placeholders
        EMPTY, LOAD, EMPTY_TEMPLATE, EMPTY_TEMPLATES_FOLDER,
        EMPTY_ROOM, SEARCH, EMPTY_TRASH, EMPTY_ARCHIVE,
        VISITOR_EMPTY_ROOM, NO_ROOMS, VISITOR_NO_ROOMS,
        EMPTY_RECENT_VIA_LINK, PAYMENT_REQUIRED,
        EMPTY_FORM_FILLING_ROOM, EMPTY_VIRTUAL_ROOM,
        EXTERNAL_STORAGE, CONNECTION,

        // Special compose screen
        PERSONAL_PORTAL_END,

        // Simple text placeholders
        SHARE, ACCESS, SUBFOLDER, USERS, GROUPS, COMMON,
        MEDIA, LOAD_GROUPS, LOAD_USERS, OTHER_ACCOUNTS;

        val isComposeType: Boolean get() = this in EMPTY..CONNECTION
        val isPersonalPortalEnd: Boolean get() = this == PERSONAL_PORTAL_END
        val isSimpleText: Boolean get() = this in SHARE..OTHER_ACCOUNTS
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

        when {
            type == Type.NONE -> {
                setVisibility(false)
                return
            }

            type.isComposeType -> {
                showComposePlaceholder(type, onClick)
                return
            }

            type.isPersonalPortalEnd -> {
                showPersonalPortalEndScreen()
                return
            }

            type.isSimpleText -> {
                showSimpleText(getSimpleTextTitle(type))
            }
        }
    }

    private fun showComposePlaceholder(type: Type, onClick: () -> Unit) {
        setVisibility(true)
        with(binding.composeView) {
            isVisible = true
            setContent {
                ManagerTheme {
                    PlaceholderView(type = type, onClick = onClick)
                }
            }
        }
    }

    private fun showPersonalPortalEndScreen() {
        setVisibility(true)
        with(binding.composeView) {
            isVisible = true
            setContent {
                ManagerTheme {
                    PersonalMigrationScreen()
                }
            }
        }
    }

    private fun showSimpleText(@StringRes titleRes: Int) {
        setTitle(titleRes)
        setVisibility(true)
    }

    private fun getSimpleTextTitle(type: Type): Int = when (type) {
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
        else -> error("Invalid simple text type: $type")
    }

    @Composable
    private fun PlaceholderView(type: Type, onClick: () -> Unit = {}) {
        val context = LocalContext.current

        when (type) {
            Type.LOAD -> {
                ActivityIndicatorView(title = context.getString(R.string.placeholder_loading_files))
            }
            else -> {
                val config = getPlaceholderConfig(type)
                PlaceholderView(
                    image = config.image,
                    title = context.getString(config.titleRes),
                    subtitle = config.subtitleRes?.let(context::getString).orEmpty()
                ) {
                    config.buttonTitleRes?.let { titleRes ->
                        AppButton(
                            titleResId = titleRes,
                            onClick = onClick,
                            modifier = Modifier.testTag(type.name),
                        )
                    }
                }
            }
        }
    }

    private fun getPlaceholderConfig(type: Type): PlaceholderConfig = when (type) {
        Type.EMPTY -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.placeholder_empty_folder
        )

        Type.VISITOR_EMPTY_ROOM -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.placeholder_empty_folder,
            subtitleRes = R.string.placeholder_empty_room_visitor_desc
        )

        Type.EMPTY_TEMPLATES_FOLDER -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_screen,
            titleRes = R.string.placeholder_empty_templates_folder,
            subtitleRes = R.string.placeholder_empty_templates_folder_desc
        )

        Type.EMPTY_TEMPLATE -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.room_placeholder_created_template_title,
            subtitleRes = R.string.room_placeholder_created_room_subtitle
        )

        Type.EMPTY_ROOM -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.room_placeholder_created_room_title,
            subtitleRes = R.string.room_placeholder_created_room_subtitle
        )

        Type.EMPTY_FORM_FILLING_ROOM -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.room_placeholder_created_filling_form_room_title,
            subtitleRes = R.string.room_placeholder_created_filling_form_room_subtitle
        )

        Type.EMPTY_VIRTUAL_ROOM -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_virtual_room,
            titleRes = R.string.room_placeholder_created_virtual_room_title,
            subtitleRes = R.string.room_placeholder_created_virtual_room_subtitle
        )

        Type.SEARCH -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_no_search_result,
            titleRes = R.string.placeholder_no_search_result,
            subtitleRes = R.string.placeholder_no_search_result_desc
        )

        Type.EMPTY_TRASH -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_trash,
            titleRes = R.string.placeholder_empty_trash,
            subtitleRes = R.string.placeholder_empty_trash_desc
        )

        Type.EMPTY_ARCHIVE -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.placeholder_empty_archive,
            subtitleRes = R.string.placeholder_empty_archive_desc
        )

        Type.NO_ROOMS -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.placeholder_no_rooms,
            subtitleRes = R.string.placeholder_no_rooms_desc
        )

        Type.VISITOR_NO_ROOMS -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
            titleRes = R.string.placeholder_no_rooms,
            subtitleRes = R.string.placeholder_no_rooms_visitor_desc
        )

        Type.EMPTY_RECENT_VIA_LINK -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_empty_recently,
            titleRes = R.string.placeholder_empty_folder,
            subtitleRes = R.string.placeholder_empty_recent_via_link_desc
        )

        Type.PAYMENT_REQUIRED -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_payment_required,
            titleRes = R.string.placeholder_payment_required,
            subtitleRes = R.string.placeholder_payment_required_desc,
            buttonTitleRes = R.string.placeholder_payment_required_button
        )

        Type.EXTERNAL_STORAGE -> PlaceholderConfig(
            image = null,
            titleRes = R.string.app_manage_files_title,
            subtitleRes = R.string.app_manage_files_description,
            buttonTitleRes = R.string.settings_item_title
        )

        Type.CONNECTION -> PlaceholderConfig(
            image = lib.toolkit.base.R.drawable.placeholder_no_connection,
            titleRes = R.string.placeholder_connection,
            subtitleRes = R.string.placeholder_connection_desc,
            buttonTitleRes = R.string.placeholder_connection_button
        )

        else -> error("${type.name} is invalid type")
    }

    private fun setTitle(@StringRes resId: Int) {
        binding.placeholderText.setText(resId)
    }
}
