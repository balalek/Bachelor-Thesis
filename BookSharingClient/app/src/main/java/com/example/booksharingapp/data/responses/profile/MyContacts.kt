/**
 * File: MyContacts.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyContacts which contains user's contacts on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
/**
 * This data class represents user's contacts on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyContacts (
    @field:SerializedName("uziv_jmeno")
    val myUserName : String,
    @field:SerializedName("sekundarni_email")
    val secondEmail : String,
    @field:SerializedName("tel_cislo")
    val phoneNumber : Int,
    @field:SerializedName("profilovka")
    val myProfilePicture: String,
    @field:SerializedName("misto_predani")
    val handoverPlace: String,
    @field:SerializedName("PSC")
    val psc : Int,
    @field:SerializedName("hodnoceni")
    val myScore : Double,
    @field:SerializedName("uzivatel_vidi_kontakty")
    val isUserPrivileged : Boolean,
    @field:SerializedName("profil_prihlaseneho_uzivatele")
    val clickedOnLoggedInUser : Boolean
) : Parcelable
