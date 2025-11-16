package info.javaway.sc.shared.utils

/**
 * Platform-specific phone dialer utility.
 * Allows calling phone numbers using the native phone app.
 */
expect class PhoneDialer {
    /**
     * Initiates a phone call to the specified phone number.
     * Opens the native phone dialer with the number pre-filled.
     *
     * @param phoneNumber The phone number to call (format: +7XXXXXXXXXX)
     */
    fun dial(phoneNumber: String)
}
