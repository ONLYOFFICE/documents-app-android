package app.editors.manager.ui.fragments.onboarding

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import app.editors.manager.databinding.FragmentOnBoardingPageBinding
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.ui.fragments.base.BaseAppFragment

class OnBoardingPageFragment : BaseAppFragment() {

    private var viewBinding: FragmentOnBoardingPageBinding? = null

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

    @SuppressLint("SetTextI18n")
    private fun init() {
        viewBinding?.let {
            try {
                it.onBoardingPageHeader.setText(arguments?.getInt(TAG_HEADER) ?: -1)
                it.onBoardingPageInfo.setText(arguments?.getInt(TAG_INFO) ?: -1)
                it.onBoardingPageImage.setImageDrawable(AppCompatResources.getDrawable(requireContext(), arguments?.getInt(TAG_IMAGE) ?: -1))
            } catch (error: Resources.NotFoundException) {
                it.onBoardingPageHeader.text = "Not found"
                it.onBoardingPageInfo.text = "Not found"
                it.onBoardingPageImage.setImageDrawable(ColorDrawable(Color.GRAY))
                FirebaseUtils.addCrash(error)
            }
        }
    }

    companion object {
        val TAG: String = OnBoardingPageFragment::class.java.simpleName
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