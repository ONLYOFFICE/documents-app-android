package app.editors.manager.ui.views.custom

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.editors.manager.R
import app.editors.manager.databinding.IncludePlaceholdersImageBinding
import app.editors.manager.managers.utils.isVisible
import lib.toolkit.base.managers.tools.ResourcesProvider
import lib.toolkit.base.managers.utils.UiUtils

class PlaceholderViews(val view: View) {

    enum class Type {
        NONE, CONNECTION, EMPTY, SEARCH, SHARE, ACCESS,
        SUBFOLDER, USERS, GROUPS, COMMON, MEDIA, LOAD
    }

    interface OnClickListener {
        fun onRetryClick()
    }

    private var viewBinding = IncludePlaceholdersImageBinding.bind(view)
    private var mViewForHide: View? = null
    private var mOnClickListener: OnClickListener? = null

    init {
        viewBinding.placeholderLayout.isVisible = true
        viewBinding.placeholderRetry.setOnClickListener {
            mOnClickListener?.onRetryClick()
        }
    }

    fun setVisibility(isVisible: Boolean) {
        viewBinding.placeholderLayout.isVisible = isVisible
        mViewForHide?.isVisible = isVisible
    }

    fun setViewForHide(viewForHide: View?) {
        mViewForHide = viewForHide
    }

    fun setTitle(@StringRes resId: Int) {
        viewBinding.placeholderText.setText(resId)
    }

    private fun setTitleColor(@ColorRes resId: Int) {
        viewBinding.placeholderText.setTextColor(ResourcesProvider(view.context).getColor(resId))
    }

    fun setImage(@DrawableRes resId: Int) {
        viewBinding.placeholderImage.setImageResource(resId)
    }

    fun setImageTint(@ColorRes resId: Int) {
        UiUtils.setImageTint(viewBinding.placeholderImage, resId)
    }

    private fun setRetryTint(@ColorRes resId: Int) {
        viewBinding.placeholderRetry.setTextColor(ResourcesProvider(view.context).getColor(resId))
    }

    fun setTemplatePlaceholder(type: Type?) {
        when (type) {
            Type.CONNECTION -> setTitle(R.string.placeholder_connection)
            Type.EMPTY -> setTitle(R.string.placeholder_empty)
            Type.SEARCH -> setTitle(R.string.placeholder_search)
            Type.SHARE -> setTitle(R.string.placeholder_share)
            Type.ACCESS -> setTitle(R.string.placeholder_access_denied)
            Type.SUBFOLDER -> setTitle(R.string.placeholder_no_subfolders)
            Type.USERS -> setTitle(R.string.placeholder_no_users)
            Type.GROUPS -> setTitle(R.string.placeholder_no_groups)
            Type.COMMON -> setTitle(R.string.placeholder_no_users_groups)
            Type.LOAD -> setTitle(R.string.placeholder_loading_files)
            Type.MEDIA -> {
                setImage(R.drawable.ic_media_error)
                setImageTint(R.color.colorLightWhite)
                setTitle(R.string.placeholder_media_error)
                setTitleColor(R.color.colorLightWhite)
                setRetryTint(R.color.colorSecondary)
            }
            Type.NONE -> {
                setVisibility(false)
                return
            }
        }
        setVisibility(true)
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        mOnClickListener = onClickListener
    }
}