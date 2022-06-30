package app.editors.manager.ui.dialogs.fragments

import android.os.Bundle
import android.view.View
import app.editors.manager.R
import app.editors.manager.ui.fragments.filter.FilterAuthorFragment
import app.editors.manager.ui.fragments.filter.FilterFragment

class FilterDialogFragment : BaseDialogFragment() {

    companion object {
        val TAG: String = FilterDialogFragment::class.java.simpleName
        const val REQUEST_KEY_REFRESH = "request_key_refresh"
        const val BUNDLE_KEY_REFRESH = "bundle_key_refresh"
        private const val KEY_FOLDER_ID = "key_folder_id"

        fun newInstance(folderId: String?): FilterDialogFragment {
            return FilterDialogFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_FOLDER_ID, folderId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(R.menu.docs_filter)
        showFragment(FilterFragment.newInstance(arguments?.getString(KEY_FOLDER_ID)))
    }

    override fun onBackPressed(): Boolean {
        return if ((childFragmentManager.findFragmentByTag(FilterAuthorFragment.TAG)
                    as? FilterAuthorFragment)?.onBackPressed() == true) true
        else super.onBackPressed()
    }
}