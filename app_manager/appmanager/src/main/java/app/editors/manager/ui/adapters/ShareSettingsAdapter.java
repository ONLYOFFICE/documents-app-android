package app.editors.manager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.share.Share;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lib.toolkit.base.managers.tools.GlideTool;

public class ShareSettingsAdapter extends BaseAdapter<Entity> {

    @Inject
    Context mContext;

    @Inject
    GlideTool mGlideTool;

    public ShareSettingsAdapter() {
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeHolder) {
        switch (typeHolder) {
            case TYPE_HEADER: {
                final View viewItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_share_settings_header, viewGroup, false);
                return new ViewHolderHeader(viewItem);
            }

            case TYPE_ITEM_ONE: {
                final View viewItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_share_settings_item, viewGroup, false);
                return new ViewHolderItem(viewItem);
            }

            default:
                throw new RuntimeException("Unknown type is unacceptable: " + typeHolder);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Entity item = getItem(position);
        if (item != null && viewHolder != null) {
            if (viewHolder instanceof ViewHolderHeader) {
                final ViewHolderHeader mViewHolder = (ViewHolderHeader) viewHolder;
                final Header header = (Header) item;
                mViewHolder.bind(header);
            } else if (viewHolder instanceof ViewHolderItem) {
                final ViewHolderItem mViewHolder = (ViewHolderItem) viewHolder;
                final Share share = (Share) item;
                // Users - groups
                mViewHolder.bind(share);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        final Entity item = getItem(position);
        if (item instanceof Header) {
            return TYPE_HEADER;
        } else if (item instanceof Share) {
            return TYPE_ITEM_ONE;
        }

        return TYPE_UNKNOWN;
    }

    private void setAccessIcon(final ImageView imageView, final int accessCode) {
        switch (accessCode) {
            case Api.ShareCode.NONE:
            case Api.ShareCode.RESTRICT:
                imageView.setImageResource(R.drawable.ic_access_deny);
                return;
            case Api.ShareCode.REVIEW:
                imageView.setImageResource(R.drawable.ic_access_review);
                break;
            case Api.ShareCode.READ:
                imageView.setImageResource(R.drawable.ic_access_read);
                break;
            case Api.ShareCode.READ_WRITE:
                imageView.setImageResource(R.drawable.ic_access_full);
                break;
            case Api.ShareCode.COMMENT:
                imageView.setImageResource(R.drawable.ic_access_comment);
                break;
            case Api.ShareCode.FILL_FORMS:
                imageView.setImageResource(R.drawable.ic_access_fill_form);
                break;
        }
    }

    /*
     * Header ViewHolder
     * */
    protected class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.list_share_settings_header_title)
        TextView mHeaderTitle;

        public ViewHolderHeader(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Header header) {
            mHeaderTitle.setText(header.getTitle());
        }
    }

    /*
     * Share ViewHolder
     * */
    protected class ViewHolderItem extends RecyclerView.ViewHolder {

        @BindView(R.id.list_share_settings_items_layout)
        ConstraintLayout mListShareItemLayout;
        @BindView(R.id.list_share_settings_image)
        AppCompatImageView mListShareImage;
        @BindView(R.id.list_share_settings_name)
        AppCompatTextView mListShareItemName;
        @BindView(R.id.list_share_settings_info)
        AppCompatTextView mListShareItemInfo;
        @BindView(R.id.list_share_settings_context_layout)
        ConstraintLayout mListShareItemContextLayout;
        @BindView(R.id.button_popup_image)
        AppCompatImageView mListShareItemContextButton;

        public ViewHolderItem(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.list_share_settings_context_layout)
        protected void onContextClick(final View view) {
            if (mOnItemContextListener != null) {
                mOnItemContextListener.onItemContextClick(view, getLayoutPosition());
            }
        }

        public void bind(Share share) {
            if (share.getSharedTo().getUserName() != null) {
                mListShareItemInfo.setVisibility(View.VISIBLE);
                mListShareItemName.setText(share.getSharedTo().getDisplayNameHtml());

                // Set info if not empty
                final String info = share.getSharedTo().getDepartment().trim();
                if (!info.isEmpty()) {
                    mListShareItemInfo.setVisibility(View.VISIBLE);
                    mListShareItemInfo.setText(info);
                } else {
                    mListShareItemInfo.setVisibility(View.GONE);
                }

                // Set avatar
                mGlideTool.loadCrop(mListShareImage,
                        GlideUtils.getCorrectLoad(share.getSharedTo().getAvatarSmall(), App.getApp().getAppComponent().getPreference()),
                        true, R.drawable.drawable_list_share_image_item_user_placeholder,
                        R.drawable.drawable_list_share_image_item_user_placeholder);

            } else {
                mListShareItemInfo.setVisibility(View.GONE);
                mListShareItemName.setText(share.getSharedTo().getName());
                mListShareImage.setImageResource(R.drawable.drawable_list_share_image_item_group_placeholder);
            }

            // Access icons
            setAccessIcon(mListShareItemContextButton, share.getAccess());
        }
    }

}
