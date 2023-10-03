/**
 * File: EntityNotifications.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a notifications entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * This object represents the database table "oznameni" in the MySQL database
 */
object EntityNotifications : Table<Nothing>(tableName = "typ_oznameni") {
    val notificationId = int(name = "id_typu_oznameni").primaryKey()
    val notificationType = varchar(name = "typ")
}