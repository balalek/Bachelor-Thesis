/**
 * File: NotificationResponse.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called NotificationResponse which represents a response for user's notifications API call.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.notifications

/**
 * This data class represents a response for user's notifications API call.
 */
data class NotificationsResponse(
    val data: List<Notifications>,
    val isSuccess: Boolean
)
