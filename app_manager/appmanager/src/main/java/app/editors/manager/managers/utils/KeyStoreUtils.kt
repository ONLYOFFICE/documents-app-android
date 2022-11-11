package app.editors.manager.managers.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.editors.manager.BuildConfig
import app.editors.manager.app.App
import lib.toolkit.base.managers.utils.CryptUtils
import java.security.*
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

object KeyStoreUtils {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"
    private const val KEY_ALGORITHM_RSA = "RSA"

    fun init() {
        try {
            val ks = KeyStore.getInstance(ANDROID_KEY_STORE)
            ks.load(null)

            val privateKey = ks.getKey(BuildConfig.COMMUNITY_ID, null) as PrivateKey?
            val publicKey: PublicKey? = ks.getCertificate(BuildConfig.COMMUNITY_ID)?.publicKey

            privateKey?.let {
                publicKey?.let {
                    return
                }
            }

            val spec = KeyGenParameterSpec.Builder(BuildConfig.COMMUNITY_ID, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()

            val kpGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
            kpGen.initialize(spec)
            kpGen.generateKeyPair()
        } catch (error: RuntimeException) {
            App.getApp().isKeyStore = false
        }
    }

    fun encryptData(data: String): String {
        if (App.getApp().isKeyStore) {
            val ks = KeyStore.getInstance(ANDROID_KEY_STORE)
            ks.load(null)

            val publicKey = ks.getCertificate(BuildConfig.COMMUNITY_ID).publicKey ?: return ""

            val cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING)

            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val encryptedData: ByteArray?
            try {
                encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            } catch (error: IllegalBlockSizeException) {
                return CryptUtils.encryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
            }

            return Base64.encodeToString(encryptedData, Base64.DEFAULT)
        } else {
            return CryptUtils.encryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
        }
    }

    fun decryptData(data: String): String {
       if (App.getApp().isKeyStore) {
           val ks = KeyStore.getInstance(ANDROID_KEY_STORE)
           ks.load(null)

           val privateKey = ks.getKey(BuildConfig.COMMUNITY_ID, null) as PrivateKey

           val encrypted = Base64.decode(data, Base64.DEFAULT)

           val cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING)

           cipher.init(Cipher.DECRYPT_MODE, privateKey)

           val result: ByteArray?
           try {
               result = cipher.doFinal(encrypted)
           } catch (error: IllegalBlockSizeException) {
               return CryptUtils.decryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
           }
           return result.toString(Charsets.UTF_8)
       } else {
           return CryptUtils.decryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
       }
    }

}