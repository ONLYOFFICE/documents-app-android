package app.editors.manager.ui.dialogs.explorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.editors.manager.databinding.ListExplorerContextMenuBinding
import app.editors.manager.viewModels.main.ExplorerContextViewModel
import lib.toolkit.base.managers.utils.getSerializableExt
import lib.toolkit.base.managers.utils.putArgs
import lib.toolkit.base.ui.dialogs.base.BaseBottomDialog

class ExplorerContextBottomDialog : BaseBottomDialog() {

    companion object {
        private const val KEY_EXPLORER_CONTEXT_STATE = "key_explorer_context_state"
        val TAG = ExplorerContextBottomDialog::class.simpleName

        fun newInstance(state: ExplorerContextState): ExplorerContextBottomDialog {
            return ExplorerContextBottomDialog().putArgs(KEY_EXPLORER_CONTEXT_STATE to state)
        }
    }

    interface OnClickListener {
        fun onContextButtonClick(contextItem: ExplorerContextItem)
    }


    override fun getTheme(): Int {
        return lib.toolkit.base.R.style.Theme_Common_BottomSheetDialog
    }

    private val onClickListener: OnClickListener
        get() = try {
            parentFragmentManager.fragments
                .first { it.isResumed && it is OnClickListener } as OnClickListener
        } catch (_: NoSuchElementException) {
            throw RuntimeException("Parent fragment must be inherited from ${OnClickListener::class.java}")
        }

    private var viewBinding: ListExplorerContextMenuBinding? = null
    private val viewModel: ExplorerContextViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = ListExplorerContextMenuBinding.inflate(layoutInflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding?.contextList?.layoutManager = LinearLayoutManager(requireContext())
        viewBinding?.contextList?.adapter = ExplorerContextAdapter(::onClick).also { adapter ->
            adapter.setItems(viewModel.getContextItems(arguments?.getSerializableExt(KEY_EXPLORER_CONTEXT_STATE)))
        }
    }

    private fun onClick(explorerContextItem: ExplorerContextItem) {
        onClickListener.onContextButtonClick(explorerContextItem)
        dismiss()
    }

}