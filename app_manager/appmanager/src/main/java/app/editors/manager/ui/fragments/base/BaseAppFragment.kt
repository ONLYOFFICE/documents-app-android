package app.editors.manager.ui.fragments.base

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import app.editors.manager.R
import app.documents.core.network.manager.models.explorer.CloudFile
import app.documents.core.network.manager.models.explorer.Explorer
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.activities.main.MediaActivity.Companion.show
import app.editors.manager.ui.activities.main.OperationActivity.Companion.showCopy
import app.editors.manager.ui.activities.main.OperationActivity.Companion.showMove
import app.editors.manager.ui.activities.main.OperationActivity.Companion.showRestore
import app.editors.manager.ui.activities.main.ShareActivity.Companion.show
import app.editors.manager.ui.activities.main.StorageActivity.Companion.show
import app.editors.manager.ui.activities.main.WebViewerActivity.Companion.show
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.interfaces.ContextDialogInterface
import lib.toolkit.base.managers.utils.FragmentUtils.showFragment
import lib.toolkit.base.ui.fragments.base.BaseFragment

abstract class BaseAppFragment : BaseFragment() {

    var mContextDialogListener: ContextDialogInterface? = null
    protected var menu: Menu? = null
    protected var menuInflater: MenuInflater? = null
    private val TAG = javaClass.simpleName

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mContextDialogListener = context as ContextDialogInterface
            addOnDispatchTouchEvent()
        } catch (e: ClassCastException) {
            throw RuntimeException(
                BaseAppFragment::class.java.simpleName + " - must implement - " +
                        BaseAppActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        menuInflater = inflater
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    /**
     * Fragment operations
     * */
    protected fun showFragment(fragment: Fragment, tag: String?, isAdd: Boolean) {
        fragmentManager?.let {
            showFragment(it, fragment, R.id.frame_container, tag, isAdd)
        }
    }

    protected fun showParentFragment(fragment: Fragment, tag: String?, isAdd: Boolean) {
        parentFragment?.fragmentManager?.let {
            showFragment(it, fragment, R.id.frame_container, tag, isAdd)
        }
    }

    /**
     * Keyboard
     * */
    protected fun copySharedLinkToClipboard(value: String?, message: String?) {
        copyToClipboard(getString(R.string.share_clipboard_external_link_label), value, message)
    }

    /**
     * Show activity
     * */
    protected fun showOperationMoveActivity(explorer: Explorer) {
        showMove(this, explorer)
    }

    protected fun showOperationCopyActivity(explorer: Explorer) {
        showCopy(this, explorer)
    }

    protected fun showOperationRestoreActivity(explorer: Explorer) {
        showRestore(this, explorer)
    }

    protected fun showViewerActivity(file: CloudFile?) {
        show(requireActivity(), file)
    }

    protected fun showMediaActivity(explorer: Explorer?, isWebDAv: Boolean) {
        show(this, explorer, isWebDAv)
    }

    protected fun showShareActivity(item: Item?) {
        show(this, item)
    }

    protected fun showStorageActivity(isMySection: Boolean) {
        show(this, isMySection)
    }

    protected fun getDialogFragment(): IBaseDialogFragment? {
        return requireActivity().supportFragmentManager
            .fragments.findLast { it is BaseDialogFragment } as? IBaseDialogFragment
    }

    companion object {
        @JvmStatic
        protected val PERMISSION_SMS = 0

        @JvmStatic
        protected val PERMISSION_WRITE_STORAGE = 1

        @JvmStatic
        protected val PERMISSION_READ_STORAGE = 2

        @JvmStatic
        protected val PERMISSION_CAMERA = 3

        @JvmStatic
        protected val PERMISSION_READ_UPLOAD = 4
    }
}