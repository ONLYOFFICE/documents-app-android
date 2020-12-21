package app.editors.manager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.editors.manager.R;
import app.editors.manager.app.WebDavApi;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.ui.adapters.BaseListAdapter;

public class BottomAccountAdapter extends BaseListAdapter<AccountsSqlData> {

    @FunctionalInterface
    public interface OnAddAccountClick {
        void onAddAccountClick();
    }

    public BottomAccountAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private Context mContext;

    @Nullable
    private OnAddAccountClick mOnAddAccountClick;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_account_item, parent, false);
            return new AccountViewHolder(view);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_account_item_layout, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mList.size() - 1) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public void setItems(List<AccountsSqlData> list) {
        list.add(new AccountsSqlData());
        super.setItems(list);
    }

    public void setOnAddAccountClick(@Nullable OnAddAccountClick onAddAccountClick) {
        this.mOnAddAccountClick = onAddAccountClick;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            ((AccountViewHolder) holder).bind(mList.get(position));
        } else if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind();
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_accounts_add_account)
        protected LinearLayoutCompat mAccountsAddLayout;

        AddViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind() {

            mAccountsAddLayout.setOnClickListener(v -> {
                if (mOnAddAccountClick != null) {
                    mOnAddAccountClick.onAddAccountClick();
                }
            });
        }
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.view_portal_icon)
        AppCompatImageView mViewPortalIcon;
        @BindView(R.id.list_account_portal)
        AppCompatTextView mListAccountPortal;
        @BindView(R.id.list_account_email)
        AppCompatTextView mListAccountEmail;
        @BindView(R.id.list_account_radio)
        RadioButton mListAccountRadio;
        @BindView(R.id.list_account_layout)
        ConstraintLayout mListAccountLayout;


        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListAccountLayout.setOnClickListener(v -> mOnItemClickListener.onItemClick(itemView, getLayoutPosition()));
        }

        public void bind(AccountsSqlData account) {
            mListAccountPortal.setText(account.getPortal());
            mListAccountEmail.setText(account.getLogin());
            mListAccountRadio.setChecked(account.isOnline());
            setDrawable(account);
        }

        private void setDrawable(AccountsSqlData account) {
            if (!account.isWebDav()) {
                mViewPortalIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher_foreground));
            } else {
                WebDavApi.Providers provider = WebDavApi.Providers.valueOf(account.getWebDavProvider());
                switch (provider) {
                    case NextCloud:
                        mViewPortalIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_storage_nextcloud));
                        break;
                    case OwnCloud:
                        mViewPortalIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_storage_owncloud));
                        break;
                    case Yandex:
                        mViewPortalIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_storage_yandex));
                        break;
                    case WebDav:
                        mViewPortalIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_storage_webdav));
                        break;
                }
            }
        }
    }

}

