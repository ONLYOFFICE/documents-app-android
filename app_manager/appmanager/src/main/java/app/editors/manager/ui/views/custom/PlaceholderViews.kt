package app.editors.manager.ui.views.custom

import android.content.Context
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.editors.manager.R
import app.editors.manager.databinding.IncludePlaceholdersTextBinding
import app.editors.manager.managers.utils.isVisible
import lib.toolkit.base.managers.tools.ResourcesProvider

class PlaceholderViews(val view: View?) {

    enum class Type {
        NONE, CONNECTION, EMPTY, SEARCH, SHARE, ACCESS,
        SUBFOLDER, USERS, GROUPS, COMMON, MEDIA, LOAD
    }

    interface OnClickListener {
        fun onRetryClick()
    }

    private val context: Context = view?.context ?: throw RuntimeException("View can not be null")
    private var textBinding = IncludePlaceholdersTextBinding.bind(view!!)
    private var viewForHide: View? = null
    private var onClickListener: OnClickListener? = null

    init {
        textBinding.placeholderLayout.isVisible = false
//        imageBinding.placeholderRetry.setOnClickListener {
//            mOnClickListener?.onRetryClick()
//        }
    }

    fun setVisibility(isVisible: Boolean) {
        textBinding.placeholderLayout.isVisible = isVisible
        viewForHide?.isVisible = !isVisible
    }

    fun setViewForHide(viewForHide: View?) {
        this.viewForHide = viewForHide
    }

    fun setTitle(@StringRes resId: Int) {
        textBinding.placeholderText.setText(resId)
    }

    private fun setTitleColor(@ColorRes resId: Int) {
        textBinding.placeholderText.setTextColor(ResourcesProvider(context).getColor(resId))
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

    fun setTemplatePlaceholder(type: Type?) {
        when (type) {
            Type.NONE -> {
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
            Type.LOAD -> setTitle(R.string.placeholder_loading_files)
            Type.MEDIA -> {
                setImage(R.drawable.ic_media_error)
                setImageTint(lib.toolkit.base.R.color.colorLightWhite)
                setTitle(R.string.placeholder_media_error)
                setTitleColor(lib.toolkit.base.R.color.colorLightWhite)
                setRetryTint(lib.toolkit.base.R.color.colorSecondary)
            }
        }
        setVisibility(true)
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
}