package app.editors.manager.ui.fragments.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import app.documents.core.network.manager.models.explorer.Item
import app.editors.manager.R
import app.editors.manager.databinding.ShareInviteFragmentLayoutBinding
import app.editors.manager.ui.activities.main.ShareActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.popup.SharePopup
import com.google.android.material.chip.Chip
import lib.toolkit.base.managers.utils.StringUtils
import lib.toolkit.base.managers.utils.getSerializableExt


class ShareInviteFragment : BaseAppFragment() {


    private var viewBinding: ShareInviteFragmentLayoutBinding? = null

    private val item: Item
        get() = checkNotNull(arguments?.getSerializableExt(ITEM_TAG))

    private var shareActivity: ShareActivity? = null

    private val currentTags: ArrayList<String> by lazy { arrayListOf() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareActivity = try {
            context as ShareActivity
        } catch (e: ClassCastException) {
            throw RuntimeException(
                ShareInviteFragment::class.java.simpleName + " - must implement - " +
                        ShareActivity::class.java.simpleName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = ShareInviteFragmentLayoutBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBarTitle(getString(R.string.share_invite_user))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        loadTagsUi()
        initListeners()
        initViews()
    }

    private fun initViews() {
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.setText(R.string.on_boarding_next_button)
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = currentTags.isNotEmpty()
        viewBinding?.sharePanelLayout?.buttonPopupLayout?.isVisible = false
        viewBinding?.sharePanelLayout?.buttonPopupLayout?.setOnClickListener { popupLayout ->
            SharePopup(requireContext(), R.layout.popup_share_menu).apply {
                setContextListener(popupContextListener)
                setItem(item)
                showOverlap(popupLayout, requireActivity())
            }
        }
    }

    private fun initListeners() {
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.setOnClickListener {
            if (getEmails().isEmpty()) {
                viewBinding?.emailCompleteLayout?.error = getString(R.string.share_invite_error_empty)
                return@setOnClickListener
            }
            showFragment(ChooseAccessFragment.newInstance(item, getEmails()), ChooseAccessFragment.TAG, false)
        }
        viewBinding?.sharePanelLayout?.inviteResetButton?.setOnClickListener {
            removeTags()
        }
    }

    private fun removeTags() {
        viewBinding?.chipGroup?.removeAllViews()
        currentTags.clear()
        updateCounter()
    }

    private fun getEmails(): List<String> {
        val emails = arrayListOf<String>()
        viewBinding?.chipGroup?.childCount?.let {
            for (i in 0 until it) {
                val chip = viewBinding?.chipGroup?.getChildAt(i) as Chip
                emails.add(chip.text.toString())
            }
        }
        return emails
    }

    private fun loadTagsUi() {
        val autoCompleteTextView = checkNotNull(viewBinding?.emailCompleteTextView)

        // done keyboard button is pressed
        autoCompleteTextView.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = textView.text.toString()
                if (StringUtils.isEmailValid(text)) {
                    textView.text = null
                    addTag(text)
                    return@setOnEditorActionListener true
                } else {
                    viewBinding?.emailCompleteLayout?.error = getString(R.string.errors_email_syntax_error)
                }
            }
            false
        }

        // space or comma is detected
        autoCompleteTextView.addTextChangedListener {
            viewBinding?.emailCompleteLayout?.error = null
            if (it != null && it.isEmpty()) {
                return@addTextChangedListener
            }

            if (it?.last() == ',' || it?.last() == ' ') {
                val name = it.substring(0, it.length - 1)
                if (!StringUtils.isEmailValid(name)) {
                    viewBinding?.emailCompleteLayout?.error = getString(R.string.errors_email_syntax_error)
                    return@addTextChangedListener
                }
                viewBinding?.emailCompleteTextView?.text = null
                addTag(name)
            }
        }

        // initialize
        for (tag in currentTags) {
            addChipToGroup(tag, currentTags)
        }
    }

    private fun addTag(name: String) {
        if (name.isNotEmpty() && !currentTags.contains(name)) {
            addChipToGroup(name, currentTags)
            currentTags.add(name)
        }
    }

    private fun addChipToGroup(name: String, items: MutableList<String>) {
        val view = layoutInflater.inflate(R.layout.single_choice_chip_email_layout, viewBinding?.chipGroup, false)
        val chip = (view as Chip).apply {
            text = name
            isCheckable = false
            isCloseIconVisible = true
            setCloseIconTintResource(lib.toolkit.base.R.color.colorError)
        }
        viewBinding?.chipGroup?.addView(chip)

        chip.setOnCloseIconClickListener {
            viewBinding?.chipGroup?.removeView(chip)
            items.remove(name)
            currentTags.remove(name)
            updateCounter()
        }
        updateCounter()
    }

    private fun updateCounter() {
        val count = viewBinding?.chipGroup?.childCount ?: 0
        viewBinding?.sharePanelLayout?.sharePanelCountSelectedText?.text = "$count"
        viewBinding?.sharePanelLayout?.sharePanelAddButton?.isEnabled = count > 0
    }

    private val popupContextListener = object : SharePopup.PopupContextListener {
        override fun onContextClick(v: View, sharePopup: SharePopup) {
            sharePopup.hide()
        }
    }

    companion object {

        val TAG: String = ShareInviteFragment::class.java.simpleName

        private const val ITEM_TAG = "item"

        fun newInstance(item: Item) =
            ShareInviteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ITEM_TAG, item)
                }
            }
    }
}
