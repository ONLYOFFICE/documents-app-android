package app.editors.manager.managers.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.editors.manager.BuildConfig
import lib.toolkit.base.managers.utils.CryptUtils
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

object KeyStoreUtils {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"
    private const val KEY_ALGORITHM_RSA = "RSA"

    private var isKeyStore: Boolean = false

    fun init() {
        try {
            val ks = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                load(null)
            }

            // Check if key exists and is accessible
            try {
                val privateKey = ks.getKey(BuildConfig.COMMUNITY_ID, null) as PrivateKey?
                val publicKey = ks.getCertificate(BuildConfig.COMMUNITY_ID)?.publicKey

                if (privateKey != null && publicKey != null) {
                    isKeyStore = true
                    return
                }
            } catch (_: Exception) {
                // Key exists but is corrupted, delete it
                ks.deleteEntry(BuildConfig.COMMUNITY_ID)
            }

            // Create new key
            val spec = KeyGenParameterSpec.Builder(
                BuildConfig.COMMUNITY_ID,
                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .setKeySize(2048)
                .build()

            KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, ANDROID_KEY_STORE).apply {
                initialize(spec)
                generateKeyPair()
                isKeyStore = true
            }
        } catch (_: Exception) {
            isKeyStore = false
            // Log the error or handle it appropriately
        }
    }

    fun encryptData(data: String): String {
        return try {
            if (isKeyStore) {
                val ks = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                    load(null)
                }

                val publicKey = ks.getCertificate(BuildConfig.COMMUNITY_ID)?.publicKey
                    ?: return fallbackEncrypt(data)

                val cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING).apply {
                    init(Cipher.ENCRYPT_MODE, publicKey)
                }

                try {
                    Base64.encodeToString(
                        cipher.doFinal(data.toByteArray(Charsets.UTF_8)),
                        Base64.URL_SAFE
                    )
                } catch (e: IllegalBlockSizeException) {
                    fallbackEncrypt(data)
                }
            } else {
                fallbackEncrypt(data)
            }
        } catch (_: Exception) {
            fallbackEncrypt(data)
        }
    }

    fun decryptData(data: String): String {
        return try {
            if (isKeyStore) {
                val ks = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
                    load(null)
                }

                val privateKey = ks.getKey(BuildConfig.COMMUNITY_ID, null) as PrivateKey?
                    ?: return fallbackDecrypt(data)

                val encrypted = Base64.decode(data, Base64.URL_SAFE)

                val cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING).apply {
                    init(Cipher.DECRYPT_MODE, privateKey)
                }

                try {
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                } catch (e: IllegalBlockSizeException) {
                    fallbackDecrypt(data)
                }
            } else {
                fallbackDecrypt(data)
            }
        } catch (_: Exception) {
            fallbackDecrypt(data)
        }
    }

    private fun fallbackEncrypt(data: String): String {
        return CryptUtils.encryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
    }

    private fun fallbackDecrypt(data: String): String {
        return CryptUtils.decryptAES128(data, BuildConfig.COMMUNITY_ID) ?: ""
    }
}