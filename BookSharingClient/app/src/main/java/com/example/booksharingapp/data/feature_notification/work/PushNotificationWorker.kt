/**
 * File: PushNotificationWorker.kt
 * Authors: Akash Kamati, Martin Baláž
 * Original code: https://github.com/akash251/Push-Notification-In-Android/blob/master/app/src/main/java/com/akash/mynotificationapp/feature_notification/work/NotificationWorker.kt
 * Description: This file contains background worker class, that is responsible for receiving push notification data and displaying a push notification.
 */

package com.example.booksharingapp.data.feature_notification.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * This is a background worker class that receives push notification data and displays a push notification.
 * @param context The context of the activity or application calling the function
 * @param parameters The parameters passed to the worker
 */
class PushNotificationWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    /**
     * This is a suspend function that performs the background work of receiving push notification data and displaying a push notification.
     * @return A Result object indicating the success or failure of the work performed
     */
    override suspend fun doWork(): Result {

        // Get the title and body data from the input data
        val title = inputData.getString("title")
        val body = inputData.getString("body")

        // Send the push notification with the title and body
        sendNotification(title!!, body!!, applicationContext)

        // Indicate that the work was successful
        return Result.success()

    }

}