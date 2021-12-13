package app.editors.manager.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import app.documents.core.account.Recent
import app.editors.manager.R
import app.editors.manager.mvp.models.list.Header
import app.editors.manager.mvp.models.ui.toRecentUI
import app.editors.manager.ui.adapters.base.BaseViewTypeAdapter
import app.editors.manager.ui.adapters.diffutilscallback.RecentDiffUtilsCallback
import app.editors.manager.ui.adapters.holders.factory.RecentHolderFactory
import lib.toolkit.base.managers.utils.TimeUtils
import lib.toolkit.base.ui.adapters.holder.ViewType

class RecentAdapter(private val context: Context, factory: RecentHolderFactory) : BaseViewTypeAdapter<ViewType>(factory) {

    fun setRecent(list: List<Recent>) {
        val diffUtils = RecentDiffUtilsCallback(getListWithHeaders(list), itemsList)
        val result = DiffUtil.calculateDiff(diffUtils)
        super.set(getListWithHeaders(list), result)
    }

    private fun getListWithHeaders(list: List<Recent>): MutableList<ViewType> {
        var isTodayHeader = false
        var isYesterdayHeader = false
        var isWeekHeader = false
        var isMonthHeader = false
        var isYearHeader = false
        var isMoreYearHeader = false

        val itemsList = mutableListOf<ViewType>()

        for (item in list.sortedByDescending { it.date }) {
            val date = item.date

            when {
                date >= TimeUtils.todayMs && !isTodayHeader -> {
                    isTodayHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_today)))
                }
                date in (TimeUtils.yesterdayMs + 1) until TimeUtils.todayMs && !isYesterdayHeader -> {
                    isYesterdayHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_yesterday)))
                }
                date in (TimeUtils.weekMs + 1) until TimeUtils.yesterdayMs && !isWeekHeader -> {
                    isWeekHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_week)))
                }
                date in (TimeUtils.monthMs + 1) until TimeUtils.weekMs && !isMonthHeader -> {
                    isMonthHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_month)))
                }
                date in (TimeUtils.yearMs + 1) until TimeUtils.monthMs && !isYearHeader -> {
                    isYearHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_year)))
                }
                date < TimeUtils.yearMs && !isMoreYearHeader -> {
                    isMoreYearHeader = true
                    itemsList.add(Header(context.getString(R.string.list_headers_more_year)))
                }
            }

            itemsList.add(item.toRecentUI())
        }
        return itemsList
    }
}