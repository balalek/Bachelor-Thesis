/**
 * File: EntityUserBook.kt
 * Author: Martin Baláž
 * Description: This file defines the database table for the many-to-many relationship between users and books
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int


/**
 * This object represents the joining table between users and books in the MySQL database, allowing many-to-many relationship between them.
 */
object EntityUserBook: Table<Nothing>(tableName = "ceka_na") {
    val waitId = int(name = "id_cekani").primaryKey()
    val userId = int(name = "id_uzivatele")
    val bookId = int(name = "id_knihy")
}