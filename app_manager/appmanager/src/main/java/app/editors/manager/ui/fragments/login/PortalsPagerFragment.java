package app.editors.manager.ui.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.editors.manager.R;
import app.editors.manager.ui.activities.login.PortalsActivity;
import app.editors.manager.ui.fragments.base.BaseAppFragment;
import app.editors.manager.ui.views.pager.PagingViewPager;
import app.editors.manager.ui.views.pager.ViewPagerAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PortalsPagerFragment extends BaseAppFragment {

    public static final String TAG = PortalsPagerFragment.class.getSimpleName();

    @BindView(R.id.on_boarding_view_pager)
    protected PagingViewPager mViewpager;

    private PortalsActivity mPortalsActivity;
    private Unbinder mUnbinder;

    public static PortalsPagerFragment newInstance() {
        return new PortalsPagerFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mPortalsActivity = (PortalsActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(PortalsPagerFragment.class.getSimpleName() + " - must implement - " +
                    PortalsActivity.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login_pager, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void init(final Bundle savedInstanceState) {
        mPortalsActivity.getTabLayout().setupWithViewPager(mViewpager, true);
        mViewpager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getFragments()));
    }

    private List<ViewPagerAdapter.Container> getFragments() {
        final List<ViewPagerAdapter.Container> pairs = new ArrayList<>();
        pairs.add(new ViewPagerAdapter.Container(EnterprisePortalFragment.newInstance(),
                getString(R.string.login_pager_enterprise)));
        pairs.add(new ViewPagerAdapter.Container(PersonalPortalFragment.newInstance(),
                getString(R.string.login_pager_personal)));
        return pairs;
    }

}
