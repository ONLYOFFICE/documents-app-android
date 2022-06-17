package lib.toolkit.base.ui.activities.base

import android.Manifest
import android.animation.AnimatorInflater
import android.graphics.Rect
import android.os.Build
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
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import lib.toolkit.base.R
import lib.toolkit.base.managers.utils.*
import lib.toolkit.base.ui.dialogs.common.CommonDialog
import lib.toolkit.base.ui.dialogs.common.holders.*
import moxy.MvpAppCompatActivity


abstract class BaseActivity : MvpAppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        protected val TAG: String = BaseActivity::class.java.simpleName

        const val REQUEST_ACTIVITY_ONBOARDING = 101
        const val REQUEST_ACTIVITY_ACCOUNTS = 102
        const val REQUEST_ACTIVITY_MEDIA = 103
        const val REQUEST_ACTIVITY_WEB_VIEWER = 104
        const val REQUEST_ACTIVITY_SHARE = 105
        const val REQUEST_ACTIVITY_STORAGE = 106
        const val REQUEST_ACTIVITY_OPERATION = 107
        const val REQUEST_ACTIVITY_FILE_PICKER = 108
        const val REQUEST_ACTIVITY_IMAGE_PICKER = 109
        const val REQUEST_ACTIVITY_CAMERA = 110
        const val REQUEST_ACTIVITY_IMAGE_VIEWER = 111
        const val REQUEST_ACTIVITY_DOWNLOAD_VIEWER = 112
        const val REQUEST_SELECT_FOLDER = 113
        const val REQUEST_ACTIVITY_PORTAL = 114
        const val REQUEST_ACTIVITY_SAVE = 115
        const val REQUEST_ACTIVITY_EXPORT = 116

        const val PERMISSION_WRITE_STORAGE = 101
        const val PERMISSION_READ_STORAGE = 102
        const val PERMISSION_READ_WRITE_STORAGE = 103
    }

    inner class OnCommonDialogClick : CommonDialog.OnClickListener {
        override fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
            this@BaseActivity.onAcceptClick(dialogs, value, tag)
        }

        override fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
            this@BaseActivity.onCancelClick(dialogs, tag)
        }

        override fun onCloseCommonDialog() {
            this@BaseActivity.onCloseCommonDialog()
        }
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

    protected var mOnDispatchTouchEvent: MutableMap<Int, OnDispatchTouchEvent>? = null
    protected var mToast: Toast? = null
    protected var mSnackBar: Snackbar? = null
    protected var commonDialog: CommonDialog? = null
    protected var mOnDialogClickListener: CommonDialog.OnClickListener? = OnCommonDialogClick()

    val dialogListeners: HashSet<CommonDialog.OnClickListener> = hashSetOf()

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
        mToast = null
        mSnackBar = null
        mOnDialogClickListener = null
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
            if (isFragmentBackPress(parentFragment)) {
                return true
            }
        }

        return false
    }

    protected fun isFragmentBackPress(fragment: Fragment): Boolean {
        fragment.childFragmentManager.fragments.asReversed().forEach { childFragment ->
            if (isFragmentBackPress(childFragment)) {
                return true
            }
        }

        return fragment is OnBackPressFragment && fragment.onBackPressed() &&
                fragment.lifecycle.currentState == Lifecycle.State.RESUMED
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        onTouchAction(event)

        mOnDispatchTouchEvent?.let {
            for ((_, value) in it) {
                value.dispatchTouchEvent(event)
            }
        }

        if (mSnackBar != null) {
            UiUtils.hideSnackBarOnOutsideTouch(mSnackBar, event)
            mSnackBar = null
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
        mToast = UiUtils.getToast(this)
    }

    fun addOnDispatchTouchEvent(onDispatchTouchEvent: OnDispatchTouchEvent) {
        if (mOnDispatchTouchEvent == null) {
            mOnDispatchTouchEvent = HashMap()
        }
        mOnDispatchTouchEvent!![onDispatchTouchEvent.hashCode()] = onDispatchTouchEvent
    }

    fun removeOnDispatchTouchEvent(onDispatchTouchEvent: OnDispatchTouchEvent) {
        if (!mOnDispatchTouchEvent.isNullOrEmpty()) {
            mOnDispatchTouchEvent!!.remove(onDispatchTouchEvent.hashCode())
        }
    }

    private fun setDialog() {
        commonDialog = supportFragmentManager.findFragmentByTag(CommonDialog.TAG) as CommonDialog?
        if (commonDialog == null) {
            commonDialog = CommonDialog.newInstance()
        }

        commonDialog!!.setFragmentManager(supportFragmentManager)
        commonDialog!!.setOnClickListener(mOnDialogClickListener!!)
    }

    fun addDialogListener(onDialogClickListener: CommonDialog.OnClickListener?) {
        onDialogClickListener?.let { dialogListeners.add(it) }
    }

    fun removeDialogListener(onDialogClickListener: CommonDialog.OnClickListener?) {
        onDialogClickListener?.let { dialogListeners.remove(it) }
    }

    fun hideDialog(forceHide: Boolean = false) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && !forceHide) {
            commonDialog?.view?.post {
                if (commonDialog?.isAdded == true) {
                    commonDialog?.dismiss()
                }
            }
        } else {
            commonDialog?.dismiss()
        }
    }

    open fun onAcceptClick(dialogs: CommonDialog.Dialogs?, value: String?, tag: String?) {
        Log.d(TAG, "onAcceptClick() - $dialogs - value: $value - tag: $tag")
//        mOnFragmentDialogClickListener?.onAcceptClick(dialogs, value, tag)
        dialogListeners.forEach {
            it.onAcceptClick(dialogs, value, tag)
        }
    }

    open fun onCancelClick(dialogs: CommonDialog.Dialogs?, tag: String?) {
        Log.d(TAG, "onCancelClick() - $dialogs - tag: $tag")
//        mOnFragmentDialogClickListener?.onCancelClick(dialogs, tag)
        dialogListeners.forEach {
            it.onCancelClick(dialogs, tag)
        }
    }

    open fun onCloseCommonDialog() {
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
            mSnackBar = this
        }
    }

    protected fun showToast(@StringRes resource: Int) {
        showToast(resources.getString(resource))
    }

    protected fun showToast(string: String) {
        mToast?.setText(string)
        mToast?.show()
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
        tag: String? = null
    ): EditLineHolder.Builder? {
        return commonDialog?.editLine()?.apply {
            setTopTitle(title)
            setBottomTitle(bottomTitle)
            setEditValue(value)
            setEditHintValue(editHint)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
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
        tag: String? = null
    ): QuestionHolder.Builder? {
        return commonDialog?.question()?.apply {
            setTopTitle(title)
            setQuestion(question)
            setAcceptTitle(acceptTitle)
            setCancelTitle(cancelTitle)
            setTag(tag)
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
        progressColor: Int = R.color.colorPrimaryLight,
        textColor: Int = 0,
        textGravity: Int = 0
    ) {
        getWaitingDialog(topTitle, cancelTitle, tag)?.run {
            setProgressType(type)
            setTextColor(textColor)
            setProgressColor(progressColor)
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
        endHint: String?,
        acceptTitle: String?,
        cancelTitle: String?,
        isPassword: Boolean = false,
        error: String?,
        tag: String?,
        tintColor: Int? = null
    ) {
        getEditDialog(title, bottomTitle, value, editHint, acceptTitle, cancelTitle, tag)?.run {
            setEditHintValue(endHint)
            setIsPassword(isPassword)
            setError(error)
            setColorTint(tintColor)
            show()
        }
    }

    /*
    * Question
    * */
    fun showQuestionDialog(title: String, tag: String?, acceptTitle: String?, cancelTitle: String?, question: String?) {
        getQuestionDialog(title, acceptTitle, cancelTitle, question, tag)?.run {
            show()
        }
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
            setProgressColor(R.color.colorPrimaryLight)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.appbar_layout_elevation)
            appBarLayout.stateListAnimator = stateListAnimator
        } else {
            appBarLayout.elevation = resources.getDimension(R.dimen.default_elevation_height)
        }
    }

    protected fun removeAppBarLayoutElevation(appBarLayout: AppBarLayout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.appbar_layout_no_elevation)
            appBarLayout.stateListAnimator = stateListAnimator
        } else {
            appBarLayout.elevation = 0.0f
        }
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
    protected fun copyToClipboard(value: String) {
        copyToClipboard(null, value, null)
    }

    protected fun copyToClipboard(label: String?, value: String, message: String?) {
        KeyboardUtils.setDataToClipboard(applicationContext, value, label ?: "Copied text")
        if (message != null) {
            showSnackBar(message)
        }
    }

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
