package app.editors.manager.ui.fragments.filter

import app.documents.core.model.cloud.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.managers.utils.Storage
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.FilterProvider
import app.editors.manager.mvp.models.filter.RoomFilterAuthor
import app.editors.manager.mvp.models.filter.RoomFilterTag
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.presenters.filter.RoomFilterPresenter
import app.editors.manager.ui.views.custom.MultiChoiceChipGroupView
import app.editors.manager.ui.views.custom.SingleChoiceChipGroupView
import lib.toolkit.base.managers.utils.putArgs
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class RoomFilterFragment : BaseFilterFragment() {

    companion object {
        val TAG = RoomFilterFragment::class.simpleName
        private const val KEY_SECTION: String = "key_section"

        fun newInstance(section: Int): RoomFilterFragment {
            return RoomFilterFragment().putArgs(KEY_SECTION to section)
        }
    }

    @InjectPresenter
    lateinit var presenter: RoomFilterPresenter

    @ProvidePresenter
    fun providePresenter(): RoomFilterPresenter {
        return RoomFilterPresenter(arguments?.getInt(KEY_SECTION))
    }

    private var authorChipGroup: SingleChoiceChipGroupView? = null

    private var typeChipGroup: SingleChoiceChipGroupView? = null
    private var tagChipGroup: MultiChoiceChipGroupView<RoomFilterTag>? = null
    private var thirdPartyChipGroup: SingleChoiceChipGroupView? = null

    override val filterPresenter: BaseFilterPresenter
        get() = presenter

    override fun initViews() {
        authorChipGroup = SingleChoiceChipGroupView(requireContext(), R.string.filter_title_author).apply {
            App.getApp().accountOnline?.let { account ->
                updateAuthorChipGroup(account)
                setChip(RoomFilterAuthor.Me, presenter.filterAuthor.id == account.id) { _, checked ->
                    presenter.filterAuthor = if (checked) FilterAuthor(account.id) else FilterAuthor()
                }
                setChip(RoomFilterAuthor.OtherUsers, closeListener = presenter::clearAuthor, checked = false) { _, _ ->
                    showAuthorFragment(
                        fragmentManager = parentFragmentManager,
                        withSelf = false,
                        selectedId = presenter.filterAuthor.id
                    ) { bundle ->
                        presenter.filterAuthor =
                            FilterAuthor.toObject(bundle.getString(FilterAuthorFragment.BUNDLE_KEY_AUTHOR))
                    }
                }
            }
        }

        typeChipGroup = SingleChoiceChipGroupView(requireContext(), R.string.filter_title_type).apply {
            setChips(RoomFilterType.allTypes, presenter.filterType) { type, checked ->
                presenter.filterType = if (checked) type else RoomFilterType.None
            }
        }

        tagChipGroup = MultiChoiceChipGroupView(requireContext(), R.string.toolbar_menu_sort_tags)
        thirdPartyChipGroup = SingleChoiceChipGroupView(requireContext(), R.string.rooms_filter_third_party_title)
        addChipGroups(authorChipGroup, typeChipGroup, tagChipGroup, thirdPartyChipGroup)

        presenter.loadFilter()
    }

    override fun onTagsLoaded(tags: Array<String>) {
        tagChipGroup?.apply {
            setChips(
                chips = tags.map(::RoomFilterTag),
                checkedChips = presenter.filterTags,
                onCheckedChange = { tags -> presenter.filterTags = tags }
            )
        }
    }

    override fun onThirdPartyLoaded(providerKeys: List<String>) {
        thirdPartyChipGroup?.setChips(
            chips = providerKeys.mapNotNull { providerKey ->
                FilterProvider(Storage.get(providerKey) ?: return@mapNotNull null)
            },
            checkedChip = presenter.filterProvider,
            chipCheckedListener = { tag, checked -> presenter.filterProvider = if (checked) tag else null }
        )
    }

    override fun updateViewState(isChanged: Boolean) {
        super.updateViewState(isChanged)
        App.getApp().accountOnline?.let(::updateAuthorChipGroup)
    }

    private fun updateAuthorChipGroup(account: CloudAccount) {
        RoomFilterAuthor.OtherUsers.option = null
        if (presenter.filterAuthor.id.isNotEmpty() && presenter.filterAuthor.id != account.id) {
            RoomFilterAuthor.OtherUsers.option = presenter.filterAuthor.name
        }
        authorChipGroup?.update(RoomFilterAuthor.OtherUsers)
    }
}