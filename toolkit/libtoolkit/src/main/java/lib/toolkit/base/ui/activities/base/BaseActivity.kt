package lib.toolkit.base.ui.activities.base

import android.Manifest
import android.animation.AnimatorInflater
import android.graphics.Rect
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.ActivitiesUtils
import lib.toolkit.base.managers.utils.FragmentUtils
import lib.toolkit.base.managers.utils.KeyboardUtils
import lib.toolkit.base.managers.utils.PermissionUtils
import lib.toolkit.base.managers.utils.UiUtils
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.holders.CustomHolder
import lib.toolkit.base.ui.dialogs.common.holders.EditLineHolder
import lib.toolkit.base.ui.dialogs.common.holders.EditMultilineHolder
import lib.toolkit.base.ui.dialogs.common.holders.InfoHolder
import lib.toolkit.base.ui.dialogs.common.holders.ProgressHolder
import lib.toolkit.base.ui.dialogs.common.holders.QuestionHolder
import lib.toolkit.base.ui.dialogs.common.holders.WaitingHolder
import moxy.MvpAppCompatActivity


abstract class BaseActivity : MvpAppCompatActivity(), FragmentManager.OnBackStackChangedListener, CommonDialog.OnClickListener {

    companion object {
        protected val TAG: String = BaseActivity::class.java.simpleName

        const val REQUEST_ACTIVITY_ONBOARDING = 101
        const val REQUEST_ACTIVITY_ACCOUNTS = 102
        const val REQUEST_ACTIVITY_MEDIA = 103
        const val REQUEST_ACTIVITY_SHARE = 105
        const val REQUEST_ACTIVITY_STORAGE = 106
        const val REQUEST_ACTIVITY_FILE_PICKER = 108
        const val REQUEST_ACTIVITY_IMAGE_PICKER = 109
        const val REQUEST_ACTIVITY_CAMERA = 110
        const val REQUEST_ACTIVITY_IMAGE_VIEWER = 111
        const val REQUEST_ACTIVITY_DOWNLOAD_VIEWER = 112
        const val REQUEST_SELECT_FOLDER = 113
        const val REQUEST_ACTIVITY_PORTAL = 114
        const val REQUEST_ACTIVITY_SAVE = 115
        const val REQUEST_ACTIVITY_EXPORT = 116
        const val REQUEST_ACTIVITY_UNLOCK = 117

        const val PERMISSION_WRITE_STORAGE = 101
        const val PERMISSION_READ_STORAGE = 102
        const val PERMISSION_READ_WRITE_STORAGE = 103

        const val EXTRA_IS_REFRESH = "EXTRA_IS_REFRESH"
    }

    val isTablet: Boolean
        get() = UiUtils.isTablet(this)

    val isLandscape: Boolean
        get() = UiUtils.isLandscape(this)

    val isPortrait: Boolean
        get() = UiUtils.isPortrait(this)

    val isActivityFront: Boolean
        get() = ActivitiesUtils.isActivityFront(this)

    val isFragmentManagerEmpty: Boolean
        get() = supportFragmentManager.fragments.isEmpty()

    private var onDispatchTouchEvent: MutableMap<Int, OnDispatchTouchEvent>? = null
    private var toast: Toast? = null
    protected var snackBar: Snackbar? = null
    protected var commonDialog: CommonDialog? = null

    private val dialogListeners: HashSet<CommonDialog.OnClickListener> = hashSetOf()

    interface OnBackPressFragment {
        fun onBackPressed(): Boolean
    }

    interface OnDispatchTouchEvent {
        fun dispatchTouchEvent(ev: MotionEvent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.removeOnBackStackChangedListener(this)
        commonDialog = null
        toast = null
        snackBar = null
    }

    override fun onBackStackChanged() {

    }

    override fun onBackPressed() {
        if (!isFragmentBackPress()) {
            super.onBackPressed()
        }
    }

    open fun setCommonDialogOpen() {

    }

    /*
    * Check all child fragments
    * */
    protected fun isFragmentBackPress(): Boolean {
        supportFragmentManager.fragments.asReversed().forEach { parentFragment ->
            if (parentFragment.isAdded && isFragmentBackPress(parentFragment)) {
                return true
            }
        }

        return false
    }

