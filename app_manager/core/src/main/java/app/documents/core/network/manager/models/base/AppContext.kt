package app.documents.core.network.manager.models.base

enum class AppContext(val value: Int) {
    NONE(0), MY(1), SHARE(2), COMMON(3), PROJECTS(4), TRASH(5);

    companion object {
        fun getEnum(value: Int): AppContext {
            for (e in values()) {
                if (e.value == value) return e
            }
            return NONE
        }
    }
}