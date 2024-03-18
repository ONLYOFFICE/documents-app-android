package app.editors.manager.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.editors.manager.databinding.FragmentListBinding
import app.editors.manager.ui.views.custom.PlaceholderViews
import app.editors.manager.ui.views.recyclers.LoadingScroll
import lib.toolkit.base.ui.recycler.WrapLinearLayoutManager

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
        fragmentListBinding?.let { binding ->
            placeholderViews = PlaceholderViews(binding.placeholderLayout.root)
            placeholderViews?.setViewForHide(binding.listOfItems)
            recyclerView = binding.listOfItems.apply {
                layoutManager = WrapLinearLayoutManager(requireContext()).also { linearLayoutManager = it }
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

    companion object {
        val TAG: String = ListFragment::class.java.simpleName
    }
}