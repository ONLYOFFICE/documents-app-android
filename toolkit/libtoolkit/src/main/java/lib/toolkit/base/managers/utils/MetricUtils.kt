package lib.toolkit.base.managers.utils

import kotlin.math.pow
import kotlin.math.round

object MetricUtils {

    fun mmToPt(value: Double): Double = value * 72 / 25.4

    fun ptToMm(value: Double): Double = value * 25.4 / 72

    fun mmToCm(value: Double): Double = value / 10

    fun cmToMm(value: Double): Double = value * 10

    fun mmToInch(value: Double): Double = value * 0.03937

    fun inchToMm(value: Double): Double = value / 0.03937

}

fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals.toDouble())
    return round(this * multiplier) / multiplier
}