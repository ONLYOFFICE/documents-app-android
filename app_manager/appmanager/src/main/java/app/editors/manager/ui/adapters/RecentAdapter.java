package app.editors.manager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import app.editors.manager.R;
import app.editors.manager.mvp.models.account.Recent;
import app.editors.manager.mvp.models.base.Entity;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.adapters.BaseListAdapter;

public class RecentAdapter extends BaseListAdapter<Entity> {

    public interface OnClick {
        void onFileClick(Recent recent, int position);
        void onContextClick(Recent recent, int position);
    }

    private Context mContext;
    private OnClick mOnClick;

    public RecentAdapter(Context context) {
        mContext = context;
    }

    public OnClick getOnClick() {
        return mOnClick;
    }

    public void setOnClick(OnClick mOnClick) {
        this.mOnClick = mOnClick;
    }

    public Context getContext() {
        return mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_explorer_files, parent, false);
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentViewHolder) {
            RecentViewHolder viewHolder = (RecentViewHolder) holder;
            viewHolder.bind((Recent) mList.get(position), this);
        }
    }

    public void moveItem(int position, int i) {
        Entity newEntity = mList.get(position);
        Entity firstEntity = mList.get(i);
        mList.set(i, newEntity);
        mList.set(position, firstEntity);
        notifyItemMoved(position, i);
    }

    void setFileIcon(final AppCompatImageView view, final String ext) {
        final StringUtils.Extension extension = StringUtils.getExtension(ext);
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
                setAlphaIcon(view, R.drawable.ic_type_video);
                return;
            case ARCH:
                setAlphaIcon(view, R.drawable.ic_type_archive);
                return;
            case UNKNOWN:
                setAlphaIcon(view, R.drawable.ic_type_file);
                return;
        }

        view.setImageResource(resId);
        view.setAlpha(1.0f);
        view.setColorFilter(ContextCompat.getColor(mContext, colorId));
    }

    private void setAlphaIcon(final AppCompatImageView view, @DrawableRes final int resId) {
        view.setImageResource(resId);
        view.setAlpha(UiUtils.getFloatResource(mContext, R.dimen.alpha_medium));
        view.clearColorFilter();
    }

}
