package lib.toolkit.base.managers.tools

import android.content.Context
import android.telephony.TelephonyManager
import javax.inject.Inject

class ServiceProvider @Inject constructor(private val context: Context) {

    fun getTelephoneService() = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

}