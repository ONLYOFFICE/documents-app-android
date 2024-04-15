package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import app.editors.manager.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

interface ChipItem {
    val chipTitleString: String?
        get() = null
    val chipTitle: Int?
    val withOption: Boolean
    var option: String?
}

abstract class ChipGroupView : LinearLayout {

    constructor(context: Context, title: Int, isSingleChoice: Boolean) : super(context) {
        inflate(context, R.layout.single_choice_chip_group_layout, this)
        chipGroup.isSingleSelection = isSingleChoice
        chipGroupTitle.isVisible = false
        chipGroupTitle.setText(title)
        chipGroup.removeAllViews()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val chipGroupTitle: TextView by lazy { checkNotNull(findViewById(R.id.chipGroupTitle)) }
    protected val chipGroup: ChipGroup by lazy { checkNotNull(findViewById(R.id.chipGroup)) }
    protected var chips: MutableList<ChipItem> = mutableListOf()

    private fun getChipLayout(withOption: Boolean): Int {
        return if (withOption) R.layout.single_choice_chip_option_layout else R.layout.single_choice_chip_layout
    }

    protected fun inflate(chip: ChipItem): Chip {
        return LayoutInflater.from(context).inflate(getChipLayout(chip.withOption), chipGroup, false) as Chip
    }

    protected fun Chip.setChipInfo(chip: ChipItem, checked: Boolean = false) {
        if (chip.option != null) {
            text = chip.option
            isChecked = true
            isCloseIconVisible = true
        } else {
            text = chip.chipTitleString ?: context.getString(chip.chipTitle ?: -1)
            isChecked = checked
            isCloseIconVisible = false
        }
    }

    protected fun setTitleVisible() {
        chipGroupTitle.isVisible = true
    }

    fun update(vararg chipItems: ChipItem) {
        clearCheck()
        chipGroup.forEachIndexed { index, chip ->
            (chip as Chip).setChipInfo(chipItems[index])
        }
    }

    fun update(chipItem: ChipItem) {
        val chip = chipGroup.getChildAt(chips.indexOf(chipItem)) as? Chip
        chip?.setChipInfo(chipItem)
    }

    fun clearCheck() {
        chipGroup.clearCheck()
    }
}