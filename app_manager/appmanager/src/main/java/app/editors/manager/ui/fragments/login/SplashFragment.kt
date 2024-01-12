package app.editors.manager.ui.fragments.login

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import app.editors.manager.app.appComponent
import app.editors.manager.databinding.FragmentSplashBinding
import app.editors.manager.managers.tools.PreferenceTool
import app.editors.manager.managers.utils.FirebaseUtils
import app.editors.manager.ui.activities.main.MainActivity
import app.editors.manager.ui.activities.main.PasscodeActivity
import app.editors.manager.ui.fragments.base.BaseAppFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashFragment : BaseAppFragment() {


    companion object {
        val TAG: String = SplashFragment::class.java.simpleName

        private const val DELAY_SPLASH = 1000

        fun newInstance(): SplashFragment {
            return SplashFragment()
        }
    }

    private var viewBinding: FragmentSplashBinding? = null

    private var disposable: Disposable? = null

    @Inject
    lateinit var preferencesTool: PreferenceTool

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireContext().appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSplashBinding.inflate(inflater)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
        viewBinding = null
    }

    private fun init() {
        try {
            val drawable =
                ContextCompat.getDrawable(requireContext(), lib.toolkit.base.R.drawable.image_onlyoffice_text)
            viewBinding?.appCompatImageView?.setImageDrawable(drawable)
        } catch (e: InflateException) {
            FirebaseUtils.addCrash("Inflate error when start")
            e.printStackTrace()
        } catch (e: Resources.NotFoundException) {
            FirebaseUtils.addCrash("Inflate error when start")
            e.printStackTrace()
        }
        disposable = Observable.just(1).delay(DELAY_SPLASH.toLong(), TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(!preferencesTool.isPasscodeLockEnable) {
                    MainActivity.show(requireContext())
                    requireActivity().finish()
                } else {
                    PasscodeActivity.show(requireContext())
                }
            }
    }
}