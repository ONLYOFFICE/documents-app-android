package app.editors.manager.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.editors.manager.databinding.FragmentOnBoardingPageBinding
import app.editors.manager.ui.fragments.base.BaseAppFragment

class OnBoardingPageFragment : BaseAppFragment() {

    @StringRes
    private var mHeaderResId = 0

    @StringRes
    private var mInfoResId = 0

    @DrawableRes
    private var mImageResId = 0

    private var viewBinding: FragmentOnBoardingPageBinding? = null

    override fun dispatchTouchEvent(ev: MotionEvent) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentOnBoardingPageBinding.inflate(inflater, container, false)
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

    private fun init() {
        getArgs()
        viewBinding?.let {
            it.onBoardingPageHeader.setText(mHeaderResId)
            it.onBoardingPageInfo.setText(mInfoResId)
            it.onBoardingPageImage.setImageResource(mImageResId)
        }
    }

    private fun getArgs() {
        arguments?.let { bundle ->
            mHeaderResId = bundle.getInt(TAG_HEADER)
            mInfoResId = bundle.getInt(TAG_INFO)
            mImageResId = bundle.getInt(TAG_IMAGE)
        }
    }

    companion object {
        val TAG = OnBoardingPageFragment::class.java.simpleName
        const val TAG_HEADER = "TAG_HEADER"
        const val TAG_INFO = "TAG_INFO"
        const val TAG_IMAGE = "TAG_IMAGE"

        fun newInstance(@StringRes headerId: Int, @StringRes infoId: Int, @DrawableRes imageId: Int):
                OnBoardingPageFragment = OnBoardingPageFragment().apply {
                arguments = Bundle(3).apply {
                    putInt(TAG_HEADER, headerId)
                    putInt(TAG_INFO, infoId)
                    putInt(TAG_IMAGE, imageId)
                }
            }
        }
}