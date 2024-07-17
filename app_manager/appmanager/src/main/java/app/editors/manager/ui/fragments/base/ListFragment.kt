package app.editors.manager.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.databinding.FragmentListBinding
import app.editors.manager.ui.adapters.holders.explorer.RecentViaLinkViewHolder
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.recyclers.LoadingScroll
import lib.toolkit.base.ui.recycler.WrapLinearLayoutManager


abstract class ListFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener {

    protected var placeholderViews: PlaceholderViews? = null
    protected var recyclerView: RecyclerView? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null
    protected var fragmentListBinding: FragmentListBinding? = null
    protected var isGridView: Boolean = false
    protected var layoutManager: LayoutManager? = null

    private val layoutAnimation by lazy {
        AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_grid_view_animation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentListBinding = FragmentListBinding.inflate(inflater, container, false)
        return fragmentListBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentListBinding = null
    }

    private fun init() {
        fragmentListBinding?.let { binding ->
            placeholderViews = PlaceholderViews(binding.placeholderLayout.root)
            placeholderViews?.setViewForHide(binding.listOfItems)
            recyclerView = binding.listOfItems.apply {
                layoutManager = if (isGridView) {
                    GridLayoutManager(requireContext(), getSpanCount()).also { it.spanSizeLookup = getSpanSizeLookup() }
                } else {
                    WrapLinearLayoutManager(requireContext())
                }.also { this@ListFragment.layoutManager = it }

                addOnScrollListener(object : LoadingScroll() {
                    override fun onListEnd() = this@ListFragment.onListEnd()
                })
            }
            swipeRefreshLayout = binding.listSwipeRefresh.apply {
                setColorSchemeResources(lib.toolkit.base.R.color.colorSecondary)
                setProgressBackgroundColorSchemeResource(lib.toolkit.base.R.color.colorSurface)
                setOnRefreshListener(this@ListFragment)
            }
        }
    }

    protected open fun onListEnd() {}

    protected open fun switchGridView(isGrid: Boolean) {
        recyclerView?.let { view ->
            setLayoutAnimation(view)
            if (isGrid) {
                val spanCount = getSpanCount()
                val layoutManager = GridLayoutManager(requireContext(), spanCount)
                layoutManager.spanSizeLookup = getSpanSizeLookup()
                view.layoutManager = layoutManager
            } else {
                view.layoutManager = WrapLinearLayoutManager(requireContext())
            }
        }
    }

    private fun setLayoutAnimation(recyclerView: RecyclerView) {
        recyclerView.setLayoutAnimation(layoutAnimation)
        recyclerView.scheduleLayoutAnimation()
        recyclerView.layoutAnimationListener = object : AnimationListener {

            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                recyclerView.layoutAnimation = null
                recyclerView.layoutAnimationListener = null
            }
        }
    }

    private fun getSpanCount(): Int = if (isLandscape) 5 else 3

    private fun getSpanSizeLookup(): GridLayoutManager.SpanSizeLookup =
        object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (recyclerView?.adapter?.getItemViewType(position) == RecentViaLinkViewHolder.LAYOUT) {
                    getSpanCount()
                } else {
                    1
                }
            }
        }

    companion object {
        val TAG: String = ListFragment::class.java.simpleName
    }
}