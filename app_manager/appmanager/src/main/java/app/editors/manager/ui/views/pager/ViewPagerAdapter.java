package app.editors.manager.ui.views.pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

    protected final FragmentManager mManager;
    protected final List<Container> mFragmentList;
    protected int mSelectedPage;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mManager = manager;
        mFragmentList = new ArrayList<>();
    }

    public ViewPagerAdapter(FragmentManager manager, List<Container> fragmentList) {
        this(manager);
        mFragmentList.addAll(fragmentList);
    }

    /*
     * Adapter events
     * */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position).mFragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).mTitle;
    }

    @Override
    public int getItemPosition(@NonNull Object item) {
        return POSITION_NONE;
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(new Container(fragment, title));
        notifyDataSetChanged();
    }

    public void addFragment(Fragment fragment, String title, int position) {
        mFragmentList.add(position, new Container(fragment, title));
        notifyDataSetChanged();
    }

    public void removeFragment(Fragment fragment) {
        mManager.beginTransaction().remove(fragment).commit();
        mFragmentList.remove(new Container(fragment, ""));
        notifyDataSetChanged();
    }

    public void removeFragment(int position) {
        mManager.beginTransaction().remove(mFragmentList.get(position).mFragment).commit();
        mFragmentList.remove(position);
        notifyDataSetChanged();
    }

    /*
     * Page change events
     * */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSelectedPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*
     * Helper methods
     * */
    public Fragment getActiveFragment(final ViewPager viewPager) {
        return (Fragment) instantiateItem(viewPager, mSelectedPage);
    }

    public boolean isActiveFragment(final Fragment fragment) {
        if (fragment == null || mSelectedPage < 0 || mSelectedPage >= mFragmentList.size()) {
            return false;
        }
        return fragment.equals(mFragmentList.get(mSelectedPage).mFragment);
    }

    public boolean isLastPagePosition() {
        return mSelectedPage == getCount() - 1;
    }

    public int getSelectedPage() {
        return mSelectedPage;
    }

    public void setSelectedPage(int page) {
        mSelectedPage = page;
    }

    public int getByTitle(String string) {
        for (Container container : mFragmentList) {
            if (container.mTitle.equals(string)) {
                return mFragmentList.indexOf(container);
            }
        }
        return 0;
    }

    /*
     * Data container for fragments
     * */
    public static class Container {

        public final Fragment mFragment;
        public final String mTitle;

        public Container(Fragment fragment, String title) {
            mFragment = fragment;
            mTitle = title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Container container = (Container) o;
            return Objects.equals(mFragment, container.mFragment);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mFragment);
        }
    }

}
