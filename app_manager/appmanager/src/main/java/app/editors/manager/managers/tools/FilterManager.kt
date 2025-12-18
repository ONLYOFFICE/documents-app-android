package app.editors.manager.managers.tools

import app.editors.manager.mvp.models.filter.Filter

class FilterManager {

    private var filterBySection: Map<Int, Filter> = mapOf()

    fun saveFilter(section: Int?, block: (Filter) -> Filter) {
        val filter = block(filterBySection[section] ?: Filter())
        filterBySection = filterBySection
            .toMutableMap()
            .apply { put(section ?: return, filter) }
    }

    fun getFilter(section: Int?): Filter {
        return filterBySection[section]?.takeUnless { section == null } ?: Filter()
    }

    fun clearFilter(section: Int?) {
        filterBySection = filterBySection
            .toMutableMap()
            .apply { remove(section ?: return) }
    }
}