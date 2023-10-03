/**
 * File: NotificationUtil.kt
 * Authors: Akash Kamati, Martin Baláž
 * Original code: https://github.com/akash251/Push-Notification-In-Android/blob/master/app/src/main/java/com/akash/mynotificationapp/feature_notification/work/PushNotificationUtil.kt
 * Description: This file contains one function, that is responsible for creating and displaying a push notification to the user.
 */

package com.example.booksharingapp.data.feature_notification.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.booksharingapp.R
import com.example.booksharingapp.data.util.Constants
import com.example.booksharingapp.ui.home.HomeActivity
import kotlin.random.Random

/**
 * This function creates and displays a push notification to the user.
 * @param title The title text to display in the notification
 * @param body The body text to display in the notification
 * @param context The context of the activity or application calling the function
 */
fun sendNotification(
    title: String,
    body: String,
    context: Context
) {
    // Get the NotificationManager system service
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create a deep link Uri and an intent to launch the HomeActivity and NotificationFragment with the deep link
    val deepLink = Uri.parse("BookSharing://notification")
    val taskIntent = Intent(
        Intent.ACTION_VIEW,
        deepLink,
        context,
        HomeActivity::class.java
    )
    // Set the flags for the intent to clear the task stack and start a new task
    taskIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

    // Create a PendingIntent for the notification to launch the HomeActivity -> NotificationFragment
    val pendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(taskIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // If running on API 23 or higher, set the PendingIntent flags as immutable
            getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE )
        } else {
            getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    // Create the NotificationCompat.Builder object with the notification settings and content
    val notificationBuilder = NotificationCompat.Builder(context, Constants.PUSH_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.baseline_notifications_active_24)
        .setContentTitle(title)
        .setContentText(body)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

    // Display the notification with a random notification ID
    notificationManager.notify(Random.nextInt(), notificationBuilder.build())

}