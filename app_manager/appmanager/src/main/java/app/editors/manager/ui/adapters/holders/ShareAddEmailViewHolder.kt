package app.editors.manager.ui.adapters.holders

import android.view.View
import app.editors.manager.R
import app.editors.manager.databinding.AddItemLayoutBinding
import app.editors.manager.mvp.models.ui.AddEmailUi
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.holder.BaseViewHolder

class ShareAddEmailViewHolder(
    view: View,
    private val listener: BaseAdapter.OnItemClickListener?
) : BaseViewHolder<AddEmailUi>(view) {

    private val binding = AddItemLayoutBinding.bind(view)

    override fun bind(item: AddEmailUi) {
        binding.title.setText(R.string.invite_by_email)
        binding.root.setOnClickListener { listener?.onItemClick(binding.root, absoluteAdapterPosition) }
    }

}