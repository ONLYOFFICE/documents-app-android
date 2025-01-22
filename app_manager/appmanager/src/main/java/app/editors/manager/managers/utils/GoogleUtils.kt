package app.editors.manager.managers.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging

object GoogleUtils {

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val services = GoogleApiAvailability.getInstance()
        return services.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun getDeviceToken(result: (resultListener: String) -> Unit, errorListener: (error: Throwable) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            result(token)
        }.addOnFailureListener { error ->
            errorListener(error)
        }
    }

}