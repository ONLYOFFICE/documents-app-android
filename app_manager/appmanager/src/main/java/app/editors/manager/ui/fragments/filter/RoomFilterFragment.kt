package app.editors.manager.ui.fragments.filter

import android.os.Bundle
import app.documents.core.storage.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.app.accountOnline
import app.editors.manager.mvp.models.filter.FilterAuthor
import app.editors.manager.mvp.models.filter.RoomFilterAuthor
import app.editors.manager.mvp.models.filter.RoomFilterType
import app.editors.manager.mvp.presenters.filter.BaseFilterPresenter
import app.editors.manager.mvp.presenters.filter.RoomFilterPresenter
import app.editors.manager.ui.views.custom.SingleChoiceChipGroupView
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class RoomFilterFragment : BaseFilterFragment() {

    companion object {
        val TAG = RoomFilterFragment::class.simpleName

        fun newInstance(folderId: String?): RoomFilterFragment {
            return RoomFilterFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_FOLDER_ID, folderId)
                }
            }
        }
    }

    @InjectPresenter
    lateinit var presenter: RoomFilterPresenter

    @ProvidePresenter
    fun providePresenter(): RoomFilterPresenter {
        return RoomFilterPresenter(arguments?.getString(KEY_FOLDER_ID))
    }

    override val filterPresenter: BaseFilterPresenter
        get() = presenter

    private var authorChipGroup: SingleChoiceChipGroupView? = null
    private var typeChipGroup: SingleChoiceChipGroupView? = null
//    private var thirdPartyChipGroup: SingleChoiceChipGroupView? = null

    override fun initViews() {
        authorChipGroup = SingleChoiceChipGroupView(requireContext()).apply {
            App.getApp().accountOnline?.let { account ->
                updateAuthorChipGroup(account)
                setTitle(R.string.filter_title_author)
                setChip(RoomFilterAuthor.Me, presenter.filterAuthor.id == account.id) { _, checked ->
                    presenter.filterAuthor = if (checked) FilterAuthor(account.id) else FilterAuthor()
                    presenter.update()
                }
                setChip(RoomFilterAuthor.OtherUsers, closeListener = presenter::clearAuthor, checked = false) { _, _ ->
                    showAuthorFragment(
                        fragmentManager = parentFragmentManager,
                        isRoom = true,
                        selectedId = presenter.filterAuthor.id
                    ) { bundle ->
                        presenter.filterAuthor =
                            FilterAuthor.toObject(bundle.getString(FilterAuthorFragment.BUNDLE_KEY_AUTHOR))
                    }
                }
            }
        }

        typeChipGroup = SingleChoiceChipGroupView(requireContext()).apply {
            setTitle(R.string.filter_title_type)
            setChips(RoomFilterType.allTypes, presenter.filterType) { type, checked ->
                presenter.filterType = if (checked) type else RoomFilterType.None
            }
        }

//        thirdPartyChipGroup = SingleChoiceChipGroupView(requireContext()).apply {
//            setTitle(R.string.rooms_filter_third_party_title)
//            setChips(RoomFilterThirdParty.allTypes, null) { _, checked ->
//
//            }
//        }

        addChipGroups(authorChipGroup, typeChipGroup)
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