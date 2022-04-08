package lib.toolkit.base.ui.views.edits

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import kotlin.math.abs

open class BaseWatcher : TextWatcher {

    companion object {
        val TAG: String = BaseWatcher::class.java.simpleName
    }

    enum class StatesAction {
        NONE, INSERT, APPEND, DELETE, REPLACE
    }

    protected var mEditView: EditText? = null
    protected var beforeText: String = ""
    protected var deletedText: String = ""
    protected var resultText: String = ""
    protected var inputText: String = ""

    protected var state = StatesAction.NONE
    protected var selectionStart = 0
    protected var selectionEnd = 0
    protected var inputSelection = Pair(0, 0)
    protected var deleteSelection = Pair(0, 0)
    var selectionPosition = 0
    var selectionPositionStart = 0
    var selectionPositionEnd = 0
    constructor()

    constructor(view: EditText?) {
        mEditView = view
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        Log.d(TAG, "beforeTextChanged($text, $start, $count, $after)")
        state = StatesAction.NONE
        beforeText = text.toString()
    }

    /*
    * start - position of selection, auto correct(word's begin) or cursor position - 1. (Behavior varies)
    * before - symbols before operation
    * count - symbols after operation
    * count <= before - it means that was DELETE
    * count >= before - it means that wat APPEND or INSERT
    * abs(before - count) > 1 - it means was REPLACE
    * */
    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        Log.d(TAG, "onTextChanged($text, $start, $before, $count)")
        resultText = text.toString()
        selectionStart = mEditView?.selectionStart ?: resultText.length
        selectionEnd = mEditView?.selectionEnd ?: resultText.length

        inputText = when {
            abs(count - before) > 1 -> {
                inputSelection = Pair(start, start + count)
                resultText.substring(inputSelection.first, inputSelection.second)
            }

            count >= before -> {
                if (count == before && text.toString() != beforeText) {
                    text.toString()
                } else {
                    inputSelection = Pair(start + before, start + count)
                    resultText.substring(inputSelection.first, inputSelection.second)
                }
            }

//            count == before -> {
//                if (mBeforeText != text) {
//                    mDeletedText = mBeforeText
//                }
//                text.toString()
//            }

            else -> {
                inputSelection = Pair(0, 0)
                ""
            }
        }

        deletedText = when {
            abs(count - before) > 1 -> {
                deleteSelection = Pair(start, start + before)
                beforeText.substring(deleteSelection.first, deleteSelection.second)
            }

            count <= before -> {
                if (count == before && beforeText != text.toString()) {
                    beforeText
                } else {
                    deleteSelection = Pair(start + count, start + before)
                    beforeText.substring(deleteSelection.first, deleteSelection.second)
                }
            }

            else -> {
                if (resultText.substring(0, before) == beforeText) {
                    deleteSelection = Pair(0, 0)
                    ""
                } else {
                    beforeText
                }

            }
        }
        selectionPosition = start + count
        setState()
    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged($selectionStart, $inputText, $deletedText, $resultText)")
    }

    protected fun setState() {
        state = when {
            inputText.isNotEmpty() && deletedText.isEmpty() -> {
                if (selectionStart == resultText.length) {
                    StatesAction.APPEND
                } else {
                    StatesAction.INSERT
                }
            }

            inputText.isEmpty() && deletedText.isNotEmpty() -> {
                StatesAction.DELETE
            }

            inputText.isNotEmpty() && deletedText.isNotEmpty() -> {
                StatesAction.REPLACE
            }

            else -> {
                StatesAction.NONE
            }
        }
    }

    protected fun setText(text: String, selection: Int? = null) {
        mEditView?.let { view ->
            view.removeTextChangedListener(this)
            view.setText(text)
            view.addTextChangedListener(this)

            (selection ?: selectionStart).let {
                view.setSelection(if (it > text.length) {
                    text.length
                } else {
                    it
                })
            }
        }
    }

    fun setSelection() {
        if (mEditView?.text?.isNotEmpty() != false) {
            if (mEditView?.text?.length!! < selectionPosition){
                mEditView?.setSelection(mEditView?.text?.length ?: 0)
            } else {
                mEditView?.setSelection(selectionPosition)
            }
        }
    }

}