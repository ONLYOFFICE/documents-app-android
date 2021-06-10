package lib.toolkit.base.managers.utils

import android.util.Base64
import android.util.Log
import java.nio.charset.Charset

import java.security.Key

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptUtils {

    @JvmField
    val TAG: String = CryptUtils::class.java.simpleName

    private const val ALGORITHM_AES = "AES"
    private const val KEY_LENGTH = 16


    @JvmStatic
    fun encryptAES(value: String?, key: String?): String? {
        return try {
            val secretKey = generateKey(key!!)
            val cipher = Cipher.getInstance(ALGORITHM_AES)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedByteValue = cipher.doFinal(value?.toByteArray(charset("utf-8")))
            val encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT)
            encryptedValue64
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            value
        }
    }

    @JvmStatic
    fun decryptAES(value: String?, key: String?): String? {
        return try {
            val secretKey = generateKey(key!!)
            val cipher = Cipher.getInstance(ALGORITHM_AES)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedValue64 = Base64.decode(value, Base64.DEFAULT)
            val decryptedByteValue = cipher.doFinal(decryptedValue64)
            val decryptedValue = String(decryptedByteValue, Charset.forName("utf-8"))
            decryptedValue
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            value
        }
    }

    @JvmStatic
    fun encryptAES128(value: String?, key: String?): String? {
        return encryptAES(
            value,
            get128Key(key)
        )
    }

    @JvmStatic
    fun decryptAES128(value: String?, key: String?): String? {
        return decryptAES(
            value,
            get128Key(key)
        )
    }

    @JvmStatic
    private fun get128Key(key: String?): String? {
        return key?.let {
            if (key.length > KEY_LENGTH) {
                key.substring(0, KEY_LENGTH)
            } else {
                key + StringUtils.repeatString(
                    "0",
                    KEY_LENGTH - key.length
                )
            }
        }
    }

    @JvmStatic
    private fun generateKey(key: String): Key {
        return SecretKeySpec(key.toByteArray(),
            ALGORITHM_AES
        )
    }

    fun decodeUri(uri: String?): String {
        uri?.let { string ->
            return if (string.contains("data=")) {
                val buffer = Base64.decode(string.replace("data=", ""), Base64.DEFAULT)
                String(buffer, charset("utf-8"))
            } else {
                val buffer = Base64.decode(string, Base64.DEFAULT)
                String(buffer, charset("utf-8"))
            }
        } ?: run {
            return ""
        }
    }

}
