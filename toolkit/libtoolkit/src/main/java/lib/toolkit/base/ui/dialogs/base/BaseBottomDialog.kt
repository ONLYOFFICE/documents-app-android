package lib.toolkit.base.ui.dialogs.base

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.holders.WaitingHolder
import moxy.MvpBottomSheetDialogFragment

abstract class BaseBottomDialog : MvpBottomSheetDialogFragment(), DialogInterface.OnShowListener,
    BaseInterfaceDialog, CommonDialog.OnClickListener {


    interface OnBottomDialogCloseListener {
        fun onBottomDialogClose()
    }

    companion object {
        val TAG = BaseBottomDialog::class.java.simpleName
    }

    protected lateinit var mBaseActivity: BaseActivity
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private var mBottomSheetCallback: BottomSheetDialogCallback? = null
    private var mCloseHandler: Handler = Handler()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mBaseActivity = context as BaseActivity
        } catch (e: ClassCastException) {
            throw RuntimeException("${BaseBottomDialog::class.java.simpleName} - must implement - ${BaseActivity::class.java.simpleName}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFragmentIsEmpty()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return getCustomDialog().apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(true)
            setOnShowListener(this@BaseBottomDialog)
            if (isTablet()) {
                setVisibleView(window?.decorView)
            }
        }
    }

    private fun setVisibleView(decorView: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            decorView?.isInvisible = true
        }
    }

    override fun onBackPressed(): Boolean {
        mCloseHandler.postDelayed({
            dismiss()
        }, 100)
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBottomSheetCallback?.let { mBottomSheetBehavior.removeBottomSheetCallback(it) }
    }
    override fun onDialogAdded() {

    }

    override fun onShowContentFragment() {

    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        Log.d(TAG, "onAcceptClick() - $dialogs - value: $value - tag: $tag")
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        Log.d(TAG, "onCancelClick() - $dialogs - tag: $tag")
        hideDialog()
    }

    protected fun hideDialog() {
        mBaseActivity.hideDialog()
    }

    protected fun showWaitingDialog(title: String?, cancelButton: String?, tag: String?) {
        mBaseActivity.setDialogListener(this)
        mBaseActivity.showWaitingDialog(title, cancelButton, WaitingHolder.ProgressType.HORIZONTAL, tag)
    }

    override fun onShow(dialogInterface: DialogInterface) {
        if (isTablet()) {
            setLayout()
        }
        setBottomSheet(dialogInterface)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        mCloseHandler.removeCallbacksAndMessages(null)
        val fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            super.show(manager, tag)
        } else {
            onDialogAdded()
        }
    }

    protected fun getCustomDialog(): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                this@BaseBottomDialog.onBackPressed()
            }
        }
    }

    protected open fun setLayout() {
        dialog?.window?.apply {
            setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

            if (isTablet()) {
                setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
            } else {
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            }
            if (decorView.isInvisible) {
                decorView.isVisible = true
            }
        }
    }

    protected fun setBottomSheet(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        mBottomSheetCallback = BottomSheetDialogCallback()
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
        mBottomSheetBehavior.peekHeight = 0
        mBottomSheetCallback?.let { mBottomSheetBehavior.addBottomSheetCallback(it) }
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    protected open fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
            dismiss()
        }
    }

    protected fun onSlide(bottomSheet: View, slideOffset: Float) {

    }

    protected fun isTablet(): Boolean {
        return UiUtils.isTablet(requireContext())
    }

    protected fun isLandscape(): Boolean {
        return UiUtils.isLandscape(requireContext())
    }

    protected fun showFragmentIsEmpty() {
        if (childFragmentManager.fragments.isEmpty()) {
            onShowContentFragment()
        }
        mBaseActivity.setDialogListener(this)
    }

    /*
    * Common callback on bottom sheet state
    * */
    protected inner class BottomSheetDialogCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            this@BaseBottomDialog.onStateChanged(bottomSheet, newState)
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            this@BaseBottomDialog.onSlide(bottomSheet, slideOffset)
            if (!isTablet() && isLandscape() && slideOffset <= 0.0f) {
                dismiss()
            }
        }
    }

}
