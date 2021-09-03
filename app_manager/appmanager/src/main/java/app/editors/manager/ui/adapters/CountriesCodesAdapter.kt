package app.editors.manager.ui.adapters

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.R
import app.editors.manager.managers.tools.CountriesCodesTool.*
import app.editors.manager.ui.adapters.base.BaseAdapter
import app.editors.manager.ui.adapters.holders.CountriesCodeViewHolder
import lib.toolkit.base.managers.extensions.inflate
import java.util.ArrayList

class CountriesCodesAdapter : BaseAdapter<Codes>(), Filterable {

    private var mAdapterFilter: AdapterFilter? = null
    private var mDefaultList: List<Codes>? = null

    override fun onCreateViewHolder(view: ViewGroup, type: Int): RecyclerView.ViewHolder =
        CountriesCodeViewHolder(view.inflate(R.layout.list_countries_codes_item), mOnItemClickListener)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CountriesCodeViewHolder).bind(getItem(position), getItem(position - 1))
    }

    override fun getFilter(): Filter =
        mAdapterFilter ?: run {
            mDefaultList = ArrayList(mList)
            AdapterFilter()
        }

    private inner class AdapterFilter : Filter() {
        private val mFilteredList: MutableList<Codes> = mutableListOf()
        private val mResults: FilterResults = FilterResults().apply {
            count = 0
            values = null
        }

        override fun performFiltering(constraint: CharSequence): FilterResults {
            val upperSymbols = constraint.toString().uppercase()
            mList = mDefaultList
            mFilteredList.clear()
            mList.forEach { codes ->
                if (codes.name.uppercase().startsWith(upperSymbols)) {
                    mFilteredList.add(codes)
                }
            }
            return mResults.also {
                it.count = mFilteredList.size
                it.values = mFilteredList
            }
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            mList = results.values as List<Codes?>
            notifyDataSetChanged()
        }
    }
}