package app.editors.manager.ui.adapters.holders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.databinding.ListCountriesCodesItemBinding
import app.editors.manager.managers.tools.CountriesCodesTool.Codes
import lib.toolkit.base.ui.adapters.BaseAdapter

class CountriesCodeViewHolder(view: View, mOnItemClickListener: BaseAdapter.OnItemClickListener?) :
    RecyclerView.ViewHolder(view) {

    private val viewBinding = ListCountriesCodesItemBinding.bind(view)

    init {
        viewBinding.root.setOnClickListener { v: View? ->
            mOnItemClickListener?.onItemClick(v, layoutPosition)
        }
    }

    fun bind(codesCurrent: Codes?, codesBefore: Codes?) {
        viewBinding.countriesCodesAlphaText.text = "+" + codesCurrent?.number
        viewBinding.countriesCodesNameText.text = codesCurrent?.name
        if (codesBefore == null || codesCurrent?.name?.get(0) != codesBefore.name[0]) {
            viewBinding.countriesCodesAlphaText.isVisible = true
            viewBinding.countriesCodesAlphaText.text = codesCurrent?.name?.get(0).toString()
        } else {
            viewBinding.countriesCodesAlphaText.isVisible = false
        }
    }
}