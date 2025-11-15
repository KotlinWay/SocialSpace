package info.javaway.sc.shared.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.aakira.napier.Napier

/**
 * Android implementation of PhoneDialer.
 * Uses Intent.ACTION_DIAL to open the native phone dialer.
 */
actual class PhoneDialer(private val context: Context) {

    /**
     * Opens the Android phone dialer with the specified phone number.
     * Uses ACTION_DIAL which doesn't require CALL_PHONE permission.
     *
     * @param phoneNumber The phone number to call (format: +7XXXXXXXXXX)
     */
    actual fun dial(phoneNumber: String) {
        try {
            // Normalize phone number (remove spaces, dashes, etc.)
            val normalizedNumber = phoneNumber.replace(Regex("[\\s\\-()]"), "")

            // Create intent with tel: URI
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$normalizedNumber")
                // Add FLAG_ACTIVITY_NEW_TASK for context that is not an Activity
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Napier.d("PhoneDialer: Opening dialer for $normalizedNumber")
            } else {
                Napier.w("PhoneDialer: No app found to handle phone call intent")
            }
        } catch (e: Exception) {
            Napier.e("PhoneDialer: Error opening dialer", e)
        }
    }
}
