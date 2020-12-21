package app.editors.manager.ui.adapters;

import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.mvp.models.account.Recent;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;

class RecentViewHolder extends RecyclerView.ViewHolder {

    private static final int COUNT_SEPARATOR = 4;
    private static final char PATH_SEPARATOR = '/';

    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mRecentImage;
    @BindView(R.id.view_icon_selectable_mask)
    FrameLayout viewIconSelectableMask;
    @BindView(R.id.view_icon_selectable_layout)
    FrameLayout viewIconSelectableLayout;
    @BindView(R.id.list_explorer_file_name)
    AppCompatTextView mRecentFileName;
    @BindView(R.id.list_explorer_file_info)
    AppCompatTextView mRecentFileInfo;
    @BindView(R.id.list_explorer_file_context)
    AppCompatImageButton mRecentContext;
    @BindView(R.id.list_explorer_file_layout)
    ConstraintLayout mRecentFileLayout;
    @BindView(R.id.view_line_separator)
    View aboutLineSeparator;


    public RecentViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Recent recent, RecentAdapter adapter) {
        mRecentFileName.setText(recent.getName());

        String info = TimeUtils.getWeekDate(recent.getDate()) + App.getApp().getString(R.string.placeholder_point) +
                StringUtils.getFormattedSize(adapter.getContext(), recent.getSize());
        mRecentFileInfo.setText(info);

        if (adapter.getOnClick() != null) {
            mRecentFileLayout.setOnClickListener(v -> adapter.getOnClick().onFileClick(recent, getLayoutPosition()));
            mRecentContext.setOnClickListener(v -> adapter.getOnClick().onContextClick(recent, getLayoutPosition()));
        }

        adapter.setFileIcon(mRecentImage, StringUtils.getExtensionFromPath(recent.getName().toLowerCase()));
    }

    private String getPath(String path) {
        int countSeparator = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == PATH_SEPARATOR) {
                countSeparator++;
                if (countSeparator == COUNT_SEPARATOR) {
                    return path.substring(i, path.length() - 1);
                }
            }
        }
        return path;
    }
}
