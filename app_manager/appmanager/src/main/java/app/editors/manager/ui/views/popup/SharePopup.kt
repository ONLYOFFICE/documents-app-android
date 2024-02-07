package app.editors.manager.ui.views.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.documents.core.network.common.contracts.ApiContract
import app.documents.core.network.manager.models.explorer.CloudFolder
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.BuildConfig
import app.editors.manager.R
import app.editors.manager.databinding.PopupShareMenuBinding
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.ui.popup.BasePopup

class SharePopup(
    context: Context,
    layoutId: Int
) : BasePopup(context, layoutId) {

    private var viewBinding: PopupShareMenuBinding? = null
    private var contextListener: PopupContextListener? = null
    private var isFullAccess = false

    var isVisitor: Boolean = false
    var isExternal: Boolean = true

    val isShowing: Boolean
        get() = popupWindow.isShowing

    override fun bind(view: View) {
        viewBinding = PopupShareMenuBinding.bind(view).apply {
            arrayOf(
                fullAccessItem.popupItemLayout,
                powerUserItem.popupItemLayout,
                fillFormItem.popupItemLayout,
                commentItem.popupItemLayout,
                reviewItem.popupItemLayout,
                editorItem.popupItemLayout,
                deleteItem.popupItemLayout,
                viewItem.popupItemLayout,
                denyItem.popupItemLayout,
                customFilterItem.popupItemLayout
            ).forEach { view ->
                view.setOnClickListener {
                    contextListener?.onContextClick(view, this@SharePopup)
                }
            }
        }
    }

    override fun hide() {
        super.hide()
        viewBinding = null
    }

    private fun disableViews(vararg views: View?) {
        views.forEach { it?.isVisible = false }
    }

    fun setExternalLink() {
        isExternal = true
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

    fun setItem(item: Item) {
        setUi(item)
    }

    private fun setUi(item: Item) {
        if (item is CloudFolder) {
            when {
                item.isRoom -> {
                    setRoomState(item)
                }
                else -> {
                    setFolderState()
                }
            }
        } else {
            setFileState(item.title)
        }
    }

    private fun setFileState(title: String) {
        viewBinding?.let { binding ->
            binding.fullAccessItem.popupItemLayout.isVisible = true
            binding.fullAccessItem.itemIcon.setImageResource(R.drawable.ic_access_full)
            binding.fullAccessItem.itemText.setText(R.string.share_popup_access_full)

            binding.viewItem.popupItemLayout.isVisible = true
            binding.viewItem.itemIcon.setImageResource(R.drawable.ic_access_read)
            binding.viewItem.itemText.setText(R.string.share_popup_access_read_only)

            binding.denyItem.popupItemLayout.isVisible = true
            binding.denyItem.itemIcon.setImageResource(R.drawable.ic_access_deny)
            binding.denyItem.itemText.setText(R.string.share_popup_access_deny_access)

            if (isFullAccess) {
                setDeleteItem()
            }

            binding.popupShareAccessSeparatorDeny.root.isVisible = true

            if (isVisitor) {
                return
            }

            setCommentItem(true)
            setReviewItem(true)
            setFillFormItem(true)

        }
        setItemsForExtension(title)
    }

    @Suppress("KotlinConstantConditions")
    private fun setItemsForExtension(title: String) {
        val extString = StringUtils.getExtensionFromPath(title)
        when (val ext = StringUtils.getExtension(extString).takeIf { it != StringUtils.Extension.FORM }
            ?: StringUtils.getFormExtension(extString)) {
            StringUtils.Extension.SHEET, StringUtils.Extension.PRESENTATION -> {
                setFillFormItem(false)
                setReviewItem(false)
                if (ext == StringUtils.Extension.SHEET) setCustomItem(true)
            }
            StringUtils.Extension.DOC, StringUtils.Extension.DOCXF -> {
                setFillFormItem(false)
            }
            StringUtils.Extension.OFORM -> {
                if (BuildConfig.APPLICATION_ID == "com.onlyoffice.documents") {
                    setCommentItem(false)
                    setReviewItem(false)
                } else {
                    setCommentItem(false)
                    setReviewItem(false)
                    setFillFormItem(false)
                }
            }
            else -> {
                setCommentItem(false)
                setReviewItem(false)
                setFillFormItem(false)
            }
        }
    }

    private fun setCustomItem(isVisible: Boolean) {
        viewBinding?.let { binding ->
            binding.customFilterItem.popupItemLayout.isVisible = isVisible
            binding.customFilterItem.itemIcon.setImageResource(R.drawable.ic_access_custom_filter)
            binding.customFilterItem.itemText.setText(R.string.share_popup_access_custom_filter)
        }
    }

    private fun setCommentItem(isVisible: Boolean) {
        viewBinding?.let { binding ->
            binding.commentItem.popupItemLayout.isVisible = isVisible
            binding.commentItem.itemIcon.setImageResource(R.drawable.ic_access_comment)
            binding.commentItem.itemText.setText(R.string.share_popup_access_comment)
        }
    }

    private fun setReviewItem(isVisible: Boolean) {
        viewBinding?.let { binding ->
            binding.reviewItem.popupItemLayout.isVisible = isVisible
            binding.reviewItem.itemIcon.setImageResource(R.drawable.ic_access_review)
            binding.reviewItem.itemText.setText(R.string.share_popup_access_review)
        }
    }

    private fun setFillFormItem(isVisible: Boolean) {
        viewBinding?.let { binding ->
            binding.fillFormItem.popupItemLayout.isVisible = isVisible
            binding.fillFormItem.itemIcon.setImageResource(R.drawable.ic_access_fill_form)
            binding.fillFormItem.itemText.setText(R.string.share_popup_access_fill_forms)
        }
    }

    private fun setRoomState(cloudFolder: CloudFolder) {
        viewBinding?.let { binding ->
            binding.fullAccessItem.popupItemLayout.isVisible = true
            binding.fullAccessItem.itemIcon.setImageResource(R.drawable.ic_room_admin)
            binding.fullAccessItem.itemText.setText(R.string.share_access_room_admin)

            binding.powerUserItem.popupItemLayout.isVisible = true
            binding.powerUserItem.itemIcon.setImageResource(R.drawable.ic_room_power_user)
            binding.powerUserItem.itemText.setText(R.string.share_access_room_power_user)

            binding.fillFormItem.popupItemLayout.isVisible = true
            binding.fillFormItem.itemIcon.setImageResource(R.drawable.ic_access_fill_form)
            binding.fillFormItem.itemText.setText(R.string.share_access_room_form_filler)

            binding.reviewItem.popupItemLayout.isVisible = true
            binding.reviewItem.itemIcon.setImageResource(R.drawable.ic_access_review)
            binding.reviewItem.itemText.setText(R.string.share_access_room_reviewer)

            binding.commentItem.popupItemLayout.isVisible = true
            binding.commentItem.itemIcon.setImageResource(R.drawable.ic_access_comment)
            binding.commentItem.itemText.setText(R.string.share_access_room_commentator)

            binding.viewItem.popupItemLayout.isVisible = true
            binding.viewItem.itemIcon.setImageResource(R.drawable.ic_access_read)
            binding.viewItem.itemText.setText(R.string.share_access_room_viewer)

            if (isFullAccess) {
                binding.popupShareAccessSeparatorDeny.root.isVisible = true
                setDeleteItem()
            }

            when (cloudFolder.roomType) {

                ApiContract.RoomType.PUBLIC_ROOM -> {
                    binding.editorItem.popupItemLayout.isVisible = true
                    binding.editorItem.itemIcon.setImageResource(R.drawable.ic_access_full)
                    binding.editorItem.itemText.setText(R.string.share_access_room_editor)
                    binding.fillFormItem.popupItemLayout.isVisible = false
                    binding.reviewItem.popupItemLayout.isVisible = false
                    binding.commentItem.popupItemLayout.isVisible = false
                    binding.editorItem.popupItemLayout.isVisible = false
                    binding.viewItem.popupItemLayout.isVisible = false
                }
                ApiContract.RoomType.COLLABORATION_ROOM -> {
                    binding.editorItem.popupItemLayout.isVisible = true
                    binding.editorItem.itemIcon.setImageResource(R.drawable.ic_access_full)
                    binding.editorItem.itemText.setText(R.string.share_access_room_editor)
                    binding.viewItem.popupItemLayout.isVisible = true
                    binding.fillFormItem.popupItemLayout.isVisible = false
                    binding.reviewItem.popupItemLayout.isVisible = false
                    binding.commentItem.popupItemLayout.isVisible = false
                }
                ApiContract.RoomType.CUSTOM_ROOM -> {
                    binding.viewItem.popupItemLayout.isVisible = true
                    binding.editorItem.popupItemLayout.isVisible = true
                    binding.editorItem.itemIcon.setImageResource(R.drawable.ic_access_full)
                    binding.editorItem.itemText.setText(R.string.share_access_room_editor)
                }
            }
        }
    }

    private fun setDeleteItem() {
        viewBinding?.let { binding ->
            binding.deleteItem.popupItemLayout.isVisible = true
            binding.deleteItem.itemIcon.setImageResource(R.drawable.ic_trash)
            binding.deleteItem.itemIcon.setColorFilter(ContextCompat.getColor(context, lib.toolkit.base.R.color.colorError))

            binding.deleteItem.itemText.setText(R.string.list_context_delete)
            binding.deleteItem.itemText.setTextColor(ContextCompat.getColor(context, lib.toolkit.base.R.color.colorError))
        }
    }

    private fun setFolderState() {
        viewBinding?.let { binding ->
            binding.fullAccessItem.popupItemLayout.isVisible = true
            binding.fullAccessItem.itemIcon.setImageResource(R.drawable.ic_access_full)
            binding.fullAccessItem.itemText.setText(R.string.share_popup_access_full)

            binding.viewItem.popupItemLayout.isVisible = true
            binding.viewItem.itemIcon.setImageResource(R.drawable.ic_access_read)
            binding.viewItem.itemText.setText(R.string.share_popup_access_read_only)

            binding.denyItem.popupItemLayout.isVisible = true
            binding.denyItem.itemIcon.setImageResource(R.drawable.ic_access_deny)
            binding.denyItem.itemText.setText(R.string.share_popup_access_deny_access)

            binding.popupShareAccessSeparatorDeny.root.isVisible = true
        }
    }

    interface PopupContextListener {
        fun onContextClick(v: View, sharePopup: SharePopup)
    }
}