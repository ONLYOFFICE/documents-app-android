package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.chip.Chip

class SingleChoiceChipGroupView : ChipGroupView {

    constructor(context: Context, title: Int) : super(context, title, true)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun <T : ChipItem> setChips(
        chips: List<T>,
        checkedChip: T?,
        closeListener: (() -> Unit)? = null,
        chipCheckedListener: (T, Boolean) -> Unit
    ) {
        if (chips.isNotEmpty()) setTitleVisible()
        chips.forEach { chipItem ->
            setChip(chipItem, chipItem == checkedChip, closeListener, chipCheckedListener)
        }
    }

    fun <T : ChipItem> setChip(
        chip: T,
        checked: Boolean,
        closeListener: (() -> Unit)? = null,
        checkedListener: (T, Boolean) -> Unit
    ) {
        setTitleVisible()
        chips.add(chip)
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
}