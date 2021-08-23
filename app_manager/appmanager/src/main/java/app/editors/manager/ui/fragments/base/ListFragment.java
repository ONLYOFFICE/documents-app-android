package app.editors.manager.ui.fragments.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import app.editors.manager.R;
import app.editors.manager.ui.views.custom.PlaceholderViews;
import app.editors.manager.ui.views.recyclers.LoadingScroll;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class ListFragment extends BaseAppFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ListFragment.class.getSimpleName();

    /*
    * Main components
    * */
    @BindView(R.id.placeholder_layout)
    protected ConstraintLayout mPlaceholderLayout;
    @BindView(R.id.list_layout)
    protected ConstraintLayout mListLayout;
    @BindView(R.id.list_of_items)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.list_swipe_refresh)
    protected SwipeRefreshLayout mSwipeRefresh;

    protected Unbinder mUnbinder;
    protected LinearLayoutManager mLinearLayoutManager;
    protected PlaceholderViews mPlaceholderViews;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void init() {
        LayoutAnimationController mRecyclerAnimation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.recycler_view_animation_layout);

        mPlaceholderViews = new PlaceholderViews(mPlaceholderLayout);
        mPlaceholderViews.setViewForHide(mRecyclerView);
        mRecyclerView.setLayoutAnimation(mRecyclerAnimation);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mSwipeRefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(), R.color.colorTransparent));
        mSwipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.colorSecondary));
        mSwipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnScrollListener(new LoadingScroll() {

            @Override
            public void onListEnd() {
                ListFragment.this.onListEnd();
            }
        });
    }

    protected void onListEnd() {

    }

}
