package app.editors.manager.ui.adapters.holders

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.editors.manager.ui.adapters.ExplorerAdapter


abstract class BaseViewHolderExplorer<T>(itemView: View, adapter: ExplorerAdapter) :
    RecyclerView.ViewHolder(itemView) {

    @JvmField
    protected var adapter: ExplorerAdapter = adapter

    protected val icon: Bitmap
        get() {
            val view = getCachedIcon() ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            return try {
                val bitmap = Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                bitmap
            } catch (_: Exception) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }

    abstract fun bind(element: T)

    abstract fun getCachedIcon(): View?

    companion object {
        const val PLACEHOLDER_POINT = " • "
    }
}