    private fun isFragmentBackPress(fragment: Fragment): Boolean {
        fragment.childFragmentManager.fragments.asReversed().forEach { childFragment ->
            if (childFragment.isAdded && childFragment.isVisible && isFragmentBackPress(childFragment)) {
                return true
            }
        }

        //TODO need check
        return if (fragment.isVisible && fragment.isAdded) {
            fragment is OnBackPressFragment  && fragment.onBackPressed() &&
                    fragment.lifecycle.currentState == Lifecycle.State.RESUMED
        } else {
            true
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        onTouchAction(event)

        onDispatchTouchEvent?.let {
            for ((_, value) in it) {
                value.dispatchTouchEvent(event)
            }
        }

        if (snackBar != null) {
            UiUtils.hideSnackBarOnOutsideTouch(snackBar, event)
            snackBar = null
        }

        return super.dispatchTouchEvent(event)
    }

    protected open fun onTouchAction(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            (currentFocus as? EditText)?.let { view ->
                Rect().let { rect ->
                    view.getGlobalVisibleRect(rect)

                    if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        onEditTouchOutside(view, event)
                    }
                }
            }
        }
    }

    protected open fun onEditTouchOutside(view: EditText, event: MotionEvent) {
        view.clearFocus()
        hideKeyboard(view)
    }

    private fun init() {
        initViews()
        setDialog()
    }

    private fun initViews() {
        supportFragmentManager.addOnBackStackChangedListener(this)
        toast = UiUtils.getToast(this)
    }

    fun addOnDispatchTouchEvent(onDispatchTouchEvent: OnDispatchTouchEvent) {
        if (this.onDispatchTouchEvent == null) {
            this.onDispatchTouchEvent = HashMap()
        }
        this.onDispatchTouchEvent!![onDispatchTouchEvent.hashCode()] = onDispatchTouchEvent
    }

    fun removeOnDispatchTouchEvent(onDispatchTouchEvent: OnDispatchTouchEvent) {
        if (!this.onDispatchTouchEvent.isNullOrEmpty()) {
            this.onDispatchTouchEvent!!.remove(onDispatchTouchEvent.hashCode())
        }
    }

    private fun setDialog() {
        commonDialog = supportFragmentManager.findFragmentByTag(CommonDialog.TAG) as CommonDialog?
        if (commonDialog == null) {
            commonDialog = CommonDialog.newInstance()
        }
        commonDialog?.setFragmentManager(supportFragmentManager)
    }

    fun addDialogListener(onDialogClickListener: CommonDialog.OnClickListener?) {
        onDialogClickListener?.let { dialogListeners.add(it) }
    }

    fun removeDialogListener(onDialogClickListener: CommonDialog.OnClickListener?) {
        onDialogClickListener?.let { dialogListeners.remove(it) }
    }

