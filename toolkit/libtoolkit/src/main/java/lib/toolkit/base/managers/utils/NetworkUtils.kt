package lib.toolkit.base.managers.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import java.net.URLDecoder
import java.net.URLEncoder


object NetworkUtils {

    @JvmStatic
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    @JvmStatic
    fun isWifiEnable(context: Context): Boolean {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        return if (wifi != null){
            when (wifi.wifiState) {
                WifiManager.WIFI_STATE_ENABLED -> true
                WifiManager.WIFI_STATE_DISABLED -> false
                else -> false
            }
        } else {
            false
        }
    }

    @JvmStatic
    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    @JvmStatic
    fun decodeUrl(string: String?): String {
        return URLDecoder.decode(string, "UTF-8")
    }

    @JvmStatic
    fun encodeUrl(string: String?): String {
        return URLEncoder.encode(string, "UTF-8")
    }
}
