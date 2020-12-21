package lib.toolkit.base.ui.views.edits

import android.text.InputFilter
import android.text.Spanned

open class BaseInputFilter : InputFilter {

    protected var mResultString: String = ""

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val resultStr = StringBuilder(dest)
        resultStr.replace(dstart, dend, source.toString())
        mResultString = resultStr.toString()
        return null
    }

}
