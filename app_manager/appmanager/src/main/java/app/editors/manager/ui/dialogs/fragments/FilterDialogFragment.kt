package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.documents.core.network.common.contracts.ApiContract
import app.editors.manager.R
import app.editors.manager.ui.fragments.filter.CloudFilterFragment
import app.editors.manager.ui.fragments.filter.FilterAuthorFragment
import app.editors.manager.ui.fragments.filter.RoomFilterFragment

class FilterDialogFragment : BaseDialogFragment() {

    companion object {
        val TAG: String = FilterDialogFragment::class.java.simpleName
        const val REQUEST_KEY_REFRESH = "request_key_refresh"
        const val BUNDLE_KEY_REFRESH = "bundle_key_refresh"
        private const val KEY_FOLDER_ID = "key_folder_id"
        private const val KEY_SECTION = "key_section"
        private const val KEY_IS_ROOT = "key_is_root"

        fun newInstance(folderId: String?, section: Int, isRoot: Boolean): FilterDialogFragment {
            return FilterDialogFragment().apply {
                arguments = Bundle(2).apply {
                    putString(KEY_FOLDER_ID, folderId)
                    putInt(KEY_SECTION, section)
                    putBoolean(KEY_IS_ROOT, isRoot)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(R.menu.docs_filter)
        showFragment(getInstance())
    }

    override fun onBackPressed(): Boolean {
        return if ((childFragmentManager.findFragmentByTag(FilterAuthorFragment.TAG)
                    as? FilterAuthorFragment)?.onBackPressed() == true) true
        else super.onBackPressed()
    }

    private fun getInstance(): Fragment {
        val folderId = arguments?.getString(KEY_FOLDER_ID)
        val section = arguments?.getInt(KEY_SECTION) ?: -1
        val isRoot = arguments?.getBoolean(KEY_IS_ROOT) == true
        return when {
            ApiContract.SectionType.isRoom(section) && isRoot -> RoomFilterFragment.newInstance(folderId)
            else -> CloudFilterFragment.newInstance(folderId, section)
        }
    }
}