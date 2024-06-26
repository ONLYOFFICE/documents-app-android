package lib.toolkit.base.ui.fragments.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.snackbar.Snackbar
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.DocumentsPicker
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.PermissionUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.activities.base.BaseActivity
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.holders.EditLineHolder
import lib.toolkit.base.ui.dialogs.common.holders.EditMultilineHolder
import lib.toolkit.base.ui.dialogs.common.holders.InfoHolder
import lib.toolkit.base.ui.dialogs.common.holders.ProgressHolder
import lib.toolkit.base.ui.dialogs.common.holders.QuestionHolder
import lib.toolkit.base.ui.dialogs.common.holders.WaitingHolder
import moxy.MvpAppCompatFragment

abstract class BaseFragment : MvpAppCompatFragment(), BaseActivity.OnBackPressFragment,
    BaseActivity.OnDispatchTouchEvent, CommonDialog.OnClickListener {

    companion object {

        private val TAG: String = BaseFragment::class.java.simpleName
        private const val TAG_TITLE = "TAG_TITLE"
        private const val TAG_CAMERA = "TAG_CAMERA"

        protected const val PERMISSION_WRITE_STORAGE = 1
        protected const val PERMISSION_READ_STORAGE = 2
        protected const val PERMISSION_CAMERA = 3
    }

    // Leak memory
    protected var baseActivity: BaseActivity? = null
    protected var snackBar: Snackbar? = null
    protected var toast: Toast? = null

    protected var toolbarTitle: String? = null

    protected var cameraUri: Uri? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            baseActivity = context as BaseActivity
            baseActivity?.addDialogListener(this)
            addOnDispatchTouchEvent()
        } catch (e: ClassCastException) {
            throw RuntimeException(
                BaseFragment::class.java.simpleName + " - must implement - " +
                        BaseActivity::class.java.simpleName
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        toolbarTitle?.let { outState.putString(TAG_TITLE, it) }
        cameraUri?.let { outState.putParcelable(TAG_CAMERA, it) }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        baseActivity?.removeOnDispatchTouchEvent(this)
        baseActivity?.removeDialogListener(this)
        toast = null
        snackBar = null
    }

    override fun onDestroy() {
        super.onDestroy()
        baseActivity = null
    }

    override fun dispatchTouchEvent(ev: MotionEvent) {
        if (snackBar != null) {
            if (snackBar?.isShown == true) {
                UiUtils.hideSnackBarOnOutsideTouch(snackBar, ev)
            }
            snackBar = null
        }
    }

    protected open fun init(view: View, savedInstanceState: Bundle?) {
        toast = UiUtils.getToast(requireActivity())
        restoreStates(savedInstanceState)
    }

    private fun restoreStates(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if (it.containsKey(TAG_TITLE)) {
                setActionBarTitle(it.getString(TAG_TITLE))
            }

            if (it.containsKey(TAG_CAMERA)) {
                cameraUri = savedInstanceState.getParcelable(TAG_CAMERA)
            }
        }
    }

    protected open fun addOnDispatchTouchEvent() {
        baseActivity?.addOnDispatchTouchEvent(this)
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        Log.d(TAG, "onAcceptClick() - $dialogs - value: $value - tag: $tag")
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        Log.d(TAG, "onCancelClick() - $dialogs - tag: $tag")
        hideDialog()
    }

    protected fun hideDialog(forceHide: Boolean = false) {
        baseActivity?.hideDialog(forceHide)
    }

    protected fun getEditDialogDialog(
        title: String? = null,
        bottomTitle: String? = null,
        value: String? = null,
        editHint: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): EditLineHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getEditDialog(title, bottomTitle, value, editHint, acceptTitle, cancelTitle, tag)
    }

    protected fun getWaitingDialog(
        topTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): WaitingHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getWaitingDialog(topTitle, cancelTitle, tag)
    }

    protected fun getQuestionDialog(
        title: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        question: String? = null,
        tag: String? = null
    ): QuestionHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getQuestionDialog(title, acceptTitle, cancelTitle, question, tag)
    }

    protected fun getInfoDialog(
        title: String? = null,
        info: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): InfoHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getInfoDialog(title, info, cancelTitle, tag)
    }

    protected fun getEditMultilineDialog(
        title: String? = null,
        hint: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): EditMultilineHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getEditMultilineDialog(title, hint, acceptTitle, cancelTitle, tag)
    }

    protected fun getProgressDialog(
        title: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): ProgressHolder.Builder? {
        baseActivity?.addDialogListener(this)
        return baseActivity?.getProgressDialog(title, cancelTitle, tag)
    }

    protected fun showWaitingDialog(
        title: String,
        progressType: WaitingHolder.ProgressType = WaitingHolder.ProgressType.HORIZONTAL
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showWaitingDialog(title, null, progressType, null)
    }

    protected fun showWaitingDialog(
        title: String?,
        tag: String?,
        progressType: WaitingHolder.ProgressType = WaitingHolder.ProgressType.HORIZONTAL
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showWaitingDialog(title, null, progressType, tag)
    }

    protected fun showWaitingDialog(
        title: String?,
        cancelButton: String?,
        tag: String?,
        progressType: WaitingHolder.ProgressType = WaitingHolder.ProgressType.HORIZONTAL
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showWaitingDialog(title, cancelButton, progressType, tag)
    }

    protected fun showWaitingDialog(
        title: String?,
        tag: String?,
        type: WaitingHolder.ProgressType = WaitingHolder.ProgressType.HORIZONTAL,
        cancelButton: String?,
        gravity: Int,
        color: Int
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showWaitingDialog(title, cancelButton, type, tag, color, color, gravity)
    }

    protected fun showQuestionDialog(
        title: String,
        string: String?,
        acceptButton: String,
        cancelButton: String?,
        tag: String,
        acceptErrorTint: Boolean = false,
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showQuestionDialog(title, tag, acceptButton, cancelButton, string, acceptErrorTint)
    }

    protected fun showEditDialogCreate(
        title: String,
        value: String?,
        hint: String?,
        endHint: String?,
        tag: String,
        acceptButton: String?,
        cancelButton: String?
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showEditDialog(
            title = title,
            bottomTitle = null,
            value = value,
            editHint = hint,
            acceptTitle = acceptButton,
            cancelTitle = cancelButton,
            isPassword = false,
            error = null,
            tag = tag,
            suffix = endHint
        )
    }

    protected fun showEditDialogRename(
        title: String,
        value: String?,
        hint: String?,
        tag: String,
        acceptButton: String?,
        cancelButton: String?,
        suffix: String?
    ) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showEditDialog(
            title = title,
            bottomTitle = null,
            value = value,
            editHint = hint,
            acceptTitle = acceptButton,
            cancelTitle = cancelButton,
            isPassword = false,
            error = null,
            tag = tag,
            suffix = suffix
        )
    }


    protected fun showProgressDialog(title: String?, isHideButton: Boolean, cancelTitle: String?, tag: String?) {
        baseActivity?.addDialogListener(this)
        baseActivity?.showProgressDialog(title, isHideButton, cancelTitle, tag)
    }

    protected fun updateProgressDialog(total: Int, progress: Int) {
        baseActivity?.updateProgressDialog(total, progress)
    }

    /*
    * SnackBar
    * */

    /*
        Fix SnackBar memory leak
        https://stackoverflow.com/questions/55697904/is-there-a-reason-for-why-accessibilitymanager-sinstance-would-cause-a-memory-leak?
     */
    protected fun showSnackBar(@StringRes resource: Int) {
        return showSnackBar(resources.getString(resource))
    }

    protected fun showSnackBar(string: String) {
        if (view == null) {
            return
        }
        val snackbar: Snackbar = UiUtils.getSnackBar(requireView())
        snackbar.setText(string)
        snackbar.setAction(null, null)
        snackbar.show()
        snackBar = snackbar
    }

    protected fun showSnackBarWithAction(
        text: String, button: String,
        onClickListener: View.OnClickListener
    ): Snackbar {
        val snackbar: Snackbar = UiUtils.getSnackBar(requireView())
        snackbar.setText(text)
        snackbar.setAction(button, onClickListener)
        snackbar.show()
        snackBar = snackbar
        return snackbar
    }

    protected fun showSnackBarWithAction(
        @StringRes text: Int, @StringRes button: Int,
        onClickListener: View.OnClickListener
    ): Snackbar {
        return showSnackBarWithAction(getString(text), getString(button), onClickListener)
    }

    /*
    * Toast
    * */
    protected fun showToast(@StringRes resource: Int) {
        showToast(resources.getString(resource))
    }

    protected fun showToast(string: String) {
        toast?.setText(string)
        toast?.show()
    }

    /*
    * Fragment operations
    * */
    protected fun showRootFragment() {
        FragmentUtils.backToRoot(parentFragmentManager)
    }

    protected fun showParentRootFragment() {
        FragmentUtils.backToRoot(requireParentFragment().parentFragmentManager)
    }

    /*
    * Action bar
    * */
    protected fun setActionBarTitle(title: String?) {
        if (title != null && isVisible) {
            toolbarTitle = title
            supportActionBar?.title = toolbarTitle
        }
    }

    protected val supportActionBar: ActionBar?
        get() = (activity as? AppCompatActivity)?.supportActionBar


    /*
    * Helpers methods
    * */
    protected val isTablet: Boolean
        get() = UiUtils.isTablet(requireContext())

    protected val isLandscape: Boolean
        get() = UiUtils.isLandscape(requireContext())

    /*
    * Keyboard
    * */
    protected fun copyToClipboard(value: String) {
        copyToClipboard(null, value, null)
    }

    protected fun copyToClipboard(label: String?, value: String?, message: String?) {
        KeyboardUtils.setDataToClipboard(requireContext(), value ?: "", label ?: "Copied text")
        if (message != null) {
            showSnackBar(message)
        }
    }

    protected fun clearClipboard() {
        KeyboardUtils.clearDataFromClipboard(requireContext())
    }

    protected val dataFromClipboard: String
        get() {
            val data = KeyboardUtils.getTextFromClipboard(requireContext())
            clearClipboard()
            return data
        }

    protected fun showKeyboardForced(editText: AppCompatEditText?) {
        KeyboardUtils.showKeyboard(editText, true)
    }

    protected fun showKeyboard(editText: AppCompatEditText?) {
        KeyboardUtils.showKeyboard(editText)
    }

    protected fun hideKeyboard(editText: AppCompatEditText?) {
        KeyboardUtils.hideKeyboard(editText)
    }

    protected fun hideKeyboard() {
        KeyboardUtils.hideKeyboard(requireActivity())
    }

    protected fun hideKeyboard(context: Context, token: IBinder?) {
        KeyboardUtils.hideKeyboard(context, token)
    }

    /*
    * Check permissions
    * */

    protected fun checkWritePermission(): Boolean {
        return PermissionUtils.requestWritePermission(
            this,
            PERMISSION_WRITE_STORAGE
        )
    }

    protected fun checkReadPermission(): Boolean {
        return PermissionUtils.requestReadPermission(
            this,
            PERMISSION_READ_STORAGE
        )
    }

    protected fun checkCameraPermission(): Boolean {
        return PermissionUtils.requestPermission(
            this, PERMISSION_CAMERA, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    /*
    * Show activity
    * */
    @SuppressLint("MissingPermission")
    protected fun showCameraActivity(name: String) {
        cameraUri = ActivitiesUtils.showCamera(this, BaseActivity.REQUEST_ACTIVITY_CAMERA, name)
    }

    @SuppressLint("MissingPermission")
    protected fun showMultipleFilePickerActivity(callback: (uris: List<Uri>?) -> Unit) {
        DocumentsPicker(requireActivity().activityResultRegistry, callback = callback)
            .show()
    }

    protected fun showFileShareActivity(uri: Uri) {
        ActivitiesUtils.showFileShare(requireContext(), null, uri)
    }

    protected fun showUrlInBrowser(url: String) {
        ActivitiesUtils.showBrowser(requireActivity(), null, url)
    }


    protected fun showDownloadFolderActivity(uri: Uri) {
        ActivitiesUtils.showDownloadViewer(this, BaseActivity.REQUEST_ACTIVITY_DOWNLOAD_VIEWER, uri)
    }

    protected fun killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    protected fun setStatusBarColor(@ColorRes color: Int) {
        UiUtils.setStatusBarColor(requireActivity(), color)
    }

    protected fun minimizeApp() {
        ActivitiesUtils.minimizeApp(requireActivity())
    }

}
