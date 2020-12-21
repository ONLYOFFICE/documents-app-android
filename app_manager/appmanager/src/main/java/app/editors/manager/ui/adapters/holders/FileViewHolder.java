package app.editors.manager.ui.adapters.holders;

import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;

public class FileViewHolder extends BaseViewHolderExplorer<File> {

    public static final int LAYOUT = R.layout.list_explorer_files;

    @BindView(R.id.list_explorer_file_layout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mViewIconSelectableImage;
    @BindView(R.id.view_icon_selectable_mask)
    FrameLayout mViewIconSelectableMask;
    @BindView(R.id.view_icon_selectable_layout)
    FrameLayout mViewIconSelectableLayout;
    @BindView(R.id.list_explorer_file_name)
    AppCompatTextView mFileName;
    @BindView(R.id.list_explorer_file_info)
    AppCompatTextView mFileInfo;
    @BindView(R.id.list_explorer_file_context)
    AppCompatImageButton mFileContext;
    @BindView(R.id.view_line_separator)
    View mViewLineSeparator;

    public FileViewHolder(View itemView, ExplorerAdapter adapter) {
        super(itemView, adapter);
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

    @OnClick(R.id.list_explorer_file_context)
    public void onContextClick(View view) {
        if (mAdapter.mOnItemContextListener != null) {
            mAdapter.mOnItemContextListener.onItemContextClick(view, getLayoutPosition());
        }
    }

    public void bind(File file) {
        // Get file info
        final StringBuilder filesInfo = new StringBuilder(TimeUtils.getWeekDate(file.getUpdated()));
        filesInfo.append(PLACEHOLDER_POINT).append(StringUtils.getFormattedSize(mAdapter.mContext, file.getPureContentLength()));
        if (mAdapter.mPreferenceTool.getSelfId().equalsIgnoreCase(file.getCreatedBy().getId())) {
            if (!mAdapter.isSectionMy()) {
                filesInfo.append(PLACEHOLDER_POINT).append(mAdapter.mContext.getString(R.string.item_owner_self));
            }
        } else if (!file.getCreatedBy().getTitle().equals("")){
            filesInfo.append(PLACEHOLDER_POINT).append(file.getCreatedBy().getDisplayName());
        }

        mFileName.setText(file.getTitle());
        mFileInfo.setText(filesInfo.toString());
        mFileContext.setVisibility(View.VISIBLE);
        mViewIconSelectableLayout.setBackground(null);
        mViewIconSelectableMask.setBackground(null);
        mAdapter.setFileIcon(mViewIconSelectableImage, file.getFileExst());

        // For selection mode add background/foreground
        if (mAdapter.isSelectMode()) {
            mFileContext.setVisibility(View.GONE);
            if (file.isSelected()) {
                mViewIconSelectableMask.setBackgroundResource(R.drawable.drawable_list_image_select_mask);
            } else {
                mViewIconSelectableLayout.setBackgroundResource(R.drawable.drawable_list_image_select_background);
            }
        }
    }

}
