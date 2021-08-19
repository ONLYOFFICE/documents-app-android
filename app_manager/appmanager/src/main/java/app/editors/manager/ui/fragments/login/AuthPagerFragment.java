package app.editors.manager.ui.fragments.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import app.editors.manager.R;
import app.editors.manager.app.App;
import app.editors.manager.managers.tools.PreferenceTool;
import app.editors.manager.ui.activities.login.AuthAppActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.pager.PagingViewPager;
import app.editors.manager.ui.views.pager.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AuthPagerFragment extends BaseAppFragment {

    public static final String TAG = AuthPagerFragment.class.getSimpleName();

    public static final int KEY_FIRST_FRAGMENT = 0;
    public static final int KEY_SECOND_FRAGMENT = 1;
    public static final int KEY_THIRD_FRAGMENT = 2;
    public static final int KEY_FOURTH_FRAGMENT = 3;

    private Unbinder mUnbinder;
    @BindView(R.id.on_boarding_view_pager)
    protected PagingViewPager mViewpager;
    @BindView(R.id.pager_fragment_layout)
    protected ConstraintLayout mFragmentLayout;
    @BindView(R.id.include)
    protected ConstraintLayout mPageIndicatorLayout;
    @BindView(R.id.on_boarding_panel_skip_button)
    protected AppCompatButton mAuthPanelSkipButton;
    @BindView(R.id.on_boarding_panel_indicator)
    protected PageIndicatorView mAuthPanelIndicator;
    @BindView(R.id.on_boarding_panel_next_button)
    protected AppCompatButton mAuthPanelNextButton;

    @Inject
    PreferenceTool mPreferenceTool;

    private AuthAdapter mAuthAdapter;
    private String mSqlData;

    public static AuthPagerFragment newInstance(String request, String key) {
        Bundle bundle = new Bundle();
        bundle.putString(AuthAppActivity.REQUEST_KEY, request);
        bundle.putString(AuthAppActivity.TFA_KEY, key);
        AuthPagerFragment fragment = new AuthPagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(AuthAppActivity.REQUEST_KEY)) {
                mSqlData = getArguments().getString(AuthAppActivity.REQUEST_KEY);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        App.getApp().getAppComponent().inject(this);
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
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
        return false;
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
                if (mAuthAdapter.isLastPagePosition()) {
                    finishWithOkCode();
                } else {
                    mViewpager.setCurrentItem(mAuthAdapter.getSelectedPage() + 1, true);
                }
                break;
        }
    }

    private void finishWithOkCode() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void init() {
        initColor();
        mAuthAdapter = new AuthAdapter(getChildFragmentManager(), getFragments());
        mViewpager.setAdapter(mAuthAdapter);
        mViewpager.addOnPageChangeListener(mAuthAdapter);

        mAuthPanelIndicator.setAnimationType(AnimationType.WORM);
        mAuthPanelIndicator.setViewPager(mViewpager);
        mAuthPanelSkipButton.setVisibility(View.INVISIBLE);
    }

    private void initColor() {
        if (getContext() != null) {
            mFragmentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorWhite));
            mPageIndicatorLayout.setBackground(ContextCompat.getDrawable(getContext(), R.color.colorWhite));

            mAuthPanelNextButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondary));
            mAuthPanelSkipButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondary));

            mAuthPanelIndicator.setSelectedColor(ContextCompat.getColor(getContext(), R.color.colorSecondary));
            mAuthPanelIndicator.setUnselectedColor(ContextCompat.getColor(getContext(), R.color.colorGrey));
        }
    }

    private List<ViewPagerAdapter.Container> getFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(AuthPageFragment.newInstance(KEY_FIRST_FRAGMENT, mSqlData, getArguments().getString(AuthAppActivity.TFA_KEY)), null));
        pairs.add(new ViewPagerAdapter.Container(AuthPageFragment.newInstance(KEY_SECOND_FRAGMENT, mSqlData, getArguments().getString(AuthAppActivity.TFA_KEY)), null));
        pairs.add(new ViewPagerAdapter.Container(AuthPageFragment.newInstance(KEY_THIRD_FRAGMENT, mSqlData, getArguments().getString(AuthAppActivity.TFA_KEY)), null));
        pairs.add(new ViewPagerAdapter.Container(AuthPageFragment.newInstance(KEY_FOURTH_FRAGMENT, mSqlData, getArguments().getString(AuthAppActivity.TFA_KEY)), null));
        return pairs;
    }

    /*
     * Pager adapter
     * */
    private class AuthAdapter extends ViewPagerAdapter {

        AuthAdapter(FragmentManager manager, List<ViewPagerAdapter.Container> fragmentList) {
            super(manager, fragmentList);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == mAuthAdapter.getCount() - 1) {
                mAuthPanelNextButton.setVisibility(View.INVISIBLE);
                mAuthPanelSkipButton.setVisibility(View.INVISIBLE);
            } else {
                mAuthPanelSkipButton.setVisibility(View.INVISIBLE);
                mAuthPanelNextButton.setVisibility(View.VISIBLE);
                mAuthPanelNextButton.setText(R.string.on_boarding_next_button);
            }
            ((AuthPageFragment) mAuthAdapter.getActiveFragment(mViewpager)).onPageSelected(position);
        }
    }

}