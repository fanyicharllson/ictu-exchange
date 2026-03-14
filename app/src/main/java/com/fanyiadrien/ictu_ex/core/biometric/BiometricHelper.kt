package com.fanyiadrien.ictu_ex.core.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Reusable biometric prompt helper.
 *
 * Call [authenticate] after Firebase login/signup succeeds,
 * before navigating to Home.
 *
 * Usage in SignInScreen / SignUpScreen:
 *
 *   BiometricHelper.authenticate(
 *       activity  = context as FragmentActivity,
 *       onSuccess = { navController.navigate(Screen.Home.route) { ... } },
 *       onFailure = { showMessage("Biometric failed. Try again.") },
 *       onError   = { msg -> showMessage(msg) }
 *   )
 */
object BiometricHelper {

    /**
     * Returns true if this device supports biometric or PIN authentication.
     * Call this before showing the prompt — if false, skip biometric and go straight to Home.
     */
    fun isAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Shows the biometric prompt.
     *
     * @param activity  The current FragmentActivity (cast context as FragmentActivity)
     * @param onSuccess Called when fingerprint / face / PIN is confirmed
     * @param onFailure Called when user fails or cancels biometric
     * @param onError   Called when a system error occurs (passes readable message)
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                // Finger not recognised — prompt stays open, user can retry
                onFailure()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User cancelled or too many attempts
                onError(errString.toString())
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify it's you")
            .setSubtitle("Use your fingerprint to enter ICTU-Ex")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(promptInfo)
    }
}