    fun hideDialog(forceHide: Boolean = false) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (commonDialog?.isAdded == true || forceHide) {
                    commonDialog?.dismiss()
                }
            }
        }
    }

    override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        Log.d(TAG, "onAcceptClick() - $dialogs - value: $value - tag: $tag")
        dialogListeners.forEach {
            it.onAcceptClick(dialogs, value, tag)
        }
    }

    override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        Log.d(TAG, "onCancelClick() - $dialogs - tag: $tag")
        dialogListeners.forEach {
            it.onCancelClick(dialogs, tag)
        }
    }

    override fun onCloseCommonDialog() {
        dialogListeners.forEach {
            it.onCloseCommonDialog()
        }
    }

    /*
    * Fragment operations
    * */
    protected fun showRootFragment() {
        FragmentUtils.backToRoot(supportFragmentManager)
    }

    /*
    * Snackbar/Toast
    * */
    protected fun showSnackBar(@StringRes resource: Int): Snackbar {
        return showSnackBar(resources.getString(resource))
    }

    @JvmOverloads
    protected fun showSnackBar(string: String, button: String? = null, action: View.OnClickListener? = null): Snackbar {
        return UiUtils.getSnackBar(this).apply {
            setText(string)
            setAction(button, action)
            show()
            snackBar = this
        }
    }

    protected fun showToast(@StringRes resource: Int) {
        showToast(resources.getString(resource))
    }

    protected fun showToast(string: String) {
        toast?.setText(string)
        toast?.show()
    }

    /*
    * Dialogs
    * */

    /*
    * Dialog builders
    * */
    fun getEditDialog(
        title: String? = null,
        bottomTitle: String? = null,
        value: String? = null,
        editHint: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null,
        suffix: String? = null
    ): EditLineHolder.Builder? {
        return commonDialog?.editLine()?.apply {
            setTopTitle(title)
            setBottomTitle(bottomTitle)
            setEditValue(value)
            setEditHintValue(editHint)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
            setSuffix(suffix)
        }
    }

    fun getWaitingDialog(
        topTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): WaitingHolder.Builder? {
        return commonDialog?.waiting()?.apply {
            setTopTitle(topTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
        }
    }

    fun getQuestionDialog(
        title: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        question: String? = null,
        tag: String? = null,
        acceptErrorTint: Boolean = false
    ): QuestionHolder.Builder? {
        return commonDialog?.question()?.apply {
            setTopTitle(title)
            setQuestion(question)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
            setAcceptErrorTint(acceptErrorTint)
        }
    }

    fun getInfoDialog(
        title: String? = null,
        info: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): InfoHolder.Builder? {
        return commonDialog?.info()?.apply {
            setTopTitle(title)
            setBottomTitle(info)
            setCancelTitle(cancelTitle)
            setTag(tag)
        }
    }

    // TODO be careful to compose view
    fun getCustomDialog(
        view: View,
        acceptListener: () -> Unit,
        title: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
    ): CustomHolder.Builder? {
        return commonDialog?.custom()?.apply {
            setTopTitle(title)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setAcceptClickListener(acceptListener)
            setView(view)
        }
    }

    fun getEditMultilineDialog(
        title: String? = null,
        hint: String? = null,
        acceptTitle: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): EditMultilineHolder.Builder? {
        return commonDialog?.editMultiline()?.apply {
            setTopTitle(title)
            setEditHintValue(hint)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
        }
    }

    fun getProgressDialog(
        title: String? = null,
        cancelTitle: String? = null,
        tag: String? = null
    ): ProgressHolder.Builder? {
        return commonDialog?.progress()?.apply {
            setTopTitle(title)
            setCancelTitle(cancelTitle)
            setTag(tag)
        }
    }

    /*
    * Dialog show
    * */

    /*
    * Waiting
    * */
    fun showWaitingDialog(
        topTitle: String?,
        cancelTitle: String?,
        type: WaitingHolder.ProgressType,
        tag: String? = null,
        progressColor: Int = R.color.colorPrimary,
        textColor: Int = 0,
        textGravity: Int = 0
    ) {
        getWaitingDialog(topTitle, cancelTitle, tag)?.run {
            setProgressType(type)
            setTextColor(textColor)
            setTopTitleGravity(textGravity)
            show()
        }
    }

    fun showWaitingDialogCircle(
        topTitle: String,
        cancelTitle: String?,
        tag: String?,
        progressColor: Int = 0,
        textColor: Int = 0,
        textGravity: Int = Gravity.START
    ) {
        showWaitingDialog(
            topTitle,
            cancelTitle,
            WaitingHolder.ProgressType.CIRCLE,
            tag,
            progressColor,
            textColor,
            textGravity
        )
    }


    /*
    * Edit line
    * */
    fun showEditDialog(
        title: String,
        bottomTitle: String?,
        value: String?,
        editHint: String?,
        acceptTitle: String?,
        cancelTitle: String?,
        isPassword: Boolean = false,
        error: String?,
        tag: String?,
        suffix: String? = null
    ) {
        getEditDialog(title, bottomTitle, value, editHint, acceptTitle, cancelTitle, tag)?.run {
            setIsPassword(isPassword)
            setError(error)
            setSuffix(suffix)
            show()
        }
    }

    /*
    * Question
    * */
    fun showQuestionDialog(
        title: String,
        tag: String?,
        acceptTitle: String?,
        cancelTitle: String?,
        question: String?,
        acceptErrorTint: Boolean = false
    ) {
        getQuestionDialog(title, acceptTitle, cancelTitle, question, tag, acceptErrorTint)?.show()
    }

    fun showEditMultilineDialog(title: String, hint: String, acceptTitle: String?, cancelTitle: String?, tag: String?) {
        getEditMultilineDialog(title, hint, acceptTitle, cancelTitle, tag)?.run {
            show()
        }
    }

    fun showProgressDialog(title: String?, hideButton: Boolean, cancelTitle: String?, tag: String?) {
        getProgressDialog(title, cancelTitle, tag)?.run {
            if (!hideButton) {
                setCancelTitle(cancelTitle)
            }
            setProgressColor(R.color.colorPrimary)
            show()
        }
    }

    fun updateProgressDialog(total: Int, progress: Int) {
        commonDialog?.progress()?.update(total, progress)
    }


    /*
    * AppBarLayout changes
    * */
    protected fun setAppBarScroll(toolbar: Toolbar) {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }

    protected fun setAppBarFix(toolbar: Toolbar) {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    }

    protected fun setAppBarLayoutElevation(appBarLayout: AppBarLayout) {
        appBarLayout.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.appbar_layout_elevation)
    }

    protected fun removeAppBarLayoutElevation(appBarLayout: AppBarLayout) {
        appBarLayout.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.appbar_layout_no_elevation)
    }

    protected fun expandAppBar(appBarLayout: AppBarLayout, isAnimate: Boolean) {
        appBarLayout.setExpanded(true, isAnimate)
    }

    protected fun collapseAppBar(appBarLayout: AppBarLayout, isAnimate: Boolean) {
        appBarLayout.setExpanded(false, isAnimate)
    }

    protected fun setCollapsingToolbarFix(collapsingToolbar: CollapsingToolbarLayout) {
        val params = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        collapsingToolbar.layoutParams = params
    }

    private fun setCollapsingToolbarScroll(collapsingToolbar: CollapsingToolbarLayout) {
        val params = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        collapsingToolbar.layoutParams = params
    }

    protected fun setStatusBarColor(@ColorRes color: Int) {
        UiUtils.setStatusBarColor(this, color)
    }

    /*
    * Helper methods
    * */
    protected fun setSoftInputResize() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    protected fun killSelf() {
        ActivitiesUtils.killSelf()
    }

    protected fun showPlayMarket(packageId: String) {
        ActivitiesUtils.showPlayMarket(applicationContext, packageId)
    }

    protected fun showApp(packageId: String) {
        ActivitiesUtils.showApp(applicationContext, packageId)
    }

    protected fun minimizeApp() {
        ActivitiesUtils.minimizeApp(this)
    }

    protected fun showBrowser(url: String) {
        ActivitiesUtils.showBrowser(this, null, url)
    }

    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    protected fun showSingleFilePicker(@StringRes title: Int, permissionReadWriteCode: Int, activityRequestCode: Int) {
        if (PermissionUtils.requestReadWritePermission(this, permissionReadWriteCode)) {
            ActivitiesUtils.showSingleFilePicker(this, title, activityRequestCode)
        }
    }

    /*
    * Keyboard
    * */

    protected open fun showKeyboard(view: View) {
        KeyboardUtils.showKeyboard(view, true)
    }

    protected open fun showTextAutoCompleteKeyboard(view: EditText) {
        KeyboardUtils.showTextAutoCompleteKeyboard(view)
    }

    protected open fun showTextAutoCorrectKeyboard(view: EditText) {
        KeyboardUtils.showTextAutoCorrectKeyboard(view)
    }

    protected open fun showNumericKeyboard(view: EditText) {
        KeyboardUtils.showNumericKeyboard(view)
    }

    protected open fun hideKeyboard() {
        KeyboardUtils.hideKeyboard(this)
    }

    protected open fun hideKeyboard(view: View) {
        KeyboardUtils.hideKeyboard(view)
    }

    protected open fun hideKeyboard(token: IBinder? = null) {
        KeyboardUtils.hideKeyboard(this, token ?: window.decorView.windowToken)
    }

}
