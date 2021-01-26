package app.editors.manager.ui.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.ui.activities.main.MainActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.pager.PagingViewPager;
import app.editors.manager.ui.views.pager.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainPagerFragment extends BaseAppFragment {

    public static final String TAG = MainPagerFragment.class.getSimpleName();
    private static final String TAG_POSITION = "TAG_POSITION";
    private static final String TAG_VISIBLE = "TAG_VISIBLE";
    private static final String TAG_SCROLL = "TAG_SCROLL";

    public final static int PAGE_DOCS_MY = 0;

    private final static int OFFSCREEN_COUNT = 5;

    @BindView(R.id.main_view_pager)
    protected PagingViewPager mMainViewPager;

    @Inject
    PreferenceTool mPreferenceTool;

    private Unbinder mUnbinder;
    private MainActivity mMainActivity;
    private ViewPagerAdapter mViewPagerAdapter;
    private boolean mIsScroll = true;
    private boolean mIsVisible = true;

    public static MainPagerFragment newInstance(final int position) {
        final MainPagerFragment mainPagerFragment = new MainPagerFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(TAG_POSITION, position);
        mainPagerFragment.setArguments(bundle);
        return mainPagerFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
        try {
            mMainActivity = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(MainPagerFragment.class.getSimpleName() + " - must implement - " +
                    MainActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_main_pager, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(TAG_VISIBLE, mIsVisible);
        outState.putBoolean(TAG_SCROLL, mIsScroll);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMainActivity.getTabLayout().setupWithViewPager(null);
        mMainActivity = null;
        mMainViewPager.setAdapter(null);
        if (getArguments() != null) {
            getArguments().clear();
        }
    }

    private void init(final Bundle savedInstanceState) {
        mMainActivity.showAppBarLayout(true);
        mMainActivity.setAppBarStates(false);
        mMainActivity.setNavigationButton(false);

        mViewPagerAdapter = new AdapterForPages(getChildFragmentManager(), getFragments());
        mMainViewPager.setOffscreenPageLimit(OFFSCREEN_COUNT);
        mMainViewPager.setAdapter(mViewPagerAdapter);
        mMainViewPager.addOnPageChangeListener(mViewPagerAdapter);
        mMainActivity.getTabLayout().setupWithViewPager(mMainViewPager, true);
        mMainActivity.setActionButtonShow(true);

        restoreStates(savedInstanceState);
        getPositionArg();

        mMainActivity.checkAccountInfo();
    }

    private void restoreStates(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_VISIBLE)) {
                mIsVisible = savedInstanceState.getBoolean(TAG_VISIBLE);
            }

            if (savedInstanceState.containsKey(TAG_SCROLL)) {
                mIsScroll = savedInstanceState.getBoolean(TAG_SCROLL);
            }
        }
    }

    private void getPositionArg() {
        final Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(TAG_POSITION)) {
            mMainViewPager.post(() -> setPosition(bundle.getInt(TAG_POSITION)));
        }
    }

    /*
     * Get fragments
     * */
    private List<ViewPagerAdapter.Container> getFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        if (!mPreferenceTool.isPersonalPortal()) {
            if (!mPreferenceTool.getIsVisitor()) {
                pairs.addAll(getMyFragments());
            }
            pairs.addAll(getVisitorFragments());
        } else {
            pairs.addAll(getMyFragments());
        }
        pairs.add(new ViewPagerAdapter.Container(DocsTrashFragment.newInstance(), getString(R.string.main_pager_docs_trash)));
        return pairs;
    }

    private List<ViewPagerAdapter.Container> getVisitorFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(DocsShareFragment.newInstance(),
                getString(R.string.main_pager_docs_share)));
        pairs.add(new ViewPagerAdapter.Container(DocsCommonFragment.newInstance(),
                getString(R.string.main_pager_docs_common)));
        if (!mPreferenceTool.isProjectDisable()) {
            pairs.add(new ViewPagerAdapter.Container(DocsProjectsFragment.newInstance(),
                    getString(R.string.main_pager_docs_projects)));
        }
        pairs.add(new ViewPagerAdapter.Container(DocsFavoritesFragment.newInstance(), getString(R.string.main_pager_docs_favorites)));
        return pairs;
    }

    private List<ViewPagerAdapter.Container> getMyFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(DocsMyFragment.newInstance(),
                getString(R.string.main_pager_docs_my)));
        return pairs;
    }

    public void showActionDialog() {
        ((DocsBaseFragment) getActiveFragment()).showActionDialog();
    }

    boolean isActivePage(final Fragment fragment) {
        return mViewPagerAdapter.isActiveFragment(mMainViewPager, fragment);
    }

    private Fragment getActiveFragment() {
        return mViewPagerAdapter.getActiveFragment(mMainViewPager);
    }

    /*
     * Set states
     * */

    public void setPosition(int position) {
        position = position - getOffsetPosition();
        if (position >= PAGE_DOCS_MY && position < mViewPagerAdapter.getCount()) {
            mMainViewPager.setCurrentItem(position, true);
        }
    }

    private int getOffsetPosition() {
        return mPreferenceTool.getIsVisitor() ? 1 : 0;
    }

    void setScrollViewPager(final boolean isScroll) {
        mIsScroll = isScroll;
        mMainViewPager.setPaging(mIsScroll);
    }

    void setToolbarState(final boolean isRoot) {
        mIsVisible = isRoot;
        mMainActivity.setAppBarStates(mIsVisible);
    }

    void setExpandToolbar() {
        mMainActivity.expandToolBar();
    }

    void setVisibilityActionButton(final boolean isShow) {
        mMainActivity.setActionButtonShow(isShow);
    }

    public int getPosition() {
        return mViewPagerAdapter.getSelectedPage();
    }

    public void setAccountEnable(boolean isEnable) {
        mMainActivity.setToolbarAccount(isEnable);
    }

    public void disableProjectModule() {
        if (mViewPagerAdapter != null){
            mViewPagerAdapter.removeFragment(mViewPagerAdapter.getCount()-1);
        }
    }

    public void setVisibleTabs(boolean isVisible) {
        mMainActivity.setAppBarStates(isVisible);
        mMainActivity.setNavigationButton(true);
    }

    /*
     * Adapter and page change listener
     * */
    private class AdapterForPages extends ViewPagerAdapter {

        AdapterForPages(FragmentManager manager, List<Container> fragmentList) {
            super(manager, fragmentList);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            mMainActivity.expandFloatingActionButton();
            mMainActivity.setPagePosition(position);
            ((DocsCloudFragment) getActiveFragment(mMainViewPager)).onScrollPage();
        }
    }

}
