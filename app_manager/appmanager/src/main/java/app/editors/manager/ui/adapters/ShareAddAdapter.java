package app.editors.manager.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.base.ItemProperties;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.mvp.models.user.Group;
import app.editors.manager.mvp.models.user.User;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.managers.tools.GlideTool;

public class ShareAddAdapter extends BaseAdapter<Entity> implements Filterable {

    public enum Mode {
        USERS, GROUPS, COMMON
    }

    private AdapterFilter mAdapterFilter;
    private List<Entity> mDefaultList;
    private Mode mMode;
    private final int mGuideLine;
    private final int mLeftMargin;

    @Inject
    Context mContext;

    @Inject
    GlideTool mGlideTool;

    @Inject
    PreferenceTool mPreferenceTool;

    public ShareAddAdapter() {
        App.getApp().getAppComponent().inject(this);
        mMode = Mode.USERS;
        mGuideLine = (int) mContext.getResources().getDimension(R.dimen.share_group_guideline);
        mLeftMargin = (int) mContext.getResources().getDimension(R.dimen.screen_margin_large);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeHolder) {
        switch (typeHolder) {
            case TYPE_HEADER: {
                final View viewItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_share_add_header, viewGroup, false);
                return new ViewHolderHeader(viewItem);
            }

            case TYPE_ITEM_ONE: {
                final View viewItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_share_add_item, viewGroup, false);
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
                mViewHolder.mHeaderTitle.setText(header.getTitle());
            } else if (viewHolder instanceof ViewHolderItem) {
                final ViewHolderItem mViewHolder = (ViewHolderItem) viewHolder;

                // Hide or show alpha groups
                if (mMode.ordinal() == Mode.USERS.ordinal() && (item instanceof User)) {
                    // Set alpha text groups
                    final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mViewHolder.mGuideline1.getLayoutParams();
                    params.guideBegin = mGuideLine;
                    mViewHolder.mGuideline1.setLayoutParams(params);

                    // Get letter
                    final User userBefore = (User) getItem(position - 1);
                    final User user = (User) item;
                    if (userBefore == null || (userBefore != null && user.getDisplayNameHtml().charAt(0) != userBefore.getDisplayNameHtml().charAt(0))) {
                        mViewHolder.mAlphaText.setVisibility(View.VISIBLE);
                        mViewHolder.mAlphaText.setText(String.valueOf(user.getDisplayNameHtml().charAt(0)).toUpperCase());
                    } else {
                        mViewHolder.mAlphaText.setVisibility(View.GONE);
                    }

                    // Remove margins for avatar
                    setMargins(mViewHolder.mAvatarImage, 0, 0, 0, 0);
                } else {
                    // Remove alpha word
                    mViewHolder.mAlphaText.setVisibility(View.GONE);
                    final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mViewHolder.mGuideline1.getLayoutParams();
                    params.guideBegin = 0;
                    mViewHolder.mGuideline1.setLayoutParams(params);

                    // Set margins for avatar
                    setMargins(mViewHolder.mAvatarImage, mLeftMargin, 0, 0, 0);
                }

                // Set types of list
                if (item instanceof User) {
                    final User user = (User) item;
                    mViewHolder.mMainTitle.setText(user.getDisplayNameHtml());
                    mViewHolder.mAvatarImage.setImageResource(R.drawable.drawable_list_share_image_item_user_placeholder);
                    mGlideTool.loadCrop(mViewHolder.mAvatarImage, GlideUtils.getCorrectLoad(user.getAvatarSmall(), mPreferenceTool), false,
                            R.drawable.drawable_list_share_image_item_user_placeholder, R.drawable.drawable_list_share_image_item_user_placeholder);

                    // Set info if not empty
                    final String info = user.getDepartment().trim();
                    if (!info.isEmpty()) {
                        mViewHolder.mInfoTitle.setVisibility(View.VISIBLE);
                        mViewHolder.mInfoTitle.setText(info);
                    } else {
                        mViewHolder.mInfoTitle.setVisibility(View.GONE);
                    }
                } else {
                    final Group group = (Group) item;
                    mViewHolder.mMainTitle.setText(group.getName());
                    mViewHolder.mInfoTitle.setVisibility(View.GONE);
                    mGlideTool.setCrop(mViewHolder.mAvatarImage, R.drawable.drawable_list_share_image_item_group_placeholder);
                }

                // Workaround image set foreground
                if (((ItemProperties) item).isSelected()) {
                    final Drawable srcDrawable = mViewHolder.mAvatarImage.getDrawable();
                    final Drawable maskDrawable = mContext.getDrawable(R.drawable.drawable_list_image_select_mask);
                    final List<Drawable> layers = new ArrayList<>();

                    if (srcDrawable != null) {
                        layers.add(srcDrawable);
                    }

                    layers.add(maskDrawable);
                    final LayerDrawable layerDrawable = new LayerDrawable(layers.toArray(new Drawable[layers.size()]));
                    mViewHolder.mAvatarImage.setImageDrawable(layerDrawable);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        final Entity item = getItem(position);
        if (item instanceof Header) {
            return TYPE_HEADER;
        } else if (item instanceof User || item instanceof Group) {
            return TYPE_ITEM_ONE;
        }

        return TYPE_UNKNOWN;
    }

    @Override
    public Filter getFilter() {
        if (mAdapterFilter == null) {
            mAdapterFilter = new AdapterFilter();
        }

        if (mDefaultList == null && mList != null) {
            mDefaultList = new ArrayList<>(mList);
        }

        return mAdapterFilter;
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        layoutParams.rightMargin = right;
        layoutParams.bottomMargin = bottom;
        view.setLayoutParams(layoutParams);
    }

    public void setMode(final Mode mode) {
        mMode = mode;
    }

    /*
    * Header ViewHolder
    * */
    protected class ViewHolderHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.list_share_add_header_title)
        TextView mHeaderTitle;

        public ViewHolderHeader(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /*
    * Item ViewHolder
    * */
    protected class ViewHolderItem extends RecyclerView.ViewHolder {

        @BindView(R.id.share_add_item_layout)
        ConstraintLayout mShareLayout;
        @BindView(R.id.share_add_item_alpha_text)
        AppCompatTextView mAlphaText;
        @BindView(R.id.guideline1)
        Guideline mGuideline1;
        @BindView(R.id.share_add_item_avatar_image)
        AppCompatImageView mAvatarImage;
        @BindView(R.id.share_add_item_main_title)
        AppCompatTextView mMainTitle;
        @BindView(R.id.share_add_item_info_title)
        AppCompatTextView mInfoTitle;

        public ViewHolderItem(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mShareLayout.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
        }
    }

    private class AdapterFilter extends Filter {

        private final FilterResults mResults;
        private final List<Entity> mFilteredList;
        private final List<Entity> mUserList;
        private final List<Entity> mGroupList;

        public AdapterFilter() {
            mResults = new FilterResults();
            mFilteredList = new ArrayList<>();
            mUserList = new ArrayList<>();
            mGroupList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (mDefaultList == null) {
                return mResults;
            }

            mList = mDefaultList;
            mResults.count = 0;
            mResults.values = null;
            mFilteredList.clear();
            mUserList.clear();
            mGroupList.clear();

            final String upperSymbols = constraint.toString().toUpperCase();
            for (int i = 0; i < mList.size(); i++) {
                final Entity entity = mList.get(i);
                if (entity instanceof User) {
                    final User user = (User) entity;
                    if (user.getDisplayNameHtml().toUpperCase().contains(upperSymbols))  {
                        mUserList.add(entity);
                    }
                } else if (entity instanceof Group) {
                    final Group group = (Group) entity;
                    if (group.getName().toUpperCase().contains(upperSymbols))  {
                        mGroupList.add(entity);
                    }
                }
            }

            // Set users header
            if (!mUserList.isEmpty()) {
                mFilteredList.add(new Header(mContext.getString(R.string.share_goal_user)));
                mFilteredList.addAll(mUserList);
            }

            // Set groups header
            if (!mGroupList.isEmpty()) {
                mFilteredList.add(new Header(mContext.getString(R.string.share_goal_group)));
                mFilteredList.addAll(mGroupList);
            }

            mResults.count = mFilteredList.size();
            mResults.values = mFilteredList;
            return mResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<Entity>) results.values;
            notifyDataSetChanged();
        }

    }

}
