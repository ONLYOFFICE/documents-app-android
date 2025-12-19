package app.editors.manager.mvp.models.ui

enum class SharingType(val shared: Boolean, val forUser: Boolean) {
    NONE(false, false),
    LINKS(true, false),
    USERS(false, true),
    LINKS_AND_USERS(true, true);

    fun toFlags() = shared to forUser

    companion object {
        fun fromFlags(hasLinks: Boolean, hasUsers: Boolean) = when {
            hasLinks && hasUsers -> LINKS_AND_USERS
            hasLinks -> LINKS
            hasUsers -> USERS
            else -> NONE
        }
    }
}