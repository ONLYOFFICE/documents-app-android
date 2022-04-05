package app.editors.manager.managers.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object GoogleUtils {

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val services = GoogleApiAvailability.getInstance()
        return when (services.isGooglePlayServicesAvailable(context)) {
            ConnectionResult.SUCCESS -> {
                true
            }
            else -> false
        }
    }

}