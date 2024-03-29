package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.forEachIndexed
import app.editors.manager.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

interface ChipItem {
    val chipTitleString: String?
            get() = null
    val chipTitle: Int?
    val withOption: Boolean
    var option: Any?
}

class SingleChoiceChipGroupView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val chipGroup: ChipGroup by lazy { checkNotNull(findViewById(R.id.chipGroup)) }
    private val chipGroupTitle: TextView by lazy { checkNotNull(findViewById(R.id.chipGroupTitle)) }
    private val chipItems: MutableList<ChipItem> = mutableListOf()

    init {
        inflate(context, R.layout.single_choice_chip_group_layout, this)
        chipGroup.removeAllViews()
    }

    private fun inflate(chip: ChipItem): Chip {
        return LayoutInflater.from(context).inflate(getChipLayout(chip.withOption), chipGroup, false) as Chip
    }

    private fun getChipLayout(withOption: Boolean): Int {
        return if (withOption) R.layout.single_choice_chip_option_layout else R.layout.single_choice_chip_layout
    }

    fun <T : ChipItem> setChips(
        chipItems: List<T>,
        checkedChip: T?,
        closeListener: (() -> Unit)? = null,
        chipCheckedListener: (T, Boolean) -> Unit
    ) {
        chipItems.forEach { chipItem ->
            setChip(chipItem, chipItem == checkedChip, closeListener, chipCheckedListener)
        }
    }

    fun <T : ChipItem> setChip(
        chip: T,
        checked: Boolean,
        closeListener: (() -> Unit)? = null,
        checkedListener: (T, Boolean) -> Unit
    ) {
        chipItems.add(chip)
        inflate(chip).apply {
            setChipInfo(chip, checked)
            setListeners(chip, closeListener, checkedListener)
            chipGroup.addView(this)
        }
    }

    private fun <T : ChipItem> Chip.setListeners(
        chip: T,
        closeListener: (() -> Unit)?,
        checkedListener: (T, Boolean) -> Unit
    ) {
        if (!chip.withOption) {
            setOnCheckedChangeListener { _, checked -> checkedListener.invoke(chip, checked) }
        } else {
            setOnCloseIconClickListener { closeListener?.invoke() }
            setOnClickListener { checkedListener(chip, true) }
        }
    }

    private fun Chip.setChipInfo(chip: ChipItem, checked: Boolean = false) {
        chip.option?.let { option ->
            text = option as? String
            isChecked = true
            isCloseIconVisible = true
        } ?: run {
            text =
                if (chip.chipTitleString != null) {
                    chip.chipTitleString
                } else {
                    context.getString(chip.chipTitle ?: -1)
                }
            isChecked = checked
            isCloseIconVisible = false
        }
    }

    fun setTitle(title: Int) {
        chipGroupTitle.setText(title)
    }

    fun clearCheck() {
        chipGroup.clearCheck()
    }

    fun update(vararg chipItems: ChipItem) {
        clearCheck()
        chipGroup.forEachIndexed { index, chip ->
            (chip as Chip).setChipInfo(chipItems[index])
        }
    }

    fun update(chipItem: ChipItem) {
        val chip = chipGroup.getChildAt(chipItems.indexOf(chipItem)) as? Chip
        chip?.setChipInfo(chipItem)
    }

}