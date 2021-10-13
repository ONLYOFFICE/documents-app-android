package app.editors.manager.ui.fragments.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentOnBoardingPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.isVisible
import app.editors.manager.ui.activities.login.AuthAppActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import com.rd.animation.type.AnimationType
import lib.toolkit.base.managers.tools.ResourcesProvider
import javax.inject.Inject

class AuthPagerFragment : BaseAppFragment() {

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var authAdapter: AuthAdapter? = null
    private var sqlData: String? = null
    private var viewBinding: FragmentOnBoardingPagerBinding? = null
    private val resourcesProvider = ResourcesProvider(requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(AuthAppActivity.REQUEST_KEY)) {
                sqlData = it.getString(AuthAppActivity.REQUEST_KEY)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentOnBoardingPagerBinding.inflate(layoutInflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    override fun onBackPressed(): Boolean {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .remove(this)
            .commit()
        return false
    }

    private fun initListeners() {
        viewBinding?.let { binding ->
            binding.include.onBoardingPanelSkipButton.setOnClickListener {
                preferenceTool.onBoarding = true
                finishWithOkCode()
            }
            binding.include.onBoardingPanelNextButton.setOnClickListener {
                if (authAdapter?.isLastPagePosition!!) {
                    finishWithOkCode()
                } else {
                    binding.onBoardingViewPager
                        .setCurrentItem(authAdapter?.selectedPage!! + 1, true)
                }
            }
        }
    }

    private fun finishWithOkCode() {
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
    }

    private fun init() {
        initColor()
        initListeners()
        viewBinding?.let {
            authAdapter = AuthAdapter(childFragmentManager, fragments).apply {
                it.onBoardingViewPager.addOnPageChangeListener(this)
            }
            it.onBoardingViewPager.adapter = authAdapter
            it.include.onBoardingPanelIndicator.setAnimationType(AnimationType.WORM)
            it.include.onBoardingPanelIndicator.setViewPager(it.onBoardingViewPager)
            it.include.onBoardingPanelSkipButton.isVisible = false
        }
    }

    private fun initColor() {
        viewBinding?.let {
            it.pagerFragmentLayout.background = resourcesProvider.getDrawable(lib.toolkit.base.R.color.colorWhite)
            it.include.pageIndicatorLayout.background =
                resourcesProvider.getDrawable(lib.toolkit.base.R.color.colorWhite)
            it.include.onBoardingPanelNextButton
                .setTextColor(resourcesProvider.getColor(lib.toolkit.base.R.color.colorSecondary))
            it.include.onBoardingPanelSkipButton
                .setTextColor(resourcesProvider.getColor(lib.toolkit.base.R.color.colorSecondary))
            it.include.onBoardingPanelIndicator.selectedColor =
                resourcesProvider.getColor(lib.toolkit.base.R.color.colorSecondary)
            it.include.onBoardingPanelIndicator.unselectedColor =
                resourcesProvider.getColor(lib.toolkit.base.R.color.colorGrey)
        }
    }

    private val fragments: List<ViewPagerAdapter.Container?>
        get() {
            return listOf(
                ViewPagerAdapter.Container(getInstance(KEY_FIRST_FRAGMENT), null),
                ViewPagerAdapter.Container(getInstance(KEY_SECOND_FRAGMENT), null),
                ViewPagerAdapter.Container(getInstance(KEY_THIRD_FRAGMENT), null),
                ViewPagerAdapter.Container(getInstance(KEY_FOURTH_FRAGMENT), null),
            )
        }

    private fun getInstance(fragment: Int): AuthPageFragment {
        sqlData?.let { data ->
            arguments?.let { args ->
                return AuthPageFragment
                    .newInstance(fragment, data, args.getString(AuthAppActivity.TFA_KEY) ?: "")
            }
        }
        return AuthPageFragment()
    }

    /*
     * Pager adapter
     * */
    private inner class AuthAdapter(manager: FragmentManager?, fragmentList: List<Container?>?) :
        ViewPagerAdapter(manager, fragmentList) {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewBinding?.let {
                if (position == authAdapter?.count!! - 1) {
                    it.include.onBoardingPanelNextButton.isVisible = false
                    it.include.onBoardingPanelSkipButton.isVisible = false
                } else {
                    it.include.onBoardingPanelNextButton.isVisible = true
                    it.include.onBoardingPanelSkipButton.isVisible = false
                    it.include.onBoardingPanelNextButton.setText(R.string.on_boarding_next_button)
                }
                (authAdapter?.getActiveFragment(it.onBoardingViewPager) as AuthPageFragment)
                    .onPageSelected(position)
            }
        }
    }

    companion object {
        val TAG = AuthPagerFragment::class.java.simpleName
        const val KEY_FIRST_FRAGMENT = 0
        const val KEY_SECOND_FRAGMENT = 1
        const val KEY_THIRD_FRAGMENT = 2
        const val KEY_FOURTH_FRAGMENT = 3

        fun newInstance(request: String?, key: String?): AuthPagerFragment =
            AuthPagerFragment().apply {
                arguments = Bundle(2).apply {
                    putString(AuthAppActivity.REQUEST_KEY, request)
                    putString(AuthAppActivity.TFA_KEY, key)
                }
            }
    }
}