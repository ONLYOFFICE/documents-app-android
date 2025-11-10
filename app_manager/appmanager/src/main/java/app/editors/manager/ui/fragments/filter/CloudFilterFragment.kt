package app.editors.manager.ui.fragments.filter

import android.os.Bundle
import app.documents.core.model.cloud.CloudAccount
import app.documents.core.model.cloud.isDocSpace
import app.documents.core.network.common.contracts.ApiContract
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
        private const val KEY_SECTION: String = "key_section"

        fun newInstance(folderId: String?, section: Int): CloudFilterFragment {
            return CloudFilterFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_FOLDER_ID, folderId)
                    putInt(KEY_SECTION, section)
                }
            }
        }
    }

    private val section: Int? by lazy { arguments?.getInt(KEY_SECTION) }

    private val isSectionShareWithMe: Boolean by lazy {
        section == ApiContract.SectionType.CLOUD_SHARE && requireContext().accountOnline.isDocSpace
    }

    @InjectPresenter
    lateinit var presenter: CloudFilterPresenter

    @ProvidePresenter
    fun providePresenter(): CloudFilterPresenter {
        return CloudFilterPresenter(
            arguments?.getString(KEY_FOLDER_ID),
            section
        )
    }

    override val filterPresenter: BaseFilterPresenter
        get() = presenter

    private var typeChipGroup: SingleChoiceChipGroupView? = null
    private var authorChipGroup: SingleChoiceChipGroupView? = null
    private var excludeChipGroup: SingleChoiceChipGroupView? = null
    private var locationChipGroup: SingleChoiceChipGroupView? = null

    private data class SimpleChipItem(
        override val chipTitle: Int,
        override val withOption: Boolean = false,
        override var option: String? = null
    ) : ChipItem

    private val usersChipItem = SimpleChipItem(
        chipTitle = if (isSectionShareWithMe)
            R.string.invite_choose_from_list else
            R.string.share_add_common_header_users,
        withOption = true
    )

    private val groupsChipItem = SimpleChipItem(R.string.share_add_common_header_groups, true)
    private val documentsChipItem = SimpleChipItem(R.string.actionbar_title_main)
    private val roomsChipItem = SimpleChipItem(R.string.main_pager_docs_virtual_room)

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
                    getString(R.string.item_owner_self).takeIf { account.id == author.id }
                        ?: author.name
            } else {
                groupsChipItem.option = author.name
            }
        }
        authorChipGroup?.update(usersChipItem, groupsChipItem)
    }

    private fun initChipGroups() {
        addChipGroups(
            createTypeChipGroup(),
            createAuthorChipGroup(),
            createExcludeChipGroup(),
            createLocationChipGroup()
        )
    }

    private fun createTypeChipGroup(): SingleChoiceChipGroupView? {
        typeChipGroup =
            SingleChoiceChipGroupView(requireContext(), R.string.filter_title_type).apply {
                setChips(
                    chips = if (!context?.accountOnline.isDocSpace) FilterType.types else FilterType.typesWithForms,
                    checkedChip = presenter.filterType
                ) { type, checked ->
                    presenter.filterType = if (checked) type else FilterType.None
                    presenter.update()
                }
            }
        return typeChipGroup
    }

    private fun createAuthorChipGroup(): SingleChoiceChipGroupView? {
        authorChipGroup = App.getApp().accountOnline?.let { account ->
            if (account.isPersonal()) return@let null
            updateAuthorChipGroup(account)
            SingleChoiceChipGroupView(
                context = requireContext(),
                title = R.string.filter_title_author
            ).apply {
                setChips(
                    chips = listOfNotNull(
                        usersChipItem,
                        groupsChipItem.takeIf { !isSectionShareWithMe }
                    ),
                    checkedChip = null,
                    closeListener = presenter::clearAuthor
                ) { item, _ ->
                    showAuthorFragment(
                        fragmentManager = parentFragmentManager,
                        isGroups = item == groupsChipItem,
                        selectedId = presenter.filterAuthor.id,
                        withSelf = !isSectionShareWithMe
                    ) { bundle ->
                        presenter.filterAuthor =
                            FilterAuthor.toObject(bundle.getString(FilterAuthorFragment.BUNDLE_KEY_AUTHOR))
                        presenter.update()
                    }
                }
            }
        }
        return authorChipGroup
    }

    private fun createExcludeChipGroup(): SingleChoiceChipGroupView? {
        if (section != ApiContract.SectionType.CLOUD_FAVORITES && !isSectionShareWithMe) {
            excludeChipGroup =
                SingleChoiceChipGroupView(
                    requireContext(),
                    R.string.filter_exclude_subfolders
                ).apply {
                    val excludeSubfolderChipItem =
                        SimpleChipItem(R.string.filter_exclude_subfolders)
                    setChip(
                        chip = excludeSubfolderChipItem,
                        checked = presenter.excludeSubfolder,
                        checkedListener = { _, checked -> presenter.excludeSubfolder = checked }
                    )
                }
        }
        return excludeChipGroup
    }

    private fun createLocationChipGroup(): SingleChoiceChipGroupView? {
        val section = arguments?.getInt(KEY_SECTION)
        if (section == ApiContract.SectionType.CLOUD_FAVORITES) {
            locationChipGroup =
                SingleChoiceChipGroupView(
                    requireContext(),
                    R.string.room_create_thirdparty_location
                ).apply {
                    val chips = listOf(documentsChipItem, roomsChipItem)
                    val checkedChip = when (presenter.location) {
                        1 -> roomsChipItem
                        2 -> documentsChipItem
                        else -> null
                    }
                    setChips(
                        chips = chips,
                        checkedChip = checkedChip
                    ) { item, checked ->
                        presenter.location = when {
                            !checked -> 0
                            item == roomsChipItem -> 1
                            item == documentsChipItem -> 2
                            else -> 0
                        }
                        presenter.update()
                    }
                }
        }
        return locationChipGroup
    }
}