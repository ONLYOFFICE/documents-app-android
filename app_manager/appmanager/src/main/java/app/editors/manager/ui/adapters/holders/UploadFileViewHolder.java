package app.editors.manager.ui.adapters.holders;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

import app.editors.manager.R;
import app.editors.manager.managers.works.UploadWork;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import butterknife.BindView;
import lib.toolkit.base.managers.utils.StringUtils;

public class UploadFileViewHolder extends BaseViewHolderExplorer<UploadFile> {

    public static final int LAYOUT = R.layout.list_explorer_upload_files;

    @BindView(R.id.view_icon_selectable_image)
    AppCompatImageView mViewIconSelectableImage;
    @BindView(R.id.view_icon_selectable_mask)
    FrameLayout mViewIconSelectableMask;
    @BindView(R.id.view_icon_selectable_layout)
    FrameLayout mViewIconSelectableLayout;
    @BindView(R.id.list_explorer_upload_file_name)
    AppCompatTextView mFileName;
    @BindView(R.id.list_explorer_upload_file_progress)
    AppCompatTextView mFileProgress;
    @BindView(R.id.list_explorer_upload_file_cancel)
    AppCompatImageButton mButtonCancelUpload;
    @BindView(R.id.upload_file_progress_bar)
    ProgressBar mUploadFileProgressBar;
    @BindView(R.id.list_explorer_upload_file_layout)
    ConstraintLayout listExplorerUploadFileLayout;

    private UploadFile mFile;

    public UploadFileViewHolder(View itemView, ExplorerAdapter adapter) {
        super(itemView, adapter);
        mButtonCancelUpload.setOnClickListener(v -> UploadWork.isCancelled = true);
    }

    @Override
    public void bind(UploadFile file) {
        mFile = file;
        if (file.getProgress() == 0) {mUploadFileProgressBar.setProgress(0);
            mFileProgress.setText(R.string.upload_manager_waiting_title);
        } else {
            updateProgress(file);
        }
        mFileName.setText(file.getName());
        mViewIconSelectableLayout.setBackground(null);
        mViewIconSelectableMask.setBackground(null);
        if (file.getUri().getPath() != null){
            mAdapter.setFileIcon(mViewIconSelectableImage, StringUtils.getExtensionFromPath(file.getUri().getPath()));
        }
    }

    public void updateProgress(UploadFile file) {
        final StringBuilder fileProgress = new StringBuilder();
        fileProgress.append(getFileProgress(file)).append(" / ").append(file.getSize());
        mFileProgress.setText(fileProgress);
        mUploadFileProgressBar.setProgress(file.getProgress());
    }

    private char[] getFileProgress(UploadFile file) {
        String stringSize = file.getSize().substring(0, file.getSize().indexOf(" "));
        double total = Double.parseDouble(stringSize.replace(',', '.'));
        double kof = total / 100;
        double progressSize = kof * file.getProgress();
        return String.format(Locale.getDefault(), "%.2f", progressSize).toCharArray();
    }
}
