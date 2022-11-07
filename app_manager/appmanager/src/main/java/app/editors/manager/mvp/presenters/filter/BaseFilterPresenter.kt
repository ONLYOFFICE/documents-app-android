
package app.editors.manager.mvp.presenters.filter
import app.editors.manager.mvp.presenters.base.BasePresenter
import app.editors.manager.mvp.views.filter.FilterView
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFilterPresenter : BasePresenter<FilterView>() {

    protected var disposable: CompositeDisposable? = CompositeDisposable()

    abstract val hasFilter: Boolean

    abstract fun saveFilter()

    abstract fun loadFilter()

    abstract fun update(initialCall: Boolean = false)

    abstract fun reset()

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

}