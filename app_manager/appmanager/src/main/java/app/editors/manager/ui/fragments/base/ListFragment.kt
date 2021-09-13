package app.editors.manager.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.R
import app.editors.manager.databinding.FragmentListBinding
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.recyclers.LoadingScroll
import lib.toolkit.base.managers.tools.ResourcesProvider

abstract class ListFragment : BaseAppFragment(), SwipeRefreshLayout.OnRefreshListener {

    protected var linearLayoutManager: LinearLayoutManager? = null
    protected var placeholderViews: PlaceholderViews? = null
    protected var recyclerView: RecyclerView? = null
    protected var swipeRefreshLayout: SwipeRefreshLayout? = null
    protected var fragmentListBinding: FragmentListBinding? = null

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
        fragmentListBinding?.let {
            val resourcesProvider = ResourcesProvider(requireContext())
            placeholderViews = PlaceholderViews(it.placeholderLayout.root)
            placeholderViews?.setViewForHide(it.listOfItems)
            it.listOfItems.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context, R.anim.recycler_view_animation_layout
            )
            linearLayoutManager = LinearLayoutManager(context)
            it.listSwipeRefresh.setProgressBackgroundColorSchemeColor(resourcesProvider
                .getColor(R.color.colorTransparent)
            )
            it.listSwipeRefresh.setColorSchemeColors(resourcesProvider
                .getColor(R.color.colorSecondary))
            it.listSwipeRefresh.setOnRefreshListener(this)
            it.listOfItems.layoutManager = linearLayoutManager
            it.listOfItems.addOnScrollListener(object : LoadingScroll() {
                override fun onListEnd() {
                    this@ListFragment.onListEnd()
                }
            })
            recyclerView = it.listOfItems
            swipeRefreshLayout = it.listSwipeRefresh
        }
    }

    protected open fun onListEnd() {}

    companion object {
        val TAG = ListFragment::class.java.simpleName
    }
}