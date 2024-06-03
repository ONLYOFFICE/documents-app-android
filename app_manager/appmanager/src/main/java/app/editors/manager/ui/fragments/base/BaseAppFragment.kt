package app.editors.manager.ui.fragments.base

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import app.documents.core.network.manager.models.explorer.Explorer
import app.editors.manager.R
import app.editors.manager.ui.activities.base.BaseAppActivity
import app.editors.manager.ui.activities.main.MediaActivity
import app.editors.manager.ui.activities.main.StorageActivity.Companion.show
import app.editors.manager.ui.dialogs.fragments.BaseDialogFragment
import app.editors.manager.ui.dialogs.fragments.IBaseDialogFragment
import app.editors.manager.ui.interfaces.ContextDialogInterface
import lib.toolkit.base.managers.utils.FragmentUtils.showFragment
import lib.toolkit.base.managers.utils.LaunchActivityForResult
import lib.toolkit.base.ui.fragments.base.BaseFragment

abstract class BaseAppFragment : BaseFragment() {

    var contextDialogListener: ContextDialogInterface? = null
    protected var menu: Menu? = null
    protected var menuInflater: MenuInflater? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            contextDialogListener = context as ContextDialogInterface
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
        showFragment(parentFragmentManager, fragment, R.id.frame_container, tag, isAdd)
    }

    protected fun showParentFragment(fragment: Fragment, tag: String?, isAdd: Boolean) {
        parentFragment?.parentFragmentManager?.let {
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
    protected fun showMediaActivity(explorer: Explorer, isWebDAv: Boolean, callback: (result: ActivityResult) -> Unit) {
        LaunchActivityForResult(
            activityResultRegistry = requireActivity().activityResultRegistry,
            callback = callback,
            intent = MediaActivity.getIntent(requireContext(), explorer, isWebDAv)
        ).show()
    }

    protected fun showStorageActivity(
        isMySection: Boolean,
        isRoom: Boolean = false,
        title: String? = null,
        providerKey: String? = null,
        providerId: Int = -1
    ) {
        show(
            fragment = this,
            isMySection = isMySection,
            isRoomStorage = isRoom,
            title = title,
            providerKey = providerKey,
            providerId = providerId
        )
    }

    protected fun getDialogFragment(): IBaseDialogFragment? {
        return requireActivity().supportFragmentManager
            .fragments.findLast { it is BaseDialogFragment } as? IBaseDialogFragment
    }

    companion object {

        val TAG: String = BaseAppFragment::class.java.simpleName

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