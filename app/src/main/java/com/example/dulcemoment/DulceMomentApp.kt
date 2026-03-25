package com.example.dulcemoment

import android.app.Application
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DulceMomentApp : Application() {
	override fun onCreate() {
		super.onCreate()
		val key = BuildConfig.STRIPE_PUBLISHABLE_KEY
		if (BuildConfig.PAYMENT_PROVIDER.equals("stripe", ignoreCase = true) && key.isNotBlank()) {
			PaymentConfiguration.init(this, key)
		}
	}
}
