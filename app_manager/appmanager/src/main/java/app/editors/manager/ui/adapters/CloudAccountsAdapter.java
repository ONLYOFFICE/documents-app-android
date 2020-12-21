package app.editors.manager.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.adapters.holders.CloudAccountsViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.ui.adapters.BaseListAdapter;

public class CloudAccountsAdapter extends BaseListAdapter<AccountsSqlData> {

    @FunctionalInterface
    public interface OnAccountClick {
        void onAccountClick(AccountsSqlData account, int position);
    }

    @FunctionalInterface
    public interface OnAccountLongClick {
        void onAccountLongClick(AccountsSqlData account, int position);
    }

    @FunctionalInterface
    public interface OnAccountContextClick {
        void onAccountContextClick(AccountsSqlData account, int position, View v);
    }

    @FunctionalInterface
    public interface OnAddAccountClick {
        void onAddAccountClick();
    }

    @Nullable
    private OnAccountClick mOnAccountClick;
    @Nullable
    private OnAccountLongClick mOnAccountLongClick;
    @Nullable
    private OnAccountContextClick mOnAccountContextClick;
    @Nullable
    private OnAddAccountClick mOnAddAccountClick;

    private boolean mIsSelectionMode;
    private WeakReference<View> mClickedView;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item_layout, parent, false);
            return new CloudAccountsViewHolder(view);
        } else {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_account_item_layout, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind();
        } else if (holder instanceof CloudAccountsViewHolder) {
            ((CloudAccountsViewHolder) holder).bind(mList.get(position),
                    mIsSelectionMode,
                    mOnAccountClick,
                    mOnAccountLongClick,
                    mOnAccountContextClick);
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

    public void setOnAccountContextClick(@Nullable OnAccountContextClick onAccountContextClick) {
        this.mOnAccountContextClick = onAccountContextClick;
    }

    public void setOnAccountClick(@Nullable OnAccountClick onAccountClick) {
        this.mOnAccountClick = onAccountClick;
    }

    public void setOnAccountLongClick(@Nullable OnAccountLongClick onAccountLongClick) {
        this.mOnAccountLongClick = onAccountLongClick;
    }

    public void setOnAddAccountClick(@Nullable OnAddAccountClick onAddAccountClick) {
        this.mOnAddAccountClick = onAddAccountClick;
    }

    public boolean isSelectionMode() {
        return mIsSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        mIsSelectionMode = selectionMode;
    }

    public void setClickedContextView(View view) {
        mClickedView = new WeakReference<>(view);
    }

    public View getClickedContextView() {
        return mClickedView.get();
    }

    class AddViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_accounts_add_account)
        protected LinearLayoutCompat mAccountsAddLayout;

        AddViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind() {
            if (mIsSelectionMode) {
                itemView.setVisibility(View.GONE);
            } else {
                itemView.setVisibility(View.VISIBLE);
            }
            mAccountsAddLayout.setOnClickListener(v -> {
                if (mOnAddAccountClick != null) {
                    mOnAddAccountClick.onAddAccountClick();
                }
            });
        }
    }
}
