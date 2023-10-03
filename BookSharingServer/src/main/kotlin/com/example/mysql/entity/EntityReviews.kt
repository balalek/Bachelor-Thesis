/**
 * File: EntityReviews.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a reviews entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.*

/**
 * This object represents the database table "recenze" in the MySQL database
 */
object EntityReviews : Table<Nothing>(tableName = "recenze") {
    val reviewId = int(name = "id_recenze").primaryKey()
    val createdBy = int(name = "vytvoril")
    val createdOn = int(name = "na")
    val content = varchar(name = "obsah")
    val score = int(name = "hodnoceni")
    val dateCreated = varchar(name = "datum_vytvoreni")
}