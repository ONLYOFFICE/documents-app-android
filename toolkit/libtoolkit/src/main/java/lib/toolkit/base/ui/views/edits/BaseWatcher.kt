package lib.toolkit.base.ui.views.edits

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import kotlin.math.abs

open class BaseWatcher : TextWatcher {

    companion object {
        val TAG = BaseWatcher::class.java.simpleName
    }

    enum class StatesAction {
        NONE, INSERT, APPEND, DELETE, REPLACE
    }

    protected var mEditView: EditText? = null
    protected var mBeforeText: String = ""
    protected var mDeletedText: String = ""
    protected var mResultText: String = ""
    protected var mInputText: String = ""

    protected var mState = StatesAction.NONE
    protected var mSelectionStart = 0
    protected var mSelectionEnd = 0
    protected var mInputSelection = Pair(0, 0)
    protected var mDeleteSelection = Pair(0, 0)
    var mSelectionPosition = 0
    var mSelectionPositionStart = 0
    var mSelectionPositionEnd = 0
    constructor()

    constructor(view: EditText?) {
        mEditView = view
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        Log.d(TAG, "beforeTextChanged($text, $start, $count, $after)")
        mState = StatesAction.NONE
        mBeforeText = text.toString()
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
        mResultText = text.toString()
        mSelectionStart = mEditView?.selectionStart ?: mResultText.length
        mSelectionEnd = mEditView?.selectionEnd ?: mResultText.length

        mInputText = when {
            abs(count - before) > 1 -> {
                mInputSelection = Pair(start, start + count)
                mResultText.substring(mInputSelection.first, mInputSelection.second)
            }

            count >= before -> {
                mInputSelection = Pair(start + before, start + count)
                mResultText.substring(mInputSelection.first, mInputSelection.second)
            }

            else -> {
                mInputSelection = Pair(0, 0)
                ""
            }
        }

        mDeletedText = when {
            abs(count - before) > 1 -> {
                mDeleteSelection = Pair(start, start + before)
                mBeforeText.substring(mDeleteSelection.first, mDeleteSelection.second)
            }

            count <= before -> {
                mDeleteSelection = Pair(start + count, start + before)
                mBeforeText.substring(mDeleteSelection.first, mDeleteSelection.second)
            }

            else -> {
                mDeleteSelection = Pair(0, 0)
                ""
            }
        }
        mSelectionPosition = start + count
        setState()
    }

    override fun afterTextChanged(s: Editable) {
        Log.d(TAG, "afterTextChanged($mSelectionStart, $mInputText, $mDeletedText, $mResultText)")
    }

    protected fun setState() {
        mState = when {
            mInputText.isNotEmpty() && mDeletedText.isEmpty() -> {
                if (mSelectionStart == mResultText.length) {
                    StatesAction.APPEND
                } else {
                    StatesAction.INSERT
                }
            }

            mInputText.isEmpty() && mDeletedText.isNotEmpty() -> {
                StatesAction.DELETE
            }

            mInputText.isNotEmpty() && mDeletedText.isNotEmpty() -> {
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

            (selection ?: mSelectionStart).let {
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
            if (mEditView?.text?.length!! < mSelectionPosition){
                mEditView?.setSelection(mEditView?.text?.length ?: 0)
            } else {
                mEditView?.setSelection(mSelectionPosition)
            }
        }
    }

}