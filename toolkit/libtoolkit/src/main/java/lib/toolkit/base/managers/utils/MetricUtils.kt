package lib.toolkit.base.managers.utils

import kotlin.math.round

object MetricUtils {

    @JvmStatic
    fun ptToMm(value: Double): Double {
        return value * 25.4 / 72
    }

    @JvmStatic
    fun mmToPt(value: Double) : Double {
        return value * 72 / 25.4
    }

    @JvmStatic
    fun mmToCm(value: Double) : Double {
        return value / 10
    }

    @JvmStatic
    fun cmToMm(value: Double) : Double {
        return value * 10
    }

    @JvmStatic
    fun roundToQuart(double: Double?): Double {
        double?.let {
            return round(it * 4) / 4.0
        }
        return 0.0
    }

}