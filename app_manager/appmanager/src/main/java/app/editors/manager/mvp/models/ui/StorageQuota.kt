package app.editors.manager.mvp.models.ui

import android.content.Context

data class StorageQuota(
    val value: Long = 0,
    val unit: SizeUnit = SizeUnit.MB,
    val enabled: Boolean = false,

    @Transient
    val visible: Boolean = false
) {

    val bytes: Long
        get() {
            return when (unit) {
                SizeUnit.Bytes -> value
                SizeUnit.KB -> value * 1024
                SizeUnit.MB -> value * 1024 * 1024
                SizeUnit.GB -> value * 1024 * 1024 * 1024
                SizeUnit.TB -> value * 1024 * 1024 * 1024 * 1024
            }
        }

    fun toString(context: Context): String {
        return "$value ${context.getString(unit.title)}"
    }

    companion object {

        fun fromBytes(bytes: Long?): StorageQuota {
            if (bytes == null) return StorageQuota(visible = false)
            if (bytes == -1L) return StorageQuota(visible = true)

            val kb = 1024.0
            val b = bytes.toDouble()
            val k = bytes / kb
            val m = k / kb
            val g = m / kb
            val t = g / kb

            return when {
                t > 1 -> StorageQuota(unit = SizeUnit.TB, value = t.toLong())
                g > 1 -> StorageQuota(unit = SizeUnit.GB, value = g.toLong())
                m > 1 -> StorageQuota(unit = SizeUnit.MB, value = m.toLong())
                k > 1 -> StorageQuota(unit = SizeUnit.KB, value = k.toLong())
                else -> StorageQuota(unit = SizeUnit.Bytes, value = b.toLong())
            }.copy(enabled = true, visible = true)
        }
    }
}