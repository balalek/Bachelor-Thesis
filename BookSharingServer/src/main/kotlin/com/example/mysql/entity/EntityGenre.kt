/**
 * File: EntityGenre.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a genre entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * This object represents the database table "zanr" in the MySQL database
 */
object EntityGenre: Table<Nothing>(tableName = "zanr") {
    val genreId = int(name = "id_zanru").primaryKey()
    val genre = varchar(name = "typ_zanru")
}