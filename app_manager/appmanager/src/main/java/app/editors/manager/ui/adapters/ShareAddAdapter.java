package app.editors.manager.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.ui.GroupUi;
import app.editors.manager.mvp.models.ui.ShareHeaderUi;
import app.editors.manager.mvp.models.ui.UserUi;
import app.editors.manager.mvp.models.ui.ViewType;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;

//TODO make divided viewHolder and load avatars, make filter by api args
public class ShareAddAdapter extends BaseAdapter<ViewType> implements Filterable {

    public enum Mode {
        USERS, GROUPS, COMMON
    }

    private AdapterFilter mAdapterFilter;
    private List<ViewType> mDefaultList;
    private Mode mMode;
    private final int mGuideLine;
    private final int mLeftMargin;

    @Inject
    Context mContext;

    public ShareAddAdapter() {
        App.getApp().getAppComponent().inject(this);
        mMode = Mode.USERS;
        mGuideLine = (int) mContext.getResources().getDimension(R.dimen.share_group_guideline);
        mLeftMargin = (int) mContext.getResources().getDimension(R.dimen.screen_margin_large);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeHolder) {
        switch (typeHolder) {
            case R.layout.list_share_add_header:
            case R.layout.list_share_settings_header: {
                final View viewItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_share_add_header, viewGroup, false);
                return new ViewHolderHeader(viewItem);
            }

            case R.layout.list_share_add_item: {
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
        final ViewType item = getItem(position);
        if (item != null && viewHolder != null) {
            if (viewHolder instanceof ViewHolderHeader) {
                final ViewHolderHeader mViewHolder = (ViewHolderHeader) viewHolder;
                final ShareHeaderUi header = (ShareHeaderUi) item;
                mViewHolder.mHeaderTitle.setText(header.getTitle());
            } else if (viewHolder instanceof ViewHolderItem) {
                final ViewHolderItem holder = (ViewHolderItem) viewHolder;

                // Hide or show alpha groups
                if (mMode.ordinal() == Mode.USERS.ordinal() && (item instanceof UserUi)) {
                    // Set alpha text groups
                    final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.mGuideline1.getLayoutParams();
                    params.guideBegin = mGuideLine;
                    holder.mGuideline1.setLayoutParams(params);

                    // Get letter
                    final UserUi userBefore = (UserUi) getItem(position - 1);
                    final UserUi user = (UserUi) item;
                    if (userBefore == null || (userBefore != null && user.getGetDisplayNameHtml().charAt(0) != userBefore.getGetDisplayNameHtml().charAt(0))) {
                        holder.mAlphaText.setVisibility(View.VISIBLE);
                        holder.mAlphaText.setText(String.valueOf(user.getGetDisplayNameHtml().charAt(0)).toUpperCase());
                    } else {
                        holder.mAlphaText.setVisibility(View.GONE);
                    }

                    // Remove margins for avatar
                    setMargins(holder.mAvatarImage, 0, 0, 0, 0);
                } else {
                    // Remove alpha word
                    holder.mAlphaText.setVisibility(View.GONE);
                    final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.mGuideline1.getLayoutParams();
                    params.guideBegin = 0;
                    holder.mGuideline1.setLayoutParams(params);

                    // Set margins for avatar
                    setMargins(holder.mAvatarImage, mLeftMargin, 0, 0, 0);
                }

                // Set types of list
                if (item instanceof UserUi) {
                    final UserUi user = (UserUi) item;
                    holder.mMainTitle.setText(user.getGetDisplayNameHtml());
                    holder.mAvatarImage.setImageResource(R.drawable.drawable_list_share_image_item_user_placeholder);
//                    mGlideTool.loadCrop(mViewHolder.mAvatarImage, GlideUtils.getCorrectLoad(user.getAvatarSmall(), mPreferenceTool), false,
//                            R.drawable.drawable_list_share_image_item_user_placeholder, R.drawable.drawable_list_share_image_item_user_placeholder);

                    // Set info if not empty
                    final String info = user.getDepartment().trim();
                    if (!info.isEmpty()) {
                        holder.mInfoTitle.setVisibility(View.VISIBLE);
                        holder.mInfoTitle.setText(info);
                    } else {
                        holder.mInfoTitle.setVisibility(View.GONE);
                    }
                } else {
                    final GroupUi group = (GroupUi) item;
                    holder.mMainTitle.setText(group.getName());
                    holder.mInfoTitle.setVisibility(View.GONE);
//                    mGlideTool.setCrop(mViewHolder.mAvatarImage, R.drawable.drawable_list_share_image_item_group_placeholder);
                }

                // Workaround image set foreground
                if ((item instanceof UserUi && ((UserUi) item).isSelected()) || (item instanceof GroupUi && ((GroupUi) item).isSelected())) {
                    final Drawable srcDrawable = holder.mAvatarImage.getDrawable();
                    final Drawable maskDrawable = mContext.getDrawable(R.drawable.drawable_list_image_select_mask);
                    final List<Drawable> layers = new ArrayList<>();

                    if (srcDrawable != null) {
                        layers.add(srcDrawable);
                    }

                    layers.add(maskDrawable);
                    final LayerDrawable layerDrawable = new LayerDrawable(layers.toArray(new Drawable[layers.size()]));
                    holder.mAvatarImage.setImageDrawable(layerDrawable);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
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
        ImageView mAvatarImage;
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
        private final List<ViewType> mFilteredList;
        private final List<ViewType> mUserList;
        private final List<ViewType> mGroupList;

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
                final ViewType entity = mList.get(i);
                if (entity instanceof UserUi) {
                    final UserUi user = (UserUi) entity;
                    if (user.getGetDisplayNameHtml().toUpperCase().contains(upperSymbols)) {
                        mUserList.add(entity);
                    }
                } else if (entity instanceof GroupUi) {
                    final GroupUi group = (GroupUi) entity;
                    if (group.getName().toUpperCase().contains(upperSymbols)) {
                        mGroupList.add(entity);
                    }
                }
            }

            // Set users header
            if (!mUserList.isEmpty()) {
                mFilteredList.add(new ShareHeaderUi(mContext.getString(R.string.share_goal_user)));
                mFilteredList.addAll(mUserList);
            }

            // Set groups header
            if (!mGroupList.isEmpty()) {
                mFilteredList.add(new ShareHeaderUi(mContext.getString(R.string.share_goal_group)));
                mFilteredList.addAll(mGroupList);
            }

            mResults.count = mFilteredList.size();
            mResults.values = mFilteredList;
            return mResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<ViewType>) results.values;
            notifyDataSetChanged();
        }

    }

}
