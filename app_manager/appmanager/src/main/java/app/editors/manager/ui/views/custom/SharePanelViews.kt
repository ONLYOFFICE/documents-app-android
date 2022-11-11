package app.editors.manager.ui.views.custom

import android.text.Editable
import android.view.View
import androidx.core.view.isVisible
import app.documents.core.network.ApiContract
import app.editors.manager.R
import app.editors.manager.databinding.IncludeSharePanelBinding
import app.editors.manager.mvp.models.explorer.CloudFolder
import app.editors.manager.mvp.models.explorer.Item
import app.editors.manager.ui.views.animation.HeightValueAnimator
import app.editors.manager.ui.views.edits.BaseWatcher
import app.editors.manager.ui.views.popup.SharePopup

class SharePanelViews(
    private val view: View,
    private val item: Item
) :
    HeightValueAnimator.OnAnimationListener {

    interface OnEventListener {
        fun onPanelAccessClick(accessCode: Int)
        fun onPanelResetClick()
        fun onPanelMessageClick(isShow: Boolean)
        fun onPanelAddClick()
        fun onMessageInput(message: String)
    }

    private val heightValueAnimator: HeightValueAnimator
    private var onEventListener: OnEventListener? = null
    private var sharePopup: SharePopup? = null
    private val popupAccessListener: PopupAccessListener
    private var viewBinding: IncludeSharePanelBinding? = null

    init {
        viewBinding = IncludeSharePanelBinding.bind(view)
        viewBinding?.sharePanelMessageEdit?.addTextChangedListener(FieldsWatcher())
        heightValueAnimator = HeightValueAnimator(viewBinding?.sharePanelMessageEditLayout)
        heightValueAnimator.setOnAnimationListener(this)
        popupAccessListener = PopupAccessListener()
        initListeners()
    }

    private fun initListeners() {
        viewBinding?.let { binding ->
            binding.buttonPopupLayout.buttonPopupLayout.setOnClickListener {
                sharePopup = SharePopup(
                        context = view.context,
                        layoutId = R.layout.popup_share_menu,
                    ).apply {
                        setItem(item)
                        setContextListener(popupAccessListener)
                        setExternalLink()
                        showOverlap(view, binding.root.height)
                    }
            }
            binding.sharePanelResetButton.setOnClickListener { onReset() }
            binding.sharePanelMessageButton.setOnClickListener { onMessage() }
            binding.sharePanelAddButton.setOnClickListener { onAdd() }
        }
    }

    private fun onReset() {
        viewBinding?.sharePanelCountSelectedText?.text = 0.toString()
        viewBinding?.sharePanelAddButton?.isEnabled = false
        onEventListener?.onPanelResetClick()
    }

    private fun onPopupAccess(accessCode: Int) {
        hideMessageView()
        setAccessIcon(accessCode)
        onEventListener?.onPanelAccessClick(accessCode)
    }

    private fun onMessage() {
        val isShowMessage = !isMessageShowed
        viewBinding?.sharePanelMessageEdit?.setText("")
        heightValueAnimator.animate(isShowMessage)
        onEventListener?.onPanelMessageClick(isShowMessage)
    }

    private fun onAdd() {
        hideMessageView()
        onEventListener?.onPanelAddClick()
    }

    private val isMessageShowed: Boolean
        get() = viewBinding?.sharePanelMessageEditLayout?.isVisible == true

    fun setAccessIcon(accessCode: Int) {
        viewBinding?.buttonPopupLayout?.let {
            when (accessCode) {
                ApiContract.ShareCode.NONE, ApiContract.ShareCode.RESTRICT -> it.buttonPopupImage.setImageResource(R.drawable.ic_access_deny)
                ApiContract.ShareCode.READ -> it.buttonPopupImage.setImageResource(R.drawable.ic_access_read)
                ApiContract.ShareCode.READ_WRITE -> {
                    if (item is CloudFolder && item.isRoom) {
                        it.buttonPopupImage.setImageResource(R.drawable.ic_drawer_menu_my_docs)
                    } else {
                        it.buttonPopupImage.setImageResource(R.drawable.ic_access_full)
                    }
                }
                ApiContract.ShareCode.REVIEW -> it.buttonPopupImage.setImageResource(R.drawable.ic_access_review)
                ApiContract.ShareCode.COMMENT -> it.buttonPopupImage.setImageResource(R.drawable.ic_access_comment)
                ApiContract.ShareCode.FILL_FORMS -> it.buttonPopupImage.setImageResource(R.drawable.ic_access_fill_form)
            }
        }
    }

    fun popupDismiss(): Boolean {
        sharePopup?.let {
            if (it.isShowing) {
                it.hide()
                return true
            }
        }
        return false
    }

    fun unbind() {
        heightValueAnimator.clear()
        viewBinding = null
    }

    fun setCount(count: Int) {
        viewBinding?.let {
            it.sharePanelResetButton.isClickable = count > 0
            it.sharePanelCountSelectedText.text = count.toString()
        }
    }

    fun setAddButtonEnable(isEnable: Boolean) {
        viewBinding?.sharePanelAddButton?.isEnabled = isEnable
    }

    fun setOnEventListener(onEventListener: OnEventListener?) {
        this.onEventListener = onEventListener
    }

    fun hideMessageView(): Boolean {
        heightValueAnimator.animate(false)
        return isMessageShowed
    }

    val message: String?
        get() {
            viewBinding?.let {
                if (it.buttonPopupLayout.buttonPopupLayout.isVisible) {
                    val message: String = it.sharePanelMessageEdit.text.toString().trim()
                    if (message.isNotEmpty()) {
                        return message
                    }
                }
            }
            return null
        }

    override fun onStart(isShow: Boolean) {}

    override fun onEnd(isShow: Boolean) {}

    /*
     * Popup callbacks
     * */
    private inner class PopupAccessListener : SharePopup.PopupContextListener {
        override fun onContextClick(v: View, sharePopup: SharePopup) {
            sharePopup.hide()
            when (v.id) {
                R.id.fullAccessItem -> onPopupAccess(ApiContract.ShareCode.READ_WRITE)
                R.id.reviewItem -> onPopupAccess(ApiContract.ShareCode.REVIEW)
                R.id.viewItem -> onPopupAccess(ApiContract.ShareCode.READ)
                R.id.denyItem -> onPopupAccess(ApiContract.ShareCode.NONE)
                R.id.commentItem -> onPopupAccess(ApiContract.ShareCode.COMMENT)
                R.id.fillFormItem -> onPopupAccess(ApiContract.ShareCode.FILL_FORMS)
            }
        }
    }

    /*
     * Text input listener
     * */
    private inner class FieldsWatcher : BaseWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            onEventListener?.onMessageInput(s.toString())
        }
    }
}