package app.editors.manager.ui.fragments.media;

import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.Api;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import app.editors.manager.ui.views.media.MediaVideoView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.UiUtils;

public class MediaVideoFragment extends BaseAppFragment implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPagerFragment.OnMediaListener,
        MediaVideoView.OnMediaVideoListener, PlaceholderViews.OnClickListener {

    public static final String TAG = MediaVideoFragment.class.getSimpleName();
    private static final String TAG_VIDEO = "TAG_VIDEO";

    private static final int TIME_MEDIA_DURATION = 3000;
    private static final int TIME_START = 0;
    private static final int TIME_PREVIEW = 1;

    @BindView(R.id.media_video_container)
    protected FrameLayout mVideoContainer;
    @BindView(R.id.media_video_layout)
    protected ConstraintLayout mVideoLayout;
    @BindView(R.id.media_video_wrapper)
    protected FrameLayout mVideoWrapper;
    @BindView(R.id.media_video_view)
    protected MediaVideoView mVideoView;
    @BindView(R.id.media_video_progress)
    protected ProgressBar mProgressBar;
    @BindView(R.id.view_icon_background_layout)
    protected FrameLayout mIconBackgroundLayout;
    @BindView(R.id.view_icon_background_image)
    protected ImageView mIconBackgroundImage;
    @BindView(R.id.placeholder_layout)
    protected ConstraintLayout mPlaceholderLayout;

    @Inject
    PreferenceTool mPreferenceTool;

    private Unbinder mUnbinder;
    private PlaceholderViews mPlaceholderViews;
    private File mVideoFile;
    private Uri mVideoUri;
    private MediaController mMediaController;
    private boolean mIsPrepared;
    private boolean mIsError;

    public static MediaVideoFragment newInstance(final File video) {
        final MediaVideoFragment fragment = new MediaVideoFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_VIDEO, video);
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
        final View view = inflater.inflate(R.layout.fragment_media_video, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startPrepareVideo();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateVideoLayoutParams();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVideoView.stopPlayback();
        mUnbinder.unbind();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mIsPrepared = true;
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE);
        mProgressBar.setVisibility(View.GONE);
        mIconBackgroundLayout.setVisibility(View.VISIBLE);
        mIconBackgroundLayout.setBackgroundResource(R.drawable.drawable_media_background_video_play_grey);
        mVideoView.seekTo(TIME_PREVIEW);
        mVideoView.pause();
        hidePosition();
        updateVideoLayoutParams();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!mIsError) {
            mIconBackgroundLayout.setVisibility(View.VISIBLE);
            mVideoView.seekTo(TIME_START);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mIsError = true;
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.MEDIA);
        showSnackBar(getString(R.string.media_video_error, String.valueOf(what)));
        return false;
    }

    @Override
    public void onMediaStart() {
        setStateStart();
    }

    @Override
    public void onMediaPause() {
        setStatePause();
    }

    @Override
    public void onMediaSuspend() {
        mIsPrepared = false;
    }

    @Override
    public void onMediaResume() {
        // Stub
    }

    @OnClick({R.id.media_video_container,
            R.id.media_video_wrapper,
            R.id.view_icon_background_layout})
    public void onWrapperClick(View v) {
        switch (v.getId()) {
            case R.id.media_video_container:
                pageClicked(true);
                break;

            case R.id.media_video_wrapper:
                if (mVideoView.isPlaying() || mIsPrepared) {
                    pageClicked(false);
                    mMediaController.show(TIME_MEDIA_DURATION);
                } else {
                    pageClicked(true);
                }
                break;

            case R.id.view_icon_background_layout:
                if (mIsPrepared) {
                    if (mVideoView.isPlaying()) {
                        mMediaController.show(TIME_MEDIA_DURATION);
                    } else {
                        mVideoView.start();
                    }
                }
                break;
        }
    }

    @Override
    public void onPageScroll(boolean isActive) {
        if (isActive) {
            startPrepareVideo();
            if (mIsPrepared) {
                hidePosition();
            }
        } else {
            stopPrepareVideo();
        }
    }

    @Override
    public void onStartScroll() {
        mMediaController.hide();
    }

    @Override
    public void onShareClick() {
        // Stub
    }

    @Override
    public void onRetryClick() {
        setVideoInitState();
        startPrepareVideo();
    }

    private void init(final Bundle savedInstanceState) {
        getArgs();
        initViews();
        restoreViews(savedInstanceState);
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mVideoFile = (File) bundle.getSerializable(TAG_VIDEO);
        if (mVideoFile.getId().equals("")) {
            mVideoUri = Uri.fromFile(new java.io.File(mVideoFile.getWebUrl()));
        } else {
            mVideoUri = Uri.parse(mVideoFile.getViewUrl());
        }
    }

    private void initViews() {
        mPlaceholderViews = new PlaceholderViews(mPlaceholderLayout);
        mPlaceholderViews.setViewForHide(mVideoLayout);
        mPlaceholderViews.setOnClickListener(this);
        mIconBackgroundLayout.setBackgroundResource(R.drawable.drawable_media_background_video_play_light);
        UiUtils.setImageTint(mIconBackgroundImage, R.drawable.ic_media_play, R.color.colorPrimary);
        mMediaController = new MediaController(getContext());
        mMediaController.setMediaPlayer(mVideoView);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnMediaVideoListener(this);
        mVideoView.setMediaController(mMediaController);
        setVideoInitState();
    }

    private void setVideoInitState() {
        mPlaceholderViews.setTemplatePlaceholder(PlaceholderViews.Type.NONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mIconBackgroundLayout.setVisibility(View.GONE);
    }

    private void restoreViews(final Bundle savedInstanceState) {
        mIsPrepared = false;
        mIsError = false;
    }

    private void updateVideoLayoutParams() {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
        if (isLandscape()) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        mVideoView.setLayoutParams(params);
    }

    private void startPrepareVideo() {
        if (isActivePage() && mVideoView != null && mVideoUri != null) {
            if (!mIsPrepared) {
                if (!mVideoFile.getId().equals("")) {
                    mVideoView.setVideoURI(mVideoUri, getHeaders());
                } else {
                    mVideoView.setVideoURI(mVideoUri);
                }
                mIsError = false;
            }
        }
    }

    private void stopPrepareVideo() {
        if (mVideoView != null && mMediaController != null) {
            if (mIsPrepared) {
                mVideoView.pause();
            } else {
                mVideoView.stopPlayback();
            }
        }
    }

    @Nullable
    private Map<String, String> getHeaders() {
        if (mPreferenceTool != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put(Api.HEADER_AUTHORIZATION, mPreferenceTool.getToken());
            headers.put(Api.HEADER_ACCEPT, Api.VALUE_ACCEPT);
            return headers;
        }
        return null;
    }

    private void setStatePause() {
        mMediaController.hide();
        mIconBackgroundLayout.setVisibility(View.VISIBLE);
    }

    private void setStateStart() {
        mMediaController.show(TIME_MEDIA_DURATION);
        mIconBackgroundLayout.setVisibility(View.GONE);
    }

    private boolean isActivePage() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MediaPagerFragment) {
            return ((MediaPagerFragment) fragment).isActiveFragment(this);
        }
        return true;
    }

    private void pageClicked(final boolean isPosition) {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MediaPagerFragment) {
            ((MediaPagerFragment) fragment).pageClicked(isPosition);
        }
    }

    private void hidePosition() {
        final Fragment fragment = getParentFragment();
        if (fragment instanceof MediaPagerFragment) {
            ((MediaPagerFragment) fragment).hidePosition();
        }
    }

}