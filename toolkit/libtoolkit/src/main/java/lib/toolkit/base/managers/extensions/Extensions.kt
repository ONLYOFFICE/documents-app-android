/*
 * Created by Michael Efremov on 17.10.20 11:25
 */

package lib.toolkit.base.managers.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun ViewGroup.inflate(layout: Int) : View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

fun ViewGroup.inflateAttach(layout: Int) : View {
    return LayoutInflater.from(this.context).inflate(layout, this, true)
}