package app.editors.manager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.documents.core.account.CloudAccount
import app.documents.core.webdav.WebDavApi
import app.editors.manager.R
import lib.toolkit.base.ui.adapters.BaseAdapter
import lib.toolkit.base.ui.adapters.BaseListAdapter

class BottomAccountAdapter : BaseListAdapter<CloudAccount>() {

    fun interface OnAddAccountClick {
        fun onAddAccountClick()
    }

    private var onAddAccountClick: OnAddAccountClick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_account_item, parent, false)
            AccountViewHolder(view, mOnItemClickListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.add_account_item_layout, parent, false)
            AddViewHolder(view)
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
        list.add(CloudAccount(id = ""))
        super.setItems(list)
    }

    fun setOnAddAccountClick(onAddAccountClick: OnAddAccountClick?) {
        this.onAddAccountClick = onAddAccountClick
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AccountViewHolder) {
            holder.bind(mList[position]!!)
        } else if (holder is AddViewHolder) {
            holder.bind()
        }
    }

    internal inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val accountsAddLayout: LinearLayoutCompat = view.findViewById(R.id.fragment_accounts_add_account)

        fun bind() {
            accountsAddLayout.setOnClickListener { v: View? ->
                if (onAddAccountClick != null) {
                    onAddAccountClick!!.onAddAccountClick()
                }
            }
        }

    }

}

class AccountViewHolder(view: View, listener: BaseAdapter.OnItemClickListener?) : RecyclerView.ViewHolder(view) {

    private val portalIcon: AppCompatImageView= view.findViewById(R.id.view_portal_icon)
    private val accountPortal: AppCompatTextView= view.findViewById(R.id.list_account_portal)
    private val accountEmail: AppCompatTextView= view.findViewById(R.id.list_account_email)
    private val accountRadio: RadioButton= view.findViewById(R.id.list_account_radio)
    private val accountLayout: ConstraintLayout = view.findViewById(R.id.list_account_layout)

    init {
        accountLayout.setOnClickListener {
            listener?.onItemClick(
                itemView,
                layoutPosition
            )
        }
    }

    fun bind(account: CloudAccount) {
        accountPortal.text = account.portal
        if (account.login?.isNotEmpty() == true) {
            accountEmail.text = account.login
        } else {
            accountEmail.text = account.name
        }
        accountRadio.isChecked = account.isOnline
        setDrawable(account)
    }

    private fun setDrawable(account: CloudAccount) {
        if (!account.isWebDav) {
            portalIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.mipmap.ic_launcher_foreground))
        } else {
            when (WebDavApi.Providers.valueOf(account.webDavProvider!!)) {
                WebDavApi.Providers.NextCloud -> portalIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, R.drawable.ic_storage_nextcloud
                    )
                )
                WebDavApi.Providers.OwnCloud -> portalIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, R.drawable.ic_storage_owncloud
                    )
                )
                WebDavApi.Providers.Yandex -> portalIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, R.drawable.ic_storage_yandex
                    )
                )
                WebDavApi.Providers.KDrive -> portalIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, R.drawable.ic_storage_kdrive
                    )
                )
                WebDavApi.Providers.WebDav -> portalIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context, R.drawable.ic_storage_webdav
                    )
                )
            }
        }
    }


}