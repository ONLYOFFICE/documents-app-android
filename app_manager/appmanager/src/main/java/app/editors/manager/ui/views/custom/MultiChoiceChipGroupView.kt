package app.editors.manager.ui.views.custom

import android.content.Context
import android.util.AttributeSet

class MultiChoiceChipGroupView<T : ChipItem> : ChipGroupView {

    constructor(context: Context, title: Int) : super(context, title, false)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private var checkedChips: MutableList<T> = mutableListOf()

    fun setChips(
        chips: List<T>,
        checkedChips: List<T>,
        onCheckedChange: (List<T>) -> Unit
    ) {
        this.chips = chips.toMutableList()
        this.checkedChips = checkedChips.toMutableList()

        if (chips.isNotEmpty()) setTitleVisible()
        chips.forEach { chipItem ->
            inflate(chipItem).apply {
                setChipInfo(chipItem, checkedChips.contains(chipItem))
                chipGroup.addView(this)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        this@MultiChoiceChipGroupView.checkedChips.add(chipItem)
                    } else {
                        this@MultiChoiceChipGroupView.checkedChips.remove(chipItem)
                    }
                    onCheckedChange.invoke(this@MultiChoiceChipGroupView.checkedChips)
                }
            }
        }
    }
}