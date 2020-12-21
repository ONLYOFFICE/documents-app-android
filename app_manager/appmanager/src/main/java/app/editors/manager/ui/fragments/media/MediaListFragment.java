package app.editors.manager.ui.fragments.media;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.Objects;

import app.editors.manager.R;
import app.editors.manager.mvp.models.explorer.Explorer;
import app.editors.manager.ui.activities.main.MediaActivity;
import app.editors.manager.ui.adapters.MediaAdapter;
import app.editors.manager.ui.fragments.base.ListFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lib.toolkit.base.managers.utils.UiUtils;
import lib.toolkit.base.ui.adapters.BaseAdapter;

public class MediaListFragment extends ListFragment implements BaseAdapter.OnItemClickListener {

    public static final String TAG = MediaListFragment.class.getSimpleName();
    private static final String TAG_MEDIA = "TAG_MEDIA";
    private static final String TAG_WEB_DAV = "TAG_WEB_DAV";

    private static final int RECYCLER_CACHE_SIZE = 30;

    private MediaActivity mMediaActivity;
    private View mToolbarView;
    private ToolbarViewHolder mToolbarViewHolder;
    private Explorer mMediaExplorer;
    private GridLayoutManager mGridLayoutManager;
    private MediaAdapter mMediaAdapter;
    private int mColumnsCount;
    private boolean mIsWebDav;

    public static MediaListFragment newInstance(final Explorer explorer, final boolean isWebDav) {
        final MediaListFragment fragment = new MediaListFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(TAG_MEDIA, explorer);
        bundle.putBoolean(TAG_WEB_DAV, isWebDav);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMediaActivity = (MediaActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(MediaListFragment.class.getSimpleName() + " - must implement - " +
                    MediaActivity.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_media_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMediaAdapter.setCellSize(getCellSize(mColumnsCount));
        mRecyclerView.setAdapter(mMediaAdapter);
    }

    @Override
    public void onRefresh() {
        // Stub
    }

    @Override
    public void onItemClick(View view, int position) {
        setClickedItem(position);
        showFragment(MediaPagerFragment.newInstance(mMediaExplorer, mIsWebDav), null, false);
    }

    private void init(final Bundle savedInstanceState) {
        getArgs();
        initViews(savedInstanceState);
        setHeader();
    }

    private void getArgs() {
        final Bundle bundle = getArguments();
        mMediaExplorer = (Explorer) Objects
                .requireNonNull(bundle, "Media must not be null")
                .getSerializable(TAG_MEDIA);
        mIsWebDav = bundle.getBoolean(TAG_WEB_DAV);
    }

    private void initViews(final Bundle savedInstanceState) {
        mMediaActivity.setToolbarState(false);
        mSwipeRefresh.setEnabled(false);
        mToolbarView = getLayoutInflater().inflate(R.layout.include_media_header_list, null);
        mToolbarViewHolder = new ToolbarViewHolder(mToolbarView);
        mMediaActivity.setToolbarView(mToolbarView);
        mColumnsCount = getResources().getInteger(R.integer.screen_media_grid_columns);
        mGridLayoutManager = new GridLayoutManager(requireContext(), mColumnsCount);
        mMediaAdapter = new MediaAdapter(getCellSize(mColumnsCount));
        mMediaAdapter.setOnItemClickListener(this);
        mMediaAdapter.setItems(mMediaExplorer.getFiles());
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mMediaAdapter);
        mRecyclerView.setItemViewCacheSize(RECYCLER_CACHE_SIZE);
    }

    private void setHeader() {
        mToolbarViewHolder.mMediaListHeaderName.setText(mMediaExplorer.getCurrent().getTitle());
        mToolbarViewHolder.mMediaListHeaderInfo.setText(getString(R.string.media_image_list_info, String.valueOf(mMediaExplorer.getFiles().size())));
    }

    private int getCellSize(final int columns) {
        return UiUtils.getScreenSize(requireContext()).x / columns;
    }

    private void setClickedItem(final int position) {
        for (int i = 0; i < mMediaExplorer.getFiles().size(); i++) {
            mMediaExplorer.getFiles().get(i).setClicked(i == position);
        }
    }

    /*
    * Custom view for toolbar
    * */
    protected class ToolbarViewHolder {

        @BindView(R.id.media_list_header_layout)
        protected ConstraintLayout mMediaListHeaderLayout;
        @BindView(R.id.media_list_header_name)
        protected AppCompatTextView mMediaListHeaderName;
        @BindView(R.id.media_list_header_view_mode)
        protected AppCompatImageView mMediaListHeaderViewMode;
        @BindView(R.id.media_list_header_info)
        protected AppCompatTextView mMediaListHeaderInfo;

        public ToolbarViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.media_list_header_view_mode)
        public void onHeaderClicks(final View view) {
            showFragment(MediaPagerFragment.newInstance(mMediaExplorer, mIsWebDav), null, false);
        }
    }

}
