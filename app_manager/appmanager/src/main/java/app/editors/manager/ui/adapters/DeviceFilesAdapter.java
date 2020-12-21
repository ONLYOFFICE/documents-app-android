package app.editors.manager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

import app.editors.manager.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.TimeUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.adapters.BaseListAdapter;

public class DeviceFilesAdapter extends BaseListAdapter<File> {

    public interface OnDeviceFileClick {
        void onFileClick(File file, int position);

        void onFolderClick(File folder, int position);

        void onContextClick(View view, File file, int position);
    }

    private OnDeviceFileClick mClickListener;
    private Context mContext;
    private WeakReference<View> mClickedView;

    public DeviceFilesAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_device_file_item, parent, false);
        return new DeviceFilesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DeviceFilesViewHolder) {
            ((DeviceFilesViewHolder) holder).bind(mList.get(position));
        }
    }

    public void setClickListener(OnDeviceFileClick mClickListener) {
        this.mClickListener = mClickListener;
    }

    public View getClickedView() {
        if (mClickedView != null) {
            return mClickedView.get();
        } else {
            return new View(mContext);
        }
    }

    class DeviceFilesViewHolder extends RecyclerView.ViewHolder {

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

        DeviceFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        public void bind(File file) {
            mFileContext.setOnClickListener(v -> {
                mClickedView = new WeakReference<>(mFileContext);
                mClickListener.onContextClick(mFileContext, file, getLayoutPosition());
            });
            mFileContext.setImageResource(R.drawable.ic_list_context_button);
            mConstraintLayout.setOnLongClickListener(v -> {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(v, getLayoutPosition());
                }
                return false;
            });
            if (file.isFile()) {
                bindFile(file);
            } else if (file.isDirectory()) {
                bindFolder(file);
            }
        }

        private void bindFolder(File file) {
            mFileInfo.setText(TimeUtils.formatDate(new Date(file.lastModified())));
            mFileName.setText(file.getName());
            mConstraintLayout.setOnClickListener(v -> mClickListener.onFolderClick(file, getLayoutPosition()));
            mViewIconSelectableImage.setImageResource(R.drawable.ic_type_folder);
            mViewIconSelectableImage.setAlpha(1.0f);
            mViewIconSelectableImage.clearColorFilter();
        }

        private void bindFile(File file) {
            mFileInfo.setText(new StringBuilder().append(TimeUtils.formatDate(new Date(file.lastModified())))
                    .append(" ")
                    .append(StringUtils.getFormattedSize(mContext, file.length())));
            mFileName.setText(file.getName());
            mConstraintLayout.setOnClickListener(v -> mClickListener.onFileClick(file, getLayoutPosition()));
            setFileIcon(file.getName());
        }

        public void setFileIcon(final String name) {
            final StringUtils.Extension extension = StringUtils.getExtension(StringUtils.getExtensionFromPath(name.toLowerCase()));
            @DrawableRes int resId = R.drawable.ic_type_file;
            @ColorRes int colorId = R.color.colorGrey;
            switch (extension) {
                case DOC:
                    resId = R.drawable.ic_type_text_document;
                    colorId = R.color.colorDocTint;
                    break;
                case SHEET:
                    resId = R.drawable.ic_type_spreadsheet;
                    colorId = R.color.colorSheetTint;
                    break;
                case PRESENTATION:
                    resId = R.drawable.ic_type_presentation;
                    colorId = R.color.colorPresentationTint;
                    break;
                case IMAGE:
                case IMAGE_GIF:
                    resId = R.drawable.ic_type_image;
                    colorId = R.color.colorPicTint;
                    break;
                case HTML:
                case EBOOK:
                case PDF:
                    resId = R.drawable.ic_type_pdf;
                    colorId = R.color.colorPdfTint;
                    break;
                case VIDEO_SUPPORT:
                    resId = R.drawable.ic_type_video;
                    colorId = R.color.colorVideoTint;
                    break;
                case VIDEO:
                    setAlphaIcon(mViewIconSelectableImage, R.drawable.ic_type_video);
                    return;
                case ARCH:
                    setAlphaIcon(mViewIconSelectableImage, R.drawable.ic_type_archive);
                    return;
                case UNKNOWN:
                    setAlphaIcon(mViewIconSelectableImage, R.drawable.ic_type_file);
                    return;
            }

            mViewIconSelectableImage.setImageResource(resId);
            mViewIconSelectableImage.setAlpha(1.0f);
            mViewIconSelectableImage.setColorFilter(ContextCompat.getColor(mContext, colorId));
        }

        void setAlphaIcon(final AppCompatImageView view, final int resId) {
            view.setImageResource(resId);
            view.setAlpha(UiUtils.getFloatResource(mContext, R.dimen.alpha_medium));
            view.clearColorFilter();
        }
    }
}
