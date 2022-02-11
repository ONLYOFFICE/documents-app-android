package app.editors.manager.ui.fragments.factory

import android.content.Context
import app.editors.manager.R
import app.editors.manager.ui.fragments.main.*
import app.documents.core.network.ApiContract

class TabFragmentFactory(val context: Context) {
    companion object {
        fun getSectionFragment(section: Int, stringAccount: String, fileData: String? = null): DocsCloudFragment =
            when(section) {
                ApiContract.SectionType.CLOUD_USER -> DocsMyFragment.newInstance(stringAccount)
                ApiContract.SectionType.CLOUD_SHARE -> DocsShareFragment.newInstance(stringAccount)
                ApiContract.SectionType.CLOUD_FAVORITES -> DocsFavoritesFragment.newInstance(stringAccount)
                ApiContract.SectionType.CLOUD_COMMON -> DocsCommonFragment.newInstance(stringAccount)
                ApiContract.SectionType.CLOUD_TRASH -> DocsTrashFragment.newInstance(stringAccount)
                ApiContract.SectionType.CLOUD_PROJECTS -> DocsProjectsFragment.newInstance(stringAccount, fileData)
                else -> DocsMyFragment.newInstance(stringAccount)
            }
    }
    fun getTabTitle(tab: Int): String =
        when(tab) {
            ApiContract.SectionType.CLOUD_COMMON -> context.getString(R.string.main_pager_docs_common)
            ApiContract.SectionType.CLOUD_USER -> context.getString(R.string.main_pager_docs_my)
            ApiContract.SectionType.CLOUD_FAVORITES -> context.getString(R.string.main_pager_docs_favorites)
            ApiContract.SectionType.CLOUD_SHARE -> context.getString(R.string.main_pager_docs_share)
            ApiContract.SectionType.CLOUD_TRASH -> context.getString(R.string.main_pager_docs_trash)
            ApiContract.SectionType.CLOUD_PROJECTS -> context.getString(R.string.main_pager_docs_projects)
            else -> ""
        }
}