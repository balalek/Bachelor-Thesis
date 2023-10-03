/**
 * File: EntityUserNotifications.kt
 * Author: Martin Baláž
 * Description: This file defines the database table for the many-to-many relationship between users and notifications
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * This object represents the joining table between users and notifications in the MySQL database, allowing many-to-many relationship between them.
 */
object EntityUserNotifications : Table<Nothing>(tableName = "oznameni") {
    val recordId = int(name = "id_oznameni").primaryKey()
    val firstUserId = int(name = "id_uzivatele")
    val secondUserId = int(name = "id_druheho_uzivatele")
    val bookId = int(name = "id_knihy")
    val bookName = varchar(name = "nazev_knihy")
    val notificationId = int(name = "id_typu_oznameni")
    val arrivalDate = varchar(name = "datum_prichodu")
}