package lib.toolkit.base.ui.views.pager


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

class ViewPagerAdapter(protected val mManager: FragmentManager) : FragmentPagerAdapter(mManager), ViewPager.OnPageChangeListener {

    protected val mFragmentList: MutableList<Container>

    var selectedPage: Int = 0
        protected set

    val isLastPagePosition: Boolean
        get() = selectedPage == count - 1

    constructor(manager: FragmentManager, fragmentList: List<Container>) : this(manager) {
        mFragmentList.addAll(fragmentList)
    }

    init {
        mFragmentList = ArrayList()
    }

    /*
    * Adapter events
    * */
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position].mFragment
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentList[position].mTitle
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(
            Container(
                fragment,
                title
            )
        )
        notifyDataSetChanged()
    }

    fun removeFragment(position: Int) {
        mManager.beginTransaction().remove(mFragmentList[position].mFragment).commit()
        mFragmentList.removeAt(position)
        notifyDataSetChanged()
    }

    /*
    * Page change events
    * */
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        selectedPage = position
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    /*
    * Helper methods
    * */
    fun getActiveFragment(viewPager: ViewPager): Fragment {
        return instantiateItem(viewPager, selectedPage) as Fragment
    }

    fun isActiveFragment(viewPager: ViewPager, fragment: Fragment): Boolean {
        return fragment == getActiveFragment(viewPager)
    }

    /*
    * Data container for fragments
    * */
    class Container(val mFragment: Fragment, val mTitle: String)

}
