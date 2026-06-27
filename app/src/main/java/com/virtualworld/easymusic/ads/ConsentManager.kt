package com.virtualworld.easymusic.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.virtualworld.easymusic.BuildConfig

class ConsentManager(context: Context) {

    private val appContext = context.applicationContext
    val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun gatherConsent(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(
                                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA,
                            )
                            .build(),
                    )
                }
            }
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    logFormError(formError)
                    onComplete()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent info update failed: ${requestConsentError.message}")
                onComplete()
            },
        )
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismiss: (FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismiss)
    }

    private fun logFormError(formError: FormError?) {
        if (formError != null) {
            Log.w(TAG, "Consent form error: ${formError.message}")
        }
    }

    companion object {
        private const val TAG = "ConsentManager"
    }
}
