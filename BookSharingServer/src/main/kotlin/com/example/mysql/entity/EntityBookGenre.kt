/**
 * File: EntityBookGenre.kt
 * Author: Martin Baláž
 * Description: This file defines the database table for the many-to-many relationship between books and genres
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int

/**
 * This object represents the joining table between books and genres in the MySQL database, allowing many-to-many relationship between them.
 */
object EntityBookGenre: Table<Nothing>(tableName = "obsahuje") {
    val containsId = int(name = "id_obsahuje").primaryKey()
    val bookId = int(name = "id_knihy")
    val genreId = int(name = "id_zanru")
}