package app.editors.manager.ui.fragments.onboarding

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.editors.manager.R
import app.editors.manager.app.App
import app.editors.manager.databinding.FragmentOnBoardingPagerBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import app.editors.manager.ui.views.pager.ViewPagerAdapter
import com.rd.animation.type.AnimationType
import lib.toolkit.base.managers.utils.SwipeEventUtils
import javax.inject.Inject

class OnBoardingPagerFragment : BaseAppFragment() {

    @Inject
    lateinit var preferenceTool: PreferenceTool

    private var onBoardAdapter: OnBoardAdapter? = null
    private var viewBinding: FragmentOnBoardingPagerBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.getApp().appComponent.inject(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentOnBoardingPagerBinding.inflate(inflater, container, false)
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
        minimizeApp()
        return true
    }

    private fun finishWithOkCode() {
        requireActivity().setResult(Activity.RESULT_OK)
        requireActivity().finish()
        MainActivity.show(requireContext(), null)
    }

    private fun init() {
        viewBinding?.let { binding ->
            onBoardAdapter = OnBoardAdapter(childFragmentManager, fragments)
            binding.onBoardingViewPager.adapter = onBoardAdapter
            binding.onBoardingViewPager.addOnPageChangeListener(onBoardAdapter!!)
            binding.include.onBoardingPanelIndicator.setAnimationType(AnimationType.WORM)
            binding.include.onBoardingPanelIndicator.setViewPager(binding.onBoardingViewPager)
            binding.include.onBoardingPanelSkipButton.setOnClickListener {
                preferenceTool.onBoarding = true
                finishWithOkCode()
            }
            binding.include.onBoardingPanelNextButton.setOnClickListener {
                if (onBoardAdapter?.isLastPagePosition == true) {
                    finishWithOkCode()
                } else {
                    binding.onBoardingViewPager
                        .setCurrentItem(onBoardAdapter?.selectedPage!! + 1, true)
                }
            }
        }
    }

    private fun getInstance(screen: Int, header: Int, info: Int) =
        ViewPagerAdapter.Container(OnBoardingPageFragment.newInstance(header, info, screen), null)

    private val fragments: List<ViewPagerAdapter.Container?>
        get() = listOf(
            getInstance(R.drawable.image_on_boarding_screen1, R.string.on_boarding_welcome_header,
                R.string.on_boarding_welcome_info),
            getInstance(R.drawable.image_on_boarding_screen2, R.string.on_boarding_edit_header,
            R.string.on_boarding_edit_info),
            getInstance(R.drawable.image_on_boarding_screen3, R.string.on_boarding_access_header,
                R.string.on_boarding_access_info),
            getInstance(R.drawable.image_on_boarding_screen4, R.string.on_boarding_collaborate_header,
                R.string.on_boarding_collaborate_info),
            getInstance(R.drawable.image_on_boarding_screen5, R.string.on_boarding_third_party_header,
                R.string.on_boarding_third_party_info))

    /*
     * Pager adapter
     * */
    private inner class OnBoardAdapter(manager: FragmentManager?, fragmentList: List<Container?>?) :
        ViewPagerAdapter(manager, fragmentList) {
        private var position = 0

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            this.position = position
            viewBinding?.let {
                if (position == onBoardAdapter?.count!! - 1) {
                    it.include.onBoardingPanelNextButton.setText(R.string.on_boarding_finish_button)
                    it.include.onBoardingPanelSkipButton.visibility = View.INVISIBLE
                    preferenceTool.onBoarding = true
                } else {
                    it.include.onBoardingPanelNextButton.setText(R.string.on_boarding_next_button)
                    it.include.onBoardingPanelSkipButton.visibility = View.VISIBLE
                }
            }
        }

        init {
            SwipeEventUtils.detectLeft(viewBinding?.onBoardingViewPager!!,
                object : SwipeEventUtils.SwipeSingleCallback {
                override fun onSwipe() {
                    if (position == onBoardAdapter?.count!! - 1) {
                        viewBinding?.include?.onBoardingPanelNextButton?.callOnClick()
                    }
                }
            })
        }
    }

    companion object {
        val TAG: String = OnBoardingPagerFragment::class.java.simpleName

        fun newInstance(): OnBoardingPagerFragment {
            return OnBoardingPagerFragment()
        }
    }
}