/**
 * File: Notifications.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Notifications which contains user's notifications.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.notifications

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's notifications. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Notifications(
    @field:SerializedName("id_zaznamu")
    val recordId: Int? = null,
    @field:SerializedName("id_oznameni")
    val notificationId: Int? = null,
    @field:SerializedName("nazev_knihy")
    val bookName: String? = null,
    @field:SerializedName("jmeno_uzivatele")
    val userName: String? = null,
    @field:SerializedName("id_uzivatele")
    val userId: Int? = null,
    @field:SerializedName("id_knihy")
    val bookId: Int? = null,
    @field:SerializedName("hodnoceni")
    val score: Double? = null,
    @field:SerializedName("profilovka")
    val profilePicture: String? = null,
    @field:SerializedName("datum_prichodu")
    val arrivalDate: String? = null,
    var isExpanded: Boolean = false
)
