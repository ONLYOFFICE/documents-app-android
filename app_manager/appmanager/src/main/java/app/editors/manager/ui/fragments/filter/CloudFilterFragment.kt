package app.editors.manager.ui.fragments.filter

import android.os.Bundle
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterType
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.presenters.filter.CloudFilterPresenter
import app.editors.manager.ui.views.custom.ChipItem
import app.editors.manager.ui.views.custom.SingleChoiceChipGroupView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CloudFilterFragment : BaseFilterFragment() {

    companion object {
        val TAG = CloudFilterFragment::class.simpleName

        fun newInstance(folderId: String?): CloudFilterFragment {
            return CloudFilterFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_FOLDER_ID, folderId)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: CloudFilterPresenter

    @ProvidePresenter
    fun providePresenter(): CloudFilterPresenter {
        return CloudFilterPresenter(arguments?.getString(KEY_FOLDER_ID))
    }

    override val filterPresenter: BaseFilterPresenter
        get() = presenter

    private var typeChipGroup: SingleChoiceChipGroupView? = null
    private var authorChipGroup: SingleChoiceChipGroupView? = null
    private var excludeChipGroup: SingleChoiceChipGroupView? = null

    private val usersChipItem = object : ChipItem {
        override val chipTitle: Int = R.string.share_add_common_header_users
        override val withOption: Boolean = true
        override var option: Any? = null
    }

    private val groupsChipItem = object : ChipItem {
        override val chipTitle: Int = R.string.share_add_common_header_groups
        override val withOption: Boolean = true
        override var option: Any? = null
    }

    override fun initViews() {
        initChipGroups()
        if (presenter.resultCount >= 0) onFilterResult(presenter.resultCount)
        presenter.update(initialCall = true)
    }

    override fun updateViewState(isChanged: Boolean) {
        super.updateViewState(isChanged)
        App.getApp().accountOnline?.let(::updateAuthorChipGroup)
    }

    private fun updateAuthorChipGroup(account: CloudAccount) {
        val author = presenter.filterAuthor
        groupsChipItem.option = null
        usersChipItem.option = null
        if (author.id.isNotEmpty()) {
            if (!author.isGroup) {
                usersChipItem.option =
                    getString(R.string.item_owner_self).takeIf { account.id == author.id } ?: author.name
            } else {
                groupsChipItem.option = author.name
            }
        }
        authorChipGroup?.update(usersChipItem, groupsChipItem)
    }

    private fun initChipGroups() {
        typeChipGroup = SingleChoiceChipGroupView(requireContext()).apply {
            setTitle(R.string.filter_title_type)
            setChips(FilterType.allTypes, presenter.filterType) { type, checked ->
                presenter.filterType = if (checked) type else FilterType.None
                presenter.update()
            }
        }

        authorChipGroup = App.getApp().accountOnline?.let { account ->
            return@let if (!account.isPersonal()) {
                updateAuthorChipGroup(account)
                SingleChoiceChipGroupView(requireContext()).apply {
                    setTitle(R.string.filter_title_author)
                    setChips(
                        chipItems = listOf(usersChipItem, groupsChipItem),
                        checkedChip = null,
                        closeListener = presenter::clearAuthor
                    ) { item, _ ->
                        showAuthorFragment(
                            fragmentManager = parentFragmentManager,
                            isGroups = item == groupsChipItem,
                            selectedId = presenter.filterAuthor.id
                        ) { bundle ->
                            presenter.filterAuthor =
                                FilterAuthor.toObject(bundle.getString(FilterAuthorFragment.BUNDLE_KEY_AUTHOR))
                            presenter.update()
                        }
                    }
                }
            } else null
        }

         excludeChipGroup = SingleChoiceChipGroupView(requireContext()).apply {
             val excludeSubfolderChipItem = object : ChipItem {
                 override val chipTitle: Int = R.string.filter_exclude_subfolders
                 override val withOption: Boolean = false
                 override var option: Any? = null
             }

            setTitle(R.string.filter_exclude_subfolders)
            setChip(excludeSubfolderChipItem, presenter.excludeSubfolder) { _, checked ->
                presenter.excludeSubfolder = checked
            }
        }

        addChipGroups(typeChipGroup, authorChipGroup, excludeChipGroup)
    }
}