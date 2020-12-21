package app.editors.manager.ui.adapters.holders;

import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import app.editors.manager.R;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.managers.utils.UiUtils;
import app.editors.manager.mvp.models.account.AccountsSqlData;
import app.editors.manager.ui.adapters.CloudAccountsAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CloudAccountsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.accountItemLayout)
    public ConstraintLayout mAccountLayout;
    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mViewIconSelectableImage;
    @BindView(R.id.imageCheck)
    AppCompatImageView mViewCheckImage;
    @BindView(R.id.view_icon_selectable_mask)
    FrameLayout mViewIconSelectableMask;
    @BindView(R.id.selectableLayout)
    FrameLayout mViewIconSelectableLayout;
    @BindView(R.id.accountItemName)
    AppCompatTextView mAccountName;
    @BindView(R.id.accountItemPortal)
    AppCompatTextView mAccountPortal;
    @BindView(R.id.accountItemEmail)
    AppCompatTextView mAccountEmail;
    @BindView(R.id.accountItemContext)
    public AppCompatImageButton mAccountContext;

    public CloudAccountsViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(AccountsSqlData account,
                     boolean isSelection,
                     @Nullable CloudAccountsAdapter.OnAccountClick accountClick,
                     @Nullable CloudAccountsAdapter.OnAccountLongClick accountLongClick,
                     @Nullable CloudAccountsAdapter.OnAccountContextClick accountContextClick) {
        mAccountName.setText(account.getName());
        mAccountPortal.setText(account.getPortal());
        mAccountEmail.setText(account.getLogin());

        mAccountContext.setVisibility(View.VISIBLE);

        if (!isSelection) {
            if (account.isOnline()) {
                mViewCheckImage.setVisibility(View.VISIBLE);
            } else {
                mViewCheckImage.setVisibility(View.GONE);
            }
        } else {
            mViewCheckImage.setVisibility(View.GONE);
        }

        if (account.isWebDav()) {
            mAccountName.setVisibility(View.GONE);
            UiUtils.setWebDavImage(account.getWebDavProvider(), mViewIconSelectableImage);
        } else {
            mAccountName.setVisibility(View.VISIBLE);
            final String url = account.getScheme() + account.getPortal() + account.getAvatarUrl();
            Glide.with(mViewIconSelectableImage)
                    .load(GlideUtils.getCorrectLoad(url, account.getToken()))
                    .apply(GlideUtils.getAvatarOptions())
                    .into(mViewIconSelectableImage);
        }

        mViewIconSelectableLayout.setBackground(null);
        mViewIconSelectableMask.setBackground(null);
        if (isSelection) {
            mAccountContext.setVisibility(View.GONE);
            if (account.isSelection()) {
                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask);
            } else {
                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_background);
            }
        }

        setListener(account, accountClick, accountLongClick, accountContextClick);
    }

    private void setListener(AccountsSqlData account,
                             CloudAccountsAdapter.OnAccountClick accountClick,
                             CloudAccountsAdapter.OnAccountLongClick accountLongClick,
                             CloudAccountsAdapter.OnAccountContextClick accountContextClick) {
        mAccountLayout.setOnClickListener(v -> {
            if (accountClick != null) {
                accountClick.onAccountClick(account, getLayoutPosition());
            }
        });

        mAccountLayout.setOnLongClickListener(v -> {
            if (accountLongClick != null) {
                accountLongClick.onAccountLongClick(account, getLayoutPosition());
                return true;
            } else {
                return false;
            }
        });

        mAccountContext.setOnClickListener(v -> {
            if (accountContextClick != null) {
                accountContextClick.onAccountContextClick(account, getLayoutPosition(), v);
            }
        });

    }
}
