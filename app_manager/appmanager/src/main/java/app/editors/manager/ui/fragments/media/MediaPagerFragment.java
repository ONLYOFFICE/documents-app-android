package app.editors.manager.ui.fragments.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.pager.PagingViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.StringUtils;

public class MediaPagerFragment extends BaseAppFragment {

    public static final String TAG = MediaPagerFragment.class.getSimpleName();
    private static final String TAG_MEDIA = "TAG_MEDIA";
    private static final String TAG_WEB_DAV = "TAG_WEB_DAV";

    public interface OnMediaListener extends PagingViewPager.OnPagerListener {
        void onShareClick();
    }

    @BindView(R.id.media_pager)
    protected PagingViewPager mMediaPager;
    @BindView(R.id.media_pager_position)
    protected AppCompatTextView mMediaPagerPosition;

    private Unbinder mUnbinder;
    private View mToolbarView;
    private MediaActivity mMediaActivity;
    private ToolbarViewHolder mToolbarViewHolder;
    private PagerAdapter mPagerAdapter;
    private int mSelectedPosition;
    private Explorer mMediaExplorer;
    private boolean mIsWebDAv;

    private Runnable mPagerPositionRunnableGone = new Runnable() {
        @Override
        public void run() {
            if (mMediaPagerPosition != null) {
                mMediaPagerPosition.setVisibility(View.GONE);
            }
        }
    };

    private Runnable mPagerPositionRunnableVisible = new Runnable() {
        @Override
        public void run() {
            if (mMediaPagerPosition != null) {
                mMediaPagerPosition.setVisibility(View.VISIBLE);
            }
        }
    };

    public static MediaPagerFragment newInstance(final Explorer explorer, final boolean mIsWebDAv) {
        final MediaPagerFragment fragment = new MediaPagerFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_MEDIA, explorer);
        bundle.putSerializable(TAG_WEB_DAV, mIsWebDAv);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMediaActivity = (MediaActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(MediaPagerFragment.class.getSimpleName() + " - must implement - " +
                    MediaActivity.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_media_pager, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_WRITE_STORAGE: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareFile();
                }
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMediaPagerPosition.removeCallbacks(mPagerPositionRunnableGone);
        mUnbinder.unbind();
    }

    private void init(final Bundle savedInstanceState) {
        getArgs();
        initViews();
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mMediaExplorer = (Explorer) bundle.getSerializable(TAG_MEDIA);
        mIsWebDAv = bundle.getBoolean(TAG_WEB_DAV);
    }

    private void initViews() {
        mMediaActivity.setToolbarState(true);
        mToolbarView = getLayoutInflater().inflate(R.layout.include_media_header_pager, null);
        mToolbarViewHolder = new ToolbarViewHolder(mToolbarView);
        mMediaActivity.setToolbarView(mToolbarView);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mMediaPager.setAdapter(mPagerAdapter);
        mMediaPager.addOnPageChangeListener(mPagerAdapter);
        mMediaPager.setCurrentItem(getClickedPosition());
        mPagerAdapter.onPageSelected(getClickedPosition());
    }

    private int getClickedPosition() {
        if (mMediaExplorer.getFiles().isEmpty()) {
            return 0;
        } else  {
            for (int i = 0; i < mMediaExplorer.getFiles().size(); i++) {
                if (mMediaExplorer.getFiles().get(i).isClicked()) {
                    return i;
                }
            }
        }
        return 0;
    }

    @SuppressLint("MissingPermission")
    private void shareFile() {
        ((OnMediaListener) getActiveFragment()).onShareClick();
    }

    private void setPagerPosition(final int position) {
        mMediaPagerPosition.setText((position + 1) + "/" + mMediaExplorer.getFiles().size());
    }

    private void showPagerPosition(final boolean isVisible) {
        mMediaPagerPosition.removeCallbacks(mPagerPositionRunnableGone);
        mMediaPagerPosition.removeCallbacks(mPagerPositionRunnableVisible);
        mMediaPagerPosition.setText((mSelectedPosition + 1) + "/" + mMediaExplorer.getFiles().size());
        mMediaPagerPosition.setVisibility(View.VISIBLE);
        mMediaPagerPosition.setAlpha(isVisible? MediaActivity.ALPHA_TO : MediaActivity.ALPHA_FROM);
        mMediaPagerPosition.animate().alpha(isVisible? MediaActivity.ALPHA_FROM : MediaActivity.ALPHA_TO).setDuration(MediaActivity.ALPHA_DELAY)
                .withEndAction(isVisible? mPagerPositionRunnableGone : mPagerPositionRunnableVisible).start();
    }

    public Fragment getActiveFragment() {
        return (Fragment) mPagerAdapter.instantiateItem(mMediaPager, mSelectedPosition);
    }

    public boolean isActiveFragment(@NonNull final Fragment fragment) {
        return fragment.equals(getActiveFragment());
    }

    public void pageClicked(boolean isPosition) {
        final boolean isVisible = mMediaActivity.showToolbar();
        if (isPosition) {
            showPagerPosition(isVisible);
        }
    }

    public void hidePosition() {
        if (mMediaPagerPosition.getVisibility() == View.VISIBLE) {
            showPagerPosition(true);
        }
    }

    public void showPosition() {
        if (mMediaPagerPosition.getVisibility() == View.GONE) {
            showPagerPosition(false);
        }
    }

    /*
     * Pager adapter
     * */
    private class PagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (mMediaExplorer != null && mMediaExplorer.getFiles().size() > 0) {
                final File file = mMediaExplorer.getFiles().get(position);
                final String ext = file.getFileExst();
                switch (StringUtils.getExtension(ext)) {
                    case IMAGE:
                    case IMAGE_GIF:
                        return MediaImageFragment.newInstance(file, getArguments().getBoolean(TAG_WEB_DAV));
                    case VIDEO_SUPPORT:
                        return MediaVideoFragment.newInstance(file);
                }
            }

            throw new RuntimeException(TAG + "getItem() - can't return null");
        }

        @Override
        public int getCount() {
            return mMediaExplorer != null && !mMediaExplorer.getFiles().isEmpty() ? mMediaExplorer.getFiles().size() : 0;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d(TAG, "Position: " + position + "; Offset: " + positionOffset + "; Position offset: " + positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            if (!mMediaExplorer.getFiles().isEmpty()) {
                mSelectedPosition = position;
                mToolbarViewHolder.mHeaderNameText.setText(mMediaExplorer.getFiles().get(position).getTitle());
                setPagerPosition(position);
                hideShareForVideo();
                notifyFragmentsScroll(mSelectedPosition);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                notifyFragmentState(getActiveFragment());
            }
        }

        private void hideShareForVideo() {
            final String ext = mMediaExplorer.getFiles().get(mSelectedPosition).getFileExst();
            if (getActiveFragment() instanceof MediaVideoFragment || StringUtils.isImageGif(ext)) {
                mToolbarViewHolder.mShareImageButton.setVisibility(View.GONE);
            } else {
                mToolbarViewHolder.mShareImageButton.setVisibility(View.VISIBLE);
                if (mMediaActivity.isToolbarVisible()) {
                    showPosition();
                }
            }
        }

        private void notifyFragmentScroll(final Fragment fragment, final boolean isActive) {
            if (fragment instanceof PagingViewPager.OnPagerListener) {
                ((PagingViewPager.OnPagerListener) fragment).onPageScroll(isActive);
            }
        }

        private void notifyFragmentState(final Fragment fragment) {
            if (fragment instanceof PagingViewPager.OnPagerListener) {
                ((PagingViewPager.OnPagerListener) fragment).onStartScroll();
            }
        }

        private void notifyFragmentsScroll(final int position) {
            notifyFragmentScroll(getActiveFragment(), true);

            // Notify previous fragment
            if (getCount() > 0 && (position - 1) >= 0) {
                notifyFragmentScroll((Fragment) instantiateItem(mMediaPager, position - 1), false);
            }

            // Notify next fragment
            if ((position + 1) < getCount()) {
                notifyFragmentScroll((Fragment) instantiateItem(mMediaPager, position + 1), false);
            }
        }

    }

    /*
     * Custom view for toolbar
     * */
    protected class ToolbarViewHolder {

        @BindView(R.id.media_pager_header_layout)
        ConstraintLayout mHeaderLayout;
        @BindView(R.id.media_pager_header_name)
        AppCompatTextView mHeaderNameText;
        @BindView(R.id.media_pager_header_share)
        AppCompatImageView mShareImageButton;
        @BindView(R.id.media_pager_header_view_mode)
        AppCompatImageView mViewModeImageButton;

        public ToolbarViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick({ R.id.media_pager_header_share,
                         R.id.media_pager_header_view_mode })
        public void onHeaderClicks(final View view) {
            switch (view.getId()) {
                case R.id.media_pager_header_share:
                    if (checkWritePermission()) {
                        shareFile();
                    }
                    break;
                case R.id.media_pager_header_view_mode:
                    showFragment(MediaListFragment.newInstance(mMediaExplorer, mIsWebDAv), null, false);
                    break;
            }
        }
    }

}
