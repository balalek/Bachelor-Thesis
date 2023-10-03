/**
 * File: MyContacts.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyContacts which represents user's contacts entity in the system.
 * 				It is used to serialize and deserialize JSON data to and from the API requests and responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents User's contacts. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyContacts (
    @field:SerializedName("uziv_jmeno")
    val myUserName : String? = null,
    @field:SerializedName("sekundarni_email")
    val secondEmail : String? = null,
    @field:SerializedName("tel_cislo")
    val phoneNumber : Int? = null,
    @field:SerializedName("profilovka")
    val myProfilePicture: String? = null,
    @field:SerializedName("misto_predani")
    val handoverPlace: String? = null,
    @field:SerializedName("PSC")
    val psc : Int? = null,
    @field:SerializedName("hodnoceni")
    val myScore : Double? = null,
    @field:SerializedName("uzivatel_vidi_kontakty")
    val isUserPrivilegedToSeeContacts : Boolean = false,
    @field:SerializedName("profil_prihlaseneho_uzivatele")
    val clickedOnLoggedInUser : Boolean = false
)
