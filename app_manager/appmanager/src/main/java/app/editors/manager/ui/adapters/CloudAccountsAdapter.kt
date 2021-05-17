package app.editors.manager.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.CloudAccount
import app.editors.manager.R
import app.editors.manager.ui.adapters.holders.CloudAccountsViewHolder
import lib.toolkit.base.managers.extensions.inflate
import lib.toolkit.base.ui.adapters.BaseListAdapter
import java.lang.ref.WeakReference

class CloudAccountsAdapter(
    private val accountClickListener: ((account: CloudAccount) -> Unit),
    private val accountLongClickListener: ((account: CloudAccount) -> Unit),
    private val accountContextClickListener: ((account: CloudAccount, position: Int, view: View) -> Unit),
    private val addClickListener: (() -> Unit)
) : BaseListAdapter<CloudAccount>() {


    var isSelectionMode = false
    private var mClickedView: WeakReference<View>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CloudAccountsViewHolder(parent.inflate(R.layout.account_list_item_layout))
//
//        } else {
//            AddViewHolder(addClickListener, isSelectionMode, parent.inflate(R.layout.add_account_item_layout))
//        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddViewHolder) {
//            holder.bind()
        } else if (holder is CloudAccountsViewHolder) {
            holder.bind(
                mList[position],
                isSelectionMode,
                accountClickListener,
                accountLongClickListener,
                accountContextClickListener
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mList.size - 1) {
            2
        } else {
            1
        }
    }

    override fun setItems(list: MutableList<CloudAccount>) {
        list.add(CloudAccount(""))
        super.setItems(list)
    }

    val clickedContextView: View?
        get() = mClickedView?.get()

}

//internal class AddViewHolder(
//    private val listener: (() -> Unit)? = null,
//    private val isSelection: Boolean,
//    view: View
//) : RecyclerView.ViewHolder(view) {
//
//    private val addLayout: LinearLayoutCompat = view.findViewById(R.id.fragment_accounts_add_account)
//
//    fun bind() {
//        if (isSelection) {
//            itemView.visibility = View.GONE
//        } else {
//            itemView.visibility = View.VISIBLE
//        }
//        addLayout.setOnClickListener {
//            listener?.invoke()
//        }
//    }
//}