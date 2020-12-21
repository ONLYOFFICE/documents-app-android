package app.editors.manager.ui.fragments.media;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;



import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.utils.GlideUtils;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.tools.GlideTool;
import lib.toolkit.base.managers.utils.ContentResolverUtils;
import lib.toolkit.base.managers.utils.StringUtils;
import lib.toolkit.base.managers.utils.WeakAsyncUtils;
import lib.toolkit.base.ui.dialogs.common.CommonDialog;

public class MediaImageFragment extends BaseAppFragment implements MediaPagerFragment.OnMediaListener,
        PlaceholderViews.OnClickListener, ContentResolverUtils.OnUriListener {

    public static final String TAG = MediaImageFragment.class.getSimpleName();
    private static final String TAG_DIALOG_SHARE = "TAG_DIALOG_SHARE";
    private static final String TAG_IMAGE = "TAG_IMAGE";
    private static final String TAG_WEB_DAV = "TAG_WEB_DAV";

    private static final int ALPHA_DELAY = 500;
    private static final float ALPHA_FROM = 0.0f;
    private static final float ALPHA_TO = 1.0f;
    private static final float SCALE_MAX = 5.0f;
    private static final float SCALE_MIN = 1.0f;

    @BindView(R.id.media_image_container)
    protected FrameLayout mMediaImageContainer;
    @BindView(R.id.placeholder_layout)
    protected ConstraintLayout mPlaceholderLayout;
    @BindView(R.id.media_image_layout)
    protected ConstraintLayout mMediaImageLayout;
    @BindView(R.id.media_image_view)
    protected PhotoView mPhotoView;
    @BindView(R.id.media_image_progress)
    protected ProgressBar mProgressBar;

    @Inject
    GlideTool mGlideTool;

    private Unbinder mUnbinder;
    private File mImage;
    private Bitmap mBitmap;
    private GifDrawable mGifDrawable;
    private WeakAsyncUtils mAsyncTaskShare;
    private PlaceholderViews mPlaceholderViews;
    private Boolean mIsWebDav;

    public static MediaImageFragment newInstance(final File image, final boolean isWebDav) {
        final MediaImageFragment fragment = new MediaImageFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_IMAGE, image);
        bundle.putBoolean(TAG_WEB_DAV, isWebDav);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_media_image, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onCancelClick(CommonDialog.Dialogs dialogs, @Nullable String tag) {
        super.onCancelClick(dialogs, tag);
        if (tag != null) {
            if (TAG_DIALOG_SHARE.equals(tag)) {
                cancelSharing();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isActivePage() && mPhotoView.getMinimumScale() < mPhotoView.getScale()) {
            mPhotoView.setScale(mPhotoView.getMinimumScale(), true);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelSharing();
        mGlideTool.clear(mGifTarget);
        mGlideTool.clear(mBitmapTarget);
        mUnbinder.unbind();
    }

    @OnClick({R.id.media_image_container, R.id.media_image_view})
    public void onClick() {
        pageClicked();
    }

    @Override
    public void onPageScroll(boolean isActive) {
        if (isActive) {
            if (mGifDrawable != null) {
                mGifDrawable.start();
            }
        }
    }

    @Override
    public void onStartScroll() {
        // Stub
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @Override
    public void onShareClick() {
        if (mBitmap != null) {
            cancelSharing();
            showWaitingDialog(null, getString(R.string.dialogs_common_cancel_button), TAG_DIALOG_SHARE);
            mAsyncTaskShare = ContentResolverUtils.getBitmapUriAsync(requireContext(), mBitmap, this,
                    StringUtils.getNameWithoutExtension(mImage.getTitle()));
        }
    }

    @Override
    public void onRetryClick() {
        setImageState();
        loadImage();
    }

    @Override
    public void onGetUri(@Nullable Uri uri) {
        hideDialog();
        if (uri != null) {
            showFileShareActivity(uri);
        }
    }

    private void init() {
        getArgs();
        initViews();
        loadImage();
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            mImage = (File) bundle.getSerializable(TAG_IMAGE);
            mIsWebDav = bundle.getBoolean(TAG_WEB_DAV);
        }
    }

    private void initViews() {
        mPlaceholderViews = new PlaceholderViews(mPlaceholderLayout);
        mPlaceholderViews.setViewForHide(mMediaImageLayout);
        mPlaceholderViews.setOnClickListener(this);
        mPhotoView.setMaximumScale(SCALE_MAX);
        mPhotoView.setMinimumScale(SCALE_MIN);
        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        setImageState();
    }

    private void setImageState() {
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE);
        mPhotoView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setImageBitmap(@NonNull final Bitmap bitmap) {
        if (mPhotoView != null) {
            mPhotoView.setImageBitmap(bitmap);
            setImageViewReady();
        }
    }

    private void setImageDrawable(@NonNull final GifDrawable gifDrawable) {
        if (mPhotoView != null) {
            mPhotoView.setImageDrawable(gifDrawable);
            setImageViewReady();
        }
    }

    private void setImageViewReady() {
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE);
        mProgressBar.setVisibility(View.GONE);
        mPhotoView.setVisibility(View.VISIBLE);
        mPhotoView.setAlpha(ALPHA_FROM);
        mPhotoView.setScale(SCALE_MIN);
        mPhotoView.animate().alpha(ALPHA_TO).setDuration(ALPHA_DELAY).start();
    }

    private void loadImage() {
        if (mImage != null) {
            if (mIsWebDav){
                loadWebDavImage();
                return;
            }
            Object url;
            if (StringUtils.isImageGif(mImage.getFileExst())) {
                if (mImage.getId().equals("")) {
                    url = mImage.getWebUrl();
                } else {
                    url = GlideUtils.getCorrectLoad(mImage.getViewUrl(), App.getApp().getAppComponent().getPreference());
                }
                mGlideTool.loadGif(mGifTarget, url, false, mGifRequestListener);
            } else {
                if (mImage.getId().equals("")) {
                    url = mImage.getWebUrl();
                } else {
                    url = GlideUtils.getCorrectLoad(mImage.getViewUrl(), App.getApp().getAppComponent().getPreference());
                }
                mGlideTool.load(mBitmapTarget, url, false, mBitmapRequestListener);
            }
        }
    }

    private void loadWebDavImage() {
        Object url = GlideUtils.getWebDavUrl(mImage.getId(), App.getApp().getAppComponent().getAccountsSql().getAccountOnline());
        if  (StringUtils.isImageGif(mImage.getFileExst())) {
            mGlideTool.loadGif(mGifTarget, url, false, mGifRequestListener);
        } else {
            mGlideTool.load(mBitmapTarget, url, false, mBitmapRequestListener);
        }
    }

    private void cancelSharing() {
        if (mAsyncTaskShare != null) {
            mAsyncTaskShare.cancel();
        }
    }

    private boolean isActivePage() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MediaPagerFragment) {
            return ((MediaPagerFragment) fragment).isActiveFragment(this);
        }
        return true;
    }

    private void pageClicked() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MediaPagerFragment) {
            ((MediaPagerFragment) fragment).pageClicked(true);
        }
    }

    /*
     * Glide callbacks
     * */
    private RequestListener<GifDrawable> mGifRequestListener = new RequestListener<GifDrawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA);
            return false;
        }

        @Override
        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
            mGifDrawable = resource;
            resource.start();
            setImageDrawable(mGifDrawable);
            return false;
        }
    };

    private CustomTarget<GifDrawable> mGifTarget = new CustomTarget<GifDrawable>() {
        @Override
        public void onResourceReady(@NonNull GifDrawable resource, Transition<? super GifDrawable> transition) {
            Log.d(TAG, this.getClass().getSimpleName() + " - onResourceReady()");
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            //stub
        }
    };

    private RequestListener<Bitmap> mBitmapRequestListener = new RequestListener<Bitmap>() {

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            mBitmap = resource;
            setImageBitmap(mBitmap);
            return false;
        }
    };

    private CustomTarget<Bitmap> mBitmapTarget = new CustomTarget<Bitmap>() {
        @Override
        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
            Log.d(TAG, this.getClass().getSimpleName() + " - onResourceReady()");
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            //stub
        }
    };

}