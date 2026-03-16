package com.avilesrodriguez.data.datasource.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FCMTokenDataSource @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun getFcmToken(): String? {
        return try {
            firebaseMessaging.token.await()
        } catch (e: Exception) {
            Log.e("FCMTokenDataSource LOCO", "LOCO Error getting FCM token", e)
            null
        }
    }
}