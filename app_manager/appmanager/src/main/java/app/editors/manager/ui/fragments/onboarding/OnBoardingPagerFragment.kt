package app.editors.manager.ui.fragments.onboarding;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.pager.PagingViewPager;
import app.editors.manager.ui.views.pager.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lib.toolkit.base.managers.utils.SwipeEventUtils;

public class OnBoardingPagerFragment extends BaseAppFragment {

    public static final String TAG = OnBoardingPagerFragment.class.getSimpleName();

    private Unbinder mUnbinder;
    @BindView(R.id.on_boarding_view_pager)
    protected PagingViewPager mViewpager;
    @BindView(R.id.on_boarding_panel_skip_button)
    protected AppCompatButton mOnBoardingPanelSkipButton;
    @BindView(R.id.on_boarding_panel_indicator)
    protected PageIndicatorView mOnBoardingPanelIndicator;
    @BindView(R.id.on_boarding_panel_next_button)
    protected AppCompatButton mOnBoardingPanelNextButton;

    @Inject
    PreferenceTool mPreferenceTool;

    private OnBoardAdapter mOnBoardAdapter;

    public static OnBoardingPagerFragment newInstance() {
        return new OnBoardingPagerFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
    }

    @Override
    public void dispatchTouchEvent(@NonNull MotionEvent ev) {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_on_boarding_pager, container, false);
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

    @Override
    public boolean onBackPressed() {
        minimizeApp();
        return true;
    }

    @OnClick({R.id.on_boarding_panel_skip_button,
            R.id.on_boarding_panel_next_button})
    protected void onButtonsClick(final View view) {
        switch (view.getId()) {
            case R.id.on_boarding_panel_skip_button:
                mPreferenceTool.setOnBoarding(true);
                finishWithOkCode();
                break;
            case R.id.on_boarding_panel_next_button:
                if (mOnBoardAdapter.isLastPagePosition()) {
                    finishWithOkCode();
                } else {
                    mViewpager.setCurrentItem(mOnBoardAdapter.getSelectedPage() + 1, true);
                }
                break;
        }
    }

    private void finishWithOkCode() {
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    private void init() {
        mOnBoardAdapter = new OnBoardAdapter(getChildFragmentManager(), getFragments());
        mViewpager.setAdapter(mOnBoardAdapter);
        mViewpager.addOnPageChangeListener(mOnBoardAdapter);
        mOnBoardingPanelIndicator.setAnimationType(AnimationType.WORM);
        mOnBoardingPanelIndicator.setViewPager(mViewpager);
    }

    private List<ViewPagerAdapter.Container> getFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(R.string.on_boarding_welcome_header,
                R.string.on_boarding_welcome_info, R.drawable.image_on_boarding_screen1), null));
        pairs.add(new ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(R.string.on_boarding_edit_header,
                R.string.on_boarding_edit_info, R.drawable.image_on_boarding_screen2), null));
        pairs.add(new ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(R.string.on_boarding_access_header,
                R.string.on_boarding_access_info, R.drawable.image_on_boarding_screen3), null));
        pairs.add(new ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(R.string.on_boarding_collaborate_header,
                R.string.on_boarding_collaborate_info, R.drawable.image_on_boarding_screen4), null));
        pairs.add(new ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(R.string.on_boarding_third_party_header,
                R.string.on_boarding_third_party_info, R.drawable.image_on_boarding_screen5), null));
        return pairs;
    }

    /*
     * Pager adapter
     * */
    private class OnBoardAdapter extends ViewPagerAdapter {

        private int mPosition;

        public OnBoardAdapter(FragmentManager manager, List<ViewPagerAdapter.Container> fragmentList) {
            super(manager, fragmentList);
            SwipeEventUtils.detectLeft(mViewpager, () -> {
                if (mPosition == mOnBoardAdapter.getCount() - 1) {
                    mOnBoardingPanelNextButton.callOnClick();
                }
            });
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            mPosition = position;
            if (position == mOnBoardAdapter.getCount() - 1) {
                mOnBoardingPanelNextButton.setText(R.string.on_boarding_finish_button);
                mOnBoardingPanelSkipButton.setVisibility(View.INVISIBLE);
                mPreferenceTool.setOnBoarding(true);
            } else {
                mOnBoardingPanelSkipButton.setVisibility(View.VISIBLE);
                mOnBoardingPanelNextButton.setText(R.string.on_boarding_next_button);
            }
        }
    }

}
