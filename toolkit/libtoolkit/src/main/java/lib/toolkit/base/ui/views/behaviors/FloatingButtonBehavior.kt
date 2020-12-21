package lib.toolkit.base.ui.views.behaviors


import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FloatingButtonBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {

    /*
    * TODO fixed behavior when button transition with snackbar
    * */
    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        if (dyConsumed > 0) {
            val layoutParams = child.getLayoutParams() as CoordinatorLayout.LayoutParams
            val fabBottomMargin = layoutParams.bottomMargin
//            child.animate().translationY(child.getHeight() + fabBottomMargin).setInterpolator(new LinearInterpolator()).start();
            child.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                @SuppressLint("RestrictedApi")
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    fab.visibility = View.INVISIBLE
                }
            })
        } else if (dyConsumed < 0) {
//            child.animate().translationY(0.0f).setInterpolator(new LinearInterpolator()).start();
            child.show()
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL && type == ViewCompat.TYPE_TOUCH
    }

}
