package app.editors.manager.ui.views.custom

import android.content.Context
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import app.editors.manager.R
import app.editors.manager.databinding.IncludePlaceholdersTextBinding
import lib.compose.ui.theme.ManagerTheme
import lib.compose.ui.views.ActivityIndicatorView
import lib.compose.ui.views.PlaceholderView
import lib.toolkit.base.managers.tools.ResourcesProvider

class PlaceholderViews(val view: View?) {

    enum class Type {
        NONE, CONNECTION, EMPTY, EMPTY_ROOM, SEARCH, SHARE, ACCESS,
        SUBFOLDER, USERS, GROUPS, COMMON, MEDIA, LOAD, LOAD_GROUPS, LOAD_USERS, OTHER_ACCOUNTS
    }

    interface OnClickListener {
        fun onRetryClick()
    }

    private val context: Context = view?.context ?: throw RuntimeException("View can not be null")
    private var binding = IncludePlaceholdersTextBinding.bind(view!!)
    private var viewForHide: View? = null
    private var onClickListener: OnClickListener? = null

    init {
        binding.root.isVisible = false
        //        imageBinding.placeholderRetry.setOnClickListener {
        //            mOnClickListener?.onRetryClick()
        //        }
    }

    fun setVisibility(isVisible: Boolean) {
        binding.root.isVisible = isVisible
        viewForHide?.isVisible = !isVisible
    }

    fun setViewForHide(viewForHide: View?) {
        this.viewForHide = viewForHide
    }

    fun setTitle(@StringRes resId: Int) {
        binding.placeholderText.setText(resId)
    }

    private fun setTitleColor(@ColorRes resId: Int) {
        binding.placeholderText.setTextColor(ResourcesProvider(context).getColor(resId))
    }

    fun setImage(@DrawableRes resId: Int) {
        //        imageBinding.placeholderImage.setImageResource(resId)
    }

    fun setImageTint(@ColorRes resId: Int) {
        //        UiUtils.setImageTint(imageBinding.placeholderImage, resId)
    }

    private fun setRetryTint(@ColorRes resId: Int) {
        //        imageBinding.placeholderRetry.setTextColor(ResourcesProvider(context).getColor(resId))
    }

    fun setTemplatePlaceholder(type: Type?, onButtonClick: () -> Unit = {}) {
        binding.composeView.isVisible = false
        when (type) {
            Type.NONE, null -> {
                setVisibility(false)
                return
            }
            Type.CONNECTION -> setTitle(R.string.placeholder_connection)
            Type.EMPTY -> setTitle(R.string.placeholder_empty)
            Type.SEARCH -> setTitle(R.string.placeholder_search)
            Type.SHARE -> setTitle(R.string.placeholder_share)
            Type.ACCESS -> setTitle(R.string.placeholder_access_denied)
            Type.SUBFOLDER -> setTitle(R.string.placeholder_no_subfolders)
            Type.USERS -> setTitle(R.string.placeholder_no_users)
            Type.GROUPS -> setTitle(R.string.placeholder_no_groups)
            Type.COMMON -> setTitle(R.string.placeholder_no_users_groups)
            Type.LOAD -> {
                with(binding.composeView) {
                    isVisible = true
                    setContent {
                        ManagerTheme {
                            ActivityIndicatorView(
                                title = context?.getString(R.string.placeholder_loading_files)
                            )
                        }
                    }
                }
            }
            Type.LOAD_USERS -> setTitle(R.string.placeholder_loading_users)
            Type.LOAD_GROUPS -> setTitle(R.string.placeholder_loading_groups)
            Type.OTHER_ACCOUNTS -> {
                setTitle(R.string.placeholder_other_accounts)
                binding.inviteByEmailButton.isVisible = true
                binding.inviteByEmailButton.setOnClickListener { onButtonClick.invoke() }
            }
            Type.MEDIA -> {
                setImage(R.drawable.ic_media_error)
                setImageTint(lib.toolkit.base.R.color.colorTextSecondary)
                setTitle(R.string.placeholder_media_error)
                setTitleColor(lib.toolkit.base.R.color.colorTextSecondary)
                setRetryTint(lib.toolkit.base.R.color.colorSecondary)
            }
            Type.EMPTY_ROOM -> {
                with(binding.composeView) {
                    isVisible = true
                    setContent {
                        ManagerTheme {
                            PlaceholderView(
                                image = lib.toolkit.base.R.drawable.placeholder_empty_folder,
                                title = context.getString(R.string.room_placeholder_created_room_title),
                                subtitle = context.getString(R.string.room_placeholder_created_room_subtitle)
                            )
                        }
                    }
                }
            }
        }
        setVisibility(true)
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
}
