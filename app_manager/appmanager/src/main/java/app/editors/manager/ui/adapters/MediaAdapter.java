package app.editors.manager.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.AccountSqlTool;
import app.editors.manager.managers.tools.CacheTool;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.adapters.base.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import lib.toolkit.base.managers.tools.GlideTool;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.UiUtils;

public class MediaAdapter extends BaseAdapter<File> {

    private static final int ALPHA_DELAY = 500;
    private static final float ALPHA_FROM = 0.0f;
    private static final float ALPHA_TO = 1.0f;

    @Inject
    Context mContext;

    @Inject
    PreferenceTool mPreferenceTool;

    @Inject
    GlideTool mGlideTool;

    @Inject
    CacheTool mCacheTool;

    @Inject
    AccountSqlTool mSqlTool;

    private int mCellSize;

    public MediaAdapter(final int cellSize) {
        App.getApp().getAppComponent().inject(this);
        mCellSize = cellSize;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int typeHolder) {
        switch (typeHolder) {
            case TYPE_ITEM_ONE: {
                final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_media_image, viewGroup, false);
                return new ViewHolderImage(view);
            }

            case TYPE_ITEM_TWO: {
                final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_media_video, viewGroup, false);
                return new ViewHolderVideo(view);
            }

            default:
                throw new RuntimeException("Unknown type is unacceptable: " + typeHolder);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final File file = getItem(position);
        switch (getExtension(position)) {
            case IMAGE:
            case IMAGE_GIF: {
                final ViewHolderImage mViewHolder = (ViewHolderImage) viewHolder;
                mViewHolder.bind(file);
                break;
            }

            case VIDEO_SUPPORT: {
                final ViewHolderVideo mViewHolder = (ViewHolderVideo) viewHolder;
                mViewHolder.bind(file);

                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (getExtension(position)) {
            case IMAGE:
            case IMAGE_GIF:
                return TYPE_ITEM_ONE;
            case VIDEO_SUPPORT:
                return TYPE_ITEM_TWO;
            default:
                return TYPE_UNKNOWN;
        }
    }

    private StringUtils.Extension getExtension(int position) {
        final File file = getItem(position);
        final String ext = file.getFileExst();
        return StringUtils.getExtension(ext);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Api.HEADER_AUTHORIZATION, mPreferenceTool.getToken());
        headers.put(Api.HEADER_ACCEPT, Api.VALUE_ACCEPT);
        return headers;
    }

    public void setCellSize(int cellSize) {
        mCellSize = cellSize;
    }

    protected class ViewHolderImage extends RecyclerView.ViewHolder {

        @BindView(R.id.media_image_layout)
        FrameLayout mImageLayout;
        @BindView(R.id.media_image_view)
        ImageView mImageView;
        @BindView(R.id.media_image_progress)
        ProgressBar mProgressBar;

        ViewHolderImage(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            UiUtils.setLayoutParams(mImageLayout, mCellSize, mCellSize);
            mImageView.setClickable(false);
            mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            view.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
        }

        public void bind(File file) {
            final RequestListener<Bitmap> requestListener = new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    UiUtils.setLayoutParams(mImageView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    UiUtils.setImageTint(mImageView, R.drawable.ic_media_error, R.color.colorLightWhite);
                    showImage();
                    return true;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    mImageView.setImageBitmap(resource);
                    showImage();
                    return true;
                }
            };

            if (mSqlTool.getAccountOnline() != null && mSqlTool.getAccountOnline().isWebDav()) {
                mGlideTool.load(mImageView, GlideUtils.getWebDavUrl(file.getId(), mSqlTool.getAccountOnline()),
                        false, new Point(mCellSize, mCellSize), requestListener);
            } else if (file.getId().equals("")) {
                mGlideTool.load(mImageView, file.getWebUrl(), false, new Point(mCellSize, mCellSize), requestListener);
            } else {
                mGlideTool.load(mImageView, GlideUtils.getCorrectLoad(file.getViewUrl(), mPreferenceTool),
                        false, new Point(mCellSize, mCellSize),
                        requestListener);
            }

        }

        private void showImage() {
            mProgressBar.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setAlpha(ALPHA_FROM);
            mImageView.animate().alpha(ALPHA_TO).setDuration(ALPHA_DELAY).start();
        }
    }

    protected class ViewHolderVideo extends RecyclerView.ViewHolder {

        @BindView(R.id.media_video_layout)
        FrameLayout mVideoLayout;
        @BindView(R.id.media_video_view)
        ImageView mVideoView;
        @BindView(R.id.view_icon_background_layout)
        FrameLayout mViewIconBackgroundLayout;
        @BindView(R.id.view_icon_background_image)
        ImageView mViewIconBackgroundImage;

        ViewHolderVideo(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            UiUtils.setLayoutParams(mVideoLayout, mCellSize, mCellSize);
            mVideoView.setClickable(false);
            mViewIconBackgroundLayout.setClickable(false);
            view.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
        }

        public void bind(File file) {
            mViewIconBackgroundLayout.setBackgroundResource(R.drawable.drawable_media_background_video_play_light);
            UiUtils.setImageTint(mViewIconBackgroundImage, R.drawable.ic_media_play, R.color.colorPrimary);
            if (file.getId().equals("")) {
                mCacheTool.getBitmap(file.getWebUrl(), bitmap -> {
                    if (bitmap == null) {
                        Bitmap b = ThumbnailUtils.createVideoThumbnail(file.getWebUrl(), MediaStore.Images.Thumbnails.MINI_KIND);
                        mVideoView.setImageBitmap(b);
                        mCacheTool.addBitmap(file.getWebUrl(), b, null);
                    } else {
                        mVideoView.setImageBitmap(bitmap);
                    }
                    showVideo();
                });
            } else {
                mCacheTool.getBitmap(file.getViewUrl(), bitmap -> getFrame(bitmap, file.getViewUrl()));
            }
        }

        private void getFrame(Bitmap bitmap, String url) {
            if (bitmap == null) {
                // TODO fix queue
                ContentResolverUtils.getFrameFromWebVideoAsync(url, getHeaders(), new Point(mCellSize, mCellSize),
                        bitmapFrame -> {
                            if (bitmapFrame != null) {
                                mVideoView.setImageBitmap(bitmapFrame);
                                mCacheTool.addBitmap(url, bitmapFrame, null);
                                showVideo();
                            }
                        });
            } else {
                mVideoView.setImageBitmap(bitmap);
                showVideo();
            }
        }

        private void showVideo() {
            mViewIconBackgroundLayout.setBackgroundResource(R.drawable.drawable_media_background_video_play_grey);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setAlpha(ALPHA_FROM);
            mVideoView.animate().alpha(ALPHA_TO).setDuration(ALPHA_DELAY).start();
        }
    }

}
