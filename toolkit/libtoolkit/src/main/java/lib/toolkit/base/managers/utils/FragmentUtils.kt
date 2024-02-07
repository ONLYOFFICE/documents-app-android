package lib.toolkit.base.managers.utils

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import lib.toolkit.base.R

object FragmentUtils {

    @JvmStatic
    @JvmOverloads
    fun showFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        @IdRes frameId: Int,
        tag: String? = null,
        isAdd: Boolean = false,
        @AnimRes resEnterAnim: Int = R.anim.fragment_fade_in,
        @AnimRes resExitAnim: Int = R.anim.fragment_fade_out,
        @AnimRes resPopEnterAnim: Int = R.anim.fragment_fade_in,
        @AnimRes resPopExitAnim: Int = R.anim.fragment_fade_out
    ) {
        fragmentManager.beginTransaction().apply {
            setCustomAnimations(resEnterAnim, resExitAnim, resPopEnterAnim, resPopExitAnim)

            if (isAdd) {
                add(frameId, fragment, fragment.javaClass.simpleName)
            } else {
                replace(frameId, fragment, fragment.javaClass.simpleName)
            }

            tag?.let {
                addToBackStack(it)
            }

        }.commit()
    }

    @JvmStatic
    @JvmOverloads
    fun showBottomTopAnimFragment(fragmentManager: FragmentManager, fragment: Fragment, @IdRes frameId: Int, tag: String? = null) {
        showFragment(
            fragmentManager,
            fragment,
            frameId,
            tag,
            false,
            R.anim.fragment_translate_bottom_top_medium,
            R.anim.fragment_fade_out,
            R.anim.fragment_translate_bottom_top_medium,
            R.anim.fragment_fade_out
        )
    }

    @JvmStatic
    fun showSingleFragment(fragmentManager: FragmentManager, fragment: Fragment, @IdRes frameId: Int, tag: String) {
        if (!checkOnSingle(fragmentManager, fragment)) {
            showFragment(
                fragmentManager,
                fragment,
                frameId,
                tag,
                false
            )
        }
    }

    @JvmStatic
    fun checkOnSingle(fragmentManager: FragmentManager, fragment: Fragment): Boolean {
        fragmentManager.findFragmentByTag(fragment.javaClass.simpleName)?.let { fragment ->
            if (fragment.isVisible) {
                return true
            } else {
                fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }

        return false
    }

    @JvmStatic
    fun backToRoot(fragmentManager: FragmentManager) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

}

fun Fragment.setFragmentResultListener(block: (args: Bundle?) -> Unit) {
    parentFragmentManager.setFragmentResultListener("result", this) { _, bundle ->
        block(bundle)
    }
}

fun <T : Fragment> T.putArgs(vararg pairs: Pair<String, Any?>): T {
    if (arguments != null) {
        arguments?.putAll(bundleOf(*pairs))
    } else {
        arguments = bundleOf(*pairs)
    }
    return this
}
