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

import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Footer;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer;
import app.editors.manager.ui.adapters.holders.FooterViewHolder;
import app.editors.manager.ui.adapters.holders.UploadFileViewHolder;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;

public class ExplorerAdapter extends BaseAdapter<Entity> {

    @Inject
    public Context mContext;

    @Inject
    public PreferenceTool mPreferenceTool;

    private boolean mIsSelectMode;
    private boolean mIsFoldersMode;
    private boolean mIsFooter;
    private boolean mIsRoot;
    private boolean mIsSectionMy;

    private Footer mFooter;
    private TypeFactory mFactory;


    public ExplorerAdapter(TypeFactory factory) {
        App.getApp().getAppComponent().inject(this);
        mIsSelectMode = false;
        mIsFoldersMode = false;
        mIsFooter = false;
        mIsSectionMy = false;
        mFactory = factory;
        mFooter = new Footer();
    }

    @NonNull
    @Override
    public BaseViewHolderExplorer onCreateViewHolder(ViewGroup viewGroup, int typeHolder) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(typeHolder, viewGroup, false);
        return mFactory.createViewHolder(view, typeHolder, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder viewHolder = (FooterViewHolder) holder;
            viewHolder.bind(mFooter);
        } else {
            BaseViewHolderExplorer viewHolder = (BaseViewHolderExplorer) holder;
            setFileFavoriteStatus(position);
            viewHolder.bind(mList.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, List payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if (holder instanceof UploadFileViewHolder) {
                UploadFileViewHolder viewHolder = (UploadFileViewHolder) holder;
                viewHolder.updateProgress((UploadFile) payloads.get(0));
            }
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return FooterViewHolder.LAYOUT;
        } else {
            return getItemList().get(position).getType(mFactory);
        }
    }


    public void setSelectMode(final boolean isSelectMode) {
        mIsSelectMode = isSelectMode;
        notifyDataSetChanged();
    }

    public void setFoldersMode(final boolean isFoldersMode) {
        mIsFoldersMode = isFoldersMode;
        notifyDataSetChanged();
    }

    public void isLoading(final boolean isShow) {
        mIsFooter = isShow;
        notifyItemChanged(getItemCount() - 1);
    }

    public UploadFile getUploadFileById(String id) {
        if (mList != null) {
            for (Entity file : mList) {
                if (file instanceof UploadFile) {
                    UploadFile uploadFile = (UploadFile) file;
                    if (uploadFile.getId().equals(id)) {
                        return uploadFile;
                    }
                }
            }
        }
        return null;
    }

    public void removeUploadItemById(String id) {
        if (mList != null) {
            for (Entity file : mList) {
                if (file instanceof UploadFile) {
                    UploadFile uploadFile = (UploadFile) file;
                    if (uploadFile.getId().equals(id)) {
                        int position = mList.indexOf(uploadFile);
                        mList.remove(uploadFile);
                        notifyItemRemoved(position);
                        break;
                    }
                }
            }
        }
    }

    private void setFileFavoriteStatus(int position) {
        if(mList.get(position) instanceof File){
            File file = ((File)mList.get(position));
            if(!file.getFileStatus().isEmpty()) {
                int favoriteMask = Integer.parseInt(file.getFileStatus()) & Api.FileStatus.FAVORITE;
                if (favoriteMask != 0) {
                    file.setFavorite(true);
                } else {
                    file.setFavorite(false);
                }
            }
        }
    }

    public void setRoot(boolean root) {
        mIsRoot = root;
    }

    public void setSectionMy(boolean sectionMy) {
        mIsSectionMy = sectionMy;
    }

    public boolean isSelectMode() {
        return mIsSelectMode;
    }

    public boolean isFoldersMode() {
        return mIsFoldersMode;
    }

    public boolean isFooter() {
        return mIsFooter;
    }

    public boolean isRoot() {
        return mIsRoot;
    }

    public boolean isSectionMy() {
        return mIsSectionMy;
    }

    public void setFileIcon(final AppCompatImageView view, final String ext) {
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
                colorId = R.color.colorLightRed;
                break;
            case VIDEO_SUPPORT:
                resId = R.drawable.ic_type_video;
                colorId = R.color.colorBlack;
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

    public void setAlphaIcon(final AppCompatImageView view, @DrawableRes final int resId) {
        view.setImageResource(resId);
        view.setAlpha(UiUtils.getFloatResource(mContext, R.dimen.alpha_medium));
        view.clearColorFilter();
    }

    public void setFolderIcon(final AppCompatImageView view, final Folder folder) {
        @DrawableRes int resId = R.drawable.ic_type_folder;
        if (folder.getShared() && folder.getProviderKey().isEmpty()) {
            resId = R.drawable.ic_type_folder_shared;
        } else if (isRoot() && folder.getProviderItem() && !folder.getProviderKey().isEmpty()) {
            switch (folder.getProviderKey()) {
                case Api.Storage.BOXNET:
                    resId = R.drawable.ic_storage_box;
                    break;
                case Api.Storage.DROPBOX:
                    resId = R.drawable.ic_storage_dropbox;
                    break;
                case Api.Storage.SHAREPOINT:
                    resId = R.drawable.ic_storage_sharepoint;
                    break;
                case Api.Storage.GOOGLEDRIVE:
                    resId = R.drawable.ic_storage_google;
                    break;
                case Api.Storage.ONEDRIVE:
                case Api.Storage.SKYDRIVE:
                    resId = R.drawable.ic_storage_onedrive;
                    break;
                case Api.Storage.YANDEX:
                    resId = R.drawable.ic_storage_yandex;
                    break;
                case Api.Storage.WEBDAV:
                    resId = R.drawable.ic_storage_webdav;
                    view.setImageResource(resId);
                    view.setAlpha(UiUtils.getFloatResource(mContext, R.dimen.alpha_medium));
                    return;
            }

            view.setImageResource(resId);
            view.setAlpha(1.0f);
            view.clearColorFilter();
            return;
        }

        view.setImageResource(resId);
        view.setAlpha(UiUtils.getFloatResource(mContext, R.dimen.alpha_medium));
    }

    public void checkHeaders() {
        if (mList != null) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i) instanceof Header) {
                    Header header = (Header) mList.get(i);
                    int position = mList.indexOf(header);
                    if (position + 1 < mList.size() - 1) {
                        if (mList.get(i + 1) instanceof Header) {
                            mList.remove(header);
                            notifyItemRemoved(position);
                        }
                    } else if (mList.lastIndexOf(header) == mList.size() - 1) {
                        mList.remove(header);
                        notifyItemRemoved(position);
                    }
                }
            }
        }
    }
}
