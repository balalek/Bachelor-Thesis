/**
 * File: EntityBook.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a user entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.*

/**
 * This object represents the database table "uzivatel" in the MySQL database
 */
object EntityUser:Table<Nothing>(tableName = "uzivatel") {
    val userId = int(name = "id_uzivatele").primaryKey()
    val username = varchar(name = "uziv_jmeno")
    val email = varchar(name = "email")
    val secondEmail = varchar(name= "sekundarni_email")
    val phoneNumber = int(name = "tel_cislo")
    val profilePic = varchar(name = "profilovka")
    val placeHandover = varchar(name = "misto_predani")
    val birthday = date(name = "dat_narozeni")
    val password = varchar(name = "heslo")
    val psc = int(name = "PSC")
    val score = double(name = "hodnoceni")
    val verificationToken = varchar(name = "verifikacni_token")
    val verified = int(name = "verifikovan")
    val token = varchar(name = "token")
}