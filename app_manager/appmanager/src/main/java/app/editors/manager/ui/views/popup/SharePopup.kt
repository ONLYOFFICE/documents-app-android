package app.editors.manager.ui.views.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import app.editors.manager.databinding.PopupShareMenuBinding
import app.editors.manager.managers.utils.isVisible
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.popup.BasePopup

class SharePopup(context: Context, layoutId: Int) : BasePopup(context, layoutId) {

    private var viewBinding: PopupShareMenuBinding? = null
    private var contextListener: PopupContextListener? = null
    private var isFullAccess = false

    private var fullView: View? = null
    private var fillFormsView: View? = null
    private var commentView: View? = null
    private var reviewView: View? = null
    private var removeUser: View? = null

    var extension: StringUtils.Extension = StringUtils.Extension.UNKNOWN
        set(value) = when (value) {
            StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION -> disableViews(fillFormsView, reviewView)
            StringUtils.Extension.DOC, StringUtils.Extension.DOCXF -> disableViews(fillFormsView)
            StringUtils.Extension.OFORM -> disableViews(commentView, reviewView)
            else -> disableViews(commentView, reviewView, fillFormsView)
        }

    val isShowing: Boolean
        get() = popupWindow.isShowing

    override fun bind(view: View) {
        viewBinding = PopupShareMenuBinding.bind(view).apply {
            fullView = popupShareAccessFull.setOnClickListener()
            fillFormsView = popupShareAccessFillForms.setOnClickListener()
            commentView = popupShareAccessComment.setOnClickListener()
            reviewView = popupShareAccessReview.setOnClickListener()
            removeUser = popupShareAccessRemove.setOnClickListener()
            popupShareAccessRead.setOnClickListener()
            popupShareAccessDeny.setOnClickListener()

            if (isFullAccess) {
                popupShareAccessSeparatorDeny.root.isVisible = false
                popupShareAccessSeparatorRemove.root.isVisible = true
                popupShareAccessRemove.isVisible = true
            }
        }
    }

    override fun hide() {
        super.hide()
        viewBinding = null
    }

    private fun View.setOnClickListener(): View {
        setOnClickListener { v -> contextListener?.onContextClick(v, this@SharePopup) }
        return this
    }

    private fun disableViews(vararg views: View?) {
        views.forEach { it?.isVisible = false }
    }

    fun setExternalLink() {
        disableViews(removeUser)
    }

    fun setIsFolder() {
        disableViews(reviewView, fillFormsView, commentView)
    }

    fun setIsVisitor() {
        disableViews(reviewView, fillFormsView, commentView, fullView)
    }

    fun setFullAccess(isFullAccess: Boolean) {
        this.isFullAccess = isFullAccess
    }

    fun setContextListener(contextListener: PopupContextListener) {
        this.contextListener = contextListener
    }

    fun showOverlap(view: View, viewHeight: Int) {
        val offsetX = context.resources.getDimension(lib.toolkit.base.R.dimen.default_margin_medium).toInt()
        val offsetY = context.resources.getDimension(lib.toolkit.base.R.dimen.default_margin_xlarge).toInt()
        popupWindow.showAtLocation(view, Gravity.START or Gravity.BOTTOM, offsetX, viewHeight + offsetY)
    }

    interface PopupContextListener {
        fun onContextClick(v: View, sharePopup: SharePopup)
    }
}