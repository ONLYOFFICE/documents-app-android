package app.editors.manager.mvp.models.ui

sealed class SizeUnit(val title: Int) {

    data object Bytes : SizeUnit(lib.toolkit.base.R.string.sizes_bytes)
    data object KB : SizeUnit(lib.toolkit.base.R.string.sizes_kilobytes)
    data object MB : SizeUnit(lib.toolkit.base.R.string.sizes_megabytes)
    data object GB : SizeUnit(lib.toolkit.base.R.string.sizes_gigabytes)
    data object TB : SizeUnit(lib.toolkit.base.R.string.sizes_terabytes)

    companion object {

        fun values(): List<SizeUnit> {
            return listOf(Bytes, KB, MB, GB, TB)
        }
    }
}