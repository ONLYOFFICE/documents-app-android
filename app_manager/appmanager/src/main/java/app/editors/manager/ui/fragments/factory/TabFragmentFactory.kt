package app.editors.manager.ui.fragments.factory

import android.content.Context
import app.editors.manager.R
import app.editors.manager.ui.fragments.main.*
import lib.toolkit.base.managers.utils.TabFragmentDictionary

class TabFragmentFactory(val context: Context) {
    companion object {
        fun getSectionFragment(section: String, stringAccount: String): DocsCloudFragment =
            when {
                TabFragmentDictionary.My.contains(section) -> DocsMyFragment.newInstance(stringAccount)
                TabFragmentDictionary.Shared.contains(section) -> DocsShareFragment.newInstance(stringAccount)
                TabFragmentDictionary.Favorites.contains(section) -> DocsFavoritesFragment.newInstance(stringAccount)
                TabFragmentDictionary.Common.contains(section) -> DocsCommonFragment.newInstance(stringAccount)
                TabFragmentDictionary.Trash.contains(section) -> DocsTrashFragment.newInstance(stringAccount)
                else -> DocsMyFragment.newInstance(stringAccount)
            }
    }
    fun getTabTitle(tab: String): String =
        when {
            TabFragmentDictionary.Common.contains(tab) -> context.getString(R.string.main_pager_docs_common)
            TabFragmentDictionary.My.contains(tab) -> context.getString(R.string.main_pager_docs_my)
            TabFragmentDictionary.Favorites.contains(tab) -> context.getString(R.string.main_pager_docs_favorites)
            TabFragmentDictionary.Shared.contains(tab) -> context.getString(R.string.main_pager_docs_share)
            TabFragmentDictionary.Trash.contains(tab) -> context.getString(R.string.main_pager_docs_trash)
            else -> ""
        }
}