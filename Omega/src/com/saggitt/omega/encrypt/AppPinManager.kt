package com.saggitt.omega.encrypt

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

class AppPinManager(private val context: Context) {
    companion object {
        private const val KEY_ALIAS = "APP_PIN_KEY"
        private const val PREF_NAME = "app_pin_store"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12 // GCM standard
        private const val PIN_PREFIX = "pin_"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(pin: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE).apply { Random.Default.nextBytes(this) }
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(), spec)
        val encrypted = cipher.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decrypt(encryptedData: String): String? {
        return try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            val iv = combined.copyOfRange(0, IV_SIZE)
            val encrypted = combined.copyOfRange(IV_SIZE, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null // Decryption failed
        }
    }

    fun setPin(packageName: String, pin: String) {
        val encryptedPin = encrypt(pin)
        prefs.edit().putString("$PIN_PREFIX$packageName", encryptedPin).apply()
    }

    fun verifyPin(packageName: String, inputPin: String): Boolean {
        val stored = prefs.getString("$PIN_PREFIX$packageName", null) ?: return false
        val decrypted = decrypt(stored) ?: return false
        return decrypted == inputPin
    }

    fun removePin(packageName: String) {
        prefs.edit().remove("$PIN_PREFIX$packageName").apply()
    }

    fun isPinSet(packageName: String): Boolean {
        return prefs.contains("$PIN_PREFIX$packageName")
    }
}
