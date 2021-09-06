package app.editors.manager.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import app.editors.manager.R
import app.editors.manager.databinding.DialogFragmentMoveCopyBinding
import lib.toolkit.base.ui.dialogs.base.BaseDialog

class MoveCopyDialog : BaseDialog() {

    interface DialogButtonOnClick {
        fun continueClick(tag: String?, action: String?)
    }

    private var buttonTag = TAG_OVERWRITE
    private var viewBinding: DialogFragmentMoveCopyBinding? = null
    private var action: String? = null
    private var folderTitle: String? = null
    var dialogButtonOnClick: DialogButtonOnClick? = null

    private val changeListener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId: Int ->
            viewBinding?.continueButton?.isEnabled = true
            buttonTag = when (checkedId) {
                R.id.overwrite_radio_button -> TAG_OVERWRITE
                R.id.copy_radio_button -> TAG_DUPLICATE
                R.id.skip_radio_button -> TAG_SKIP
                else -> TAG_OVERWRITE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = DialogFragmentMoveCopyBinding.inflate(layoutInflater)
        init()
        return viewBinding?.root
    }

    private fun init() {
        viewBinding?.optionsRadioGroup?.setOnCheckedChangeListener(changeListener)
        arguments?.let {
            action = it.getString(TAG_ACTION)
            folderTitle = it.getString(TAG_FOLDER_NAME)
            setTitle(it.getStringArrayList(TAG_NAME_FILES) ?: listOf())
        }
        setListeners()
    }

    @SuppressLint("StringFormatInvalid")
    private fun setTitle(names: List<String>) {
        if (names.size == 1) {
            val text = String
                .format(getString(R.string.dialog_move_copy_title_one_file), names[0], folderTitle)
            viewBinding?.descriptionTitle?.append(text)
        } else {
            val text = String
                .format(getString(R.string.dialog_move_copy_title_files), names.size, folderTitle)
            viewBinding?.descriptionTitle?.append(text)
        }
    }

    override fun onBackPressed(): Boolean {
        dismiss()
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun setListeners() {
        viewBinding?.let {
            it.continueButton.setOnClickListener {
                dialogButtonOnClick?.continueClick(buttonTag, action)
            }
            it.cancelButton.setOnClickListener {
                onBackPressed()
            }
        }
    }

    companion object {

        @JvmField
        val TAG = MoveCopyDialog::class.java.simpleName
        const val TAG_OVERWRITE = "TAG_OVERWRITE"
        const val TAG_DUPLICATE = "TAG_DUPLICATE"
        const val TAG_SKIP = "TAG_SKIP"
        const val ACTION_MOVE = "ACTION_MOVE"
        const val ACTION_COPY = "ACTION_COPY"

        private const val TAG_NAME_FILES = "TAG_NAME_FILES"
        private const val TAG_ACTION = "TAG_ACTION"
        private const val TAG_FOLDER_NAME = "TAG_FOLDER_NAME"

        @JvmStatic
        fun newInstance(filesName: ArrayList<String>, action: String, titleFolder: String) :
                MoveCopyDialog {

            return MoveCopyDialog().apply {
                arguments = Bundle(3).apply {
                    putStringArrayList(TAG_NAME_FILES, filesName)
                    putString(TAG_ACTION, action)
                    putString(TAG_FOLDER_NAME, titleFolder)
                }
            }
        }
    }
}