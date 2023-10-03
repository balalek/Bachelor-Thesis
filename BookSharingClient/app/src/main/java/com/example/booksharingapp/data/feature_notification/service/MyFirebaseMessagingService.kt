/**
 * File: MyFirebaseMessagingService.kt
 * Author: Akash Kamati
 * Original code: https://github.com/akash251/Push-Notification-In-Android/blob/master/app/src/main/java/com/akash/mynotificationapp/feature_notification/service/PushNotificationService.kt
 * Description: This file contains class, that is responsible for handling incoming Firebase Cloud Messaging (FCM) messages.
 */

package com.example.booksharingapp.data.feature_notification.service;

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.booksharingapp.data.feature_notification.work.PushNotificationWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * This class extends FirebaseMessagingService and is used to handle incoming Firebase Cloud Messaging (FCM) messages.
 */
class MyFirebaseMessagingService: FirebaseMessagingService() {

    /**
     * This function is called when the FCM token for the device is refreshed
     * @param token The new token string
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Print the token to the console for testing purposes
        println(token)
    }

    /**
     * This function is called when an FCM message is received by the device
     * @param remoteMessage The RemoteMessage object containing the message data
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            // If the message contains data, get the title and body data from the message
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            // Create a Data object with the title and body data
            val workerData = workDataOf(
                "title" to title,
                "body" to body
            )

            // Create a OneTimeWorkRequest for the PushNotificationWorker with network constraints and the workerData as input
            val notificationRequest = OneTimeWorkRequestBuilder<PushNotificationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workerData)
                .build()

            // Enqueue the notificationRequest with WorkManager
            WorkManager.getInstance(applicationContext)
                .enqueue(notificationRequest)
        }
    }

}