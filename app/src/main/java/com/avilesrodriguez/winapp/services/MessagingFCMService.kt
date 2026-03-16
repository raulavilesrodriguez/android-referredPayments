package com.avilesrodriguez.winapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetAndStoreFCMToken
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.navigation.DeepLinks
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MessagingFCMService: FirebaseMessagingService() {

    @Inject
    lateinit var getAndStoreFCMToken: GetAndStoreFCMToken
    @Inject
    lateinit var getCurrentUserId: CurrentUserId

    companion object{
        const val CHANNEL_ID = "CHANNEL_ID"
        const val  CHANNEL_DESCRIPTION = "CHANNEL_DESCRIPTION"
        const val CHANNEL_TITLE = "CHANNEL_TITLE"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val userId = getCurrentUserId()
        if(userId.isNotEmpty()){
            // Usamos una corrutina en el scope de IO para guardar el nuevo token en segundo plano.
            // No usamos viewModelScope porque esto no es un ViewModel.
            CoroutineScope(Dispatchers.IO).launch {
                getAndStoreFCMToken(userId)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if(remoteMessage.data.isNotEmpty()){
            val senderName = remoteMessage.data["senderName"]
            val messageContent = remoteMessage.data["content"]
            val referralId = remoteMessage.data["referralId"]

            val notificationId = referralId?.hashCode() ?: System.currentTimeMillis().toInt()

            if(referralId !=null){
                showNotification(senderName, messageContent, referralId, notificationId)
            }

        }
    }

    private fun showNotification(senderName: String?, messageContent: String?, referralId:String, notificationId: Int){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_TITLE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)

        val deepLinkUrl = DeepLinks.REFERRAL_URL.replace("{id}", referralId)

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUrl.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent for the Intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationIconResId = R.drawable.logo_app

        //build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(notificationIconResId)
            .setContentTitle(senderName ?: "Unknown Sender")
            .setContentText(messageContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}