package app.editors.manager.managers.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging

object GoogleUtils {

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    fun getDeviceToken(result: (resultListener: String) -> Unit, errorListener: (error: Throwable) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            result(token)
        }.addOnFailureListener { error ->
            errorListener(error)
        }
    }

}