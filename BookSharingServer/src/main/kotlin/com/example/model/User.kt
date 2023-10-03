/**
 * File: User.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called User which represents a user entity in the system.
 * 				It is used to serialize and deserialize JSON data to and from the API requests and responses.
 */

package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * This data class represents User. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class User(
	@field:SerializedName("id_uzivatele")
	val userId: Int? = null,
	@field:SerializedName("uziv_jmeno")
	val username: String? = null,
	@field:SerializedName("email")
	val email: String? = null,
	@field:SerializedName("sekundarni_email")
	val secondEmail: String? = null,
	@field:SerializedName("tel_cislo")
	val phoneNumber: Int? = null,
	@field:SerializedName("dat_narozeni")
	val birthday: String? = null,
	@field:SerializedName("heslo")
	val password: String? = null,
	@field:SerializedName("nove_heslo")
	val newPassword: String? = null,
	@field:SerializedName("profilovka")
	val profilePic: String? = null,
	@field:SerializedName("misto_predani")
	val placeHandover: String? = null,
	@field:SerializedName("PSC")
	val psc: Int? = null,
	@field:SerializedName("hodnoceni")
	val score: Double? = null,
	var authToken: String? = null,
	@field:SerializedName("verifikacni_token")
	val verificationToken: String? = null,
	@field:SerializedName("verifikovan")
	val verified: Int? = null,
	@field:SerializedName("token")
	val token: String? = null
)