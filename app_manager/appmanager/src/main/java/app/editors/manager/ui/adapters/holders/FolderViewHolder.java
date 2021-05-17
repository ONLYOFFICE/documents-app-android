package app.editors.manager.ui.adapters.holders;

import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.CloudFolder;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import lib.toolkit.base.managers.utils.TimeUtils;

public class FolderViewHolder extends BaseViewHolderExplorer<CloudFolder> {

    public static final int LAYOUT = R.layout.list_explorer_folder;

    @BindView(R.id.list_explorer_folder_layout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mViewIconSelectableImage;
    @BindView(R.id.view_icon_selectable_mask)
    FrameLayout mViewIconSelectableMask;
    @BindView(R.id.view_icon_selectable_layout)
    FrameLayout mViewIconSelectableLayout;
    @BindView(R.id.list_explorer_folder_name)
    AppCompatTextView mFolderName;
    @BindView(R.id.list_explorer_folder_info)
    AppCompatTextView mFolderInfo;
    @BindView(R.id.list_explorer_folder_context)
    AppCompatImageButton mFolderContext;

    public FolderViewHolder(final View view, ExplorerAdapter adapter) {
        super(view, adapter);
        mConstraintLayout.setOnClickListener(v -> {
            if (mAdapter.mOnItemClickListener != null) {
                mAdapter.mOnItemClickListener.onItemClick(v, getLayoutPosition());
            }
        });

        mConstraintLayout.setOnLongClickListener(v -> {
            if (mAdapter.mOnItemLongClickListener != null) {
                mAdapter.mOnItemLongClickListener.onItemLongClick(v, getLayoutPosition());
            }
            return false;
        });
    }

    @OnClick(R.id.list_explorer_folder_context)
    public void onContextClick(View view) {
        if (mAdapter.mOnItemContextListener != null) {
            mAdapter.mOnItemContextListener.onItemContextClick(view, getLayoutPosition());
        }
    }

    public void bind(CloudFolder folder) {
        // Get folder info
        final StringBuilder folderInfo = new StringBuilder(TimeUtils.getWeekDate(folder.getUpdated()));
        if (mAdapter.mPreferenceTool.getSelfId().equalsIgnoreCase(folder.getCreatedBy().getId())) {
            if (!mAdapter.isSectionMy()) {
                folderInfo.append(PLACEHOLDER_POINT).append(mAdapter.mContext.getString(R.string.item_owner_self));
            }
        } else if (!folder.getCreatedBy().getDisplayName().equals("")) {
            folderInfo.append(PLACEHOLDER_POINT).append(folder.getCreatedBy().getDisplayName());
        }

        mFolderName.setText(folder.getTitle());
        mFolderInfo.setText(folderInfo.toString());
        mFolderContext.setVisibility(View.VISIBLE);
        mViewIconSelectableLayout.setBackground(null);
        mViewIconSelectableMask.setBackground(null);
        mAdapter.setFolderIcon(mViewIconSelectableImage, folder);

        // Show/hide context button
        if (mAdapter.isSelectMode() || mAdapter.isFoldersMode()) {
            mFolderContext.setVisibility(View.GONE);
        }

        // For selection mode add background/foreground
        if (mAdapter.isSelectMode()) {
            if (folder.isSelected()) {
                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask);
            } else {
                mViewIconSelectableLayout.setBackgroundResource(R.drawable.drawable_list_image_select_background);
            }
        }
    }

}
