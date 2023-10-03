/**
 * File: EntityBookOptions.kt
 * Author: Martin Baláž
 * Description: This file defines the database table for the many-to-many relationship between books and borrow options
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int


/**
 * This object represents the joining table between books and borrow options in the MySQL database, allowing many-to-many relationship between them.
 */
object EntityBookOptions: Table<Nothing>(tableName = "ma") {
    val containsId = int(name = "id_ma").primaryKey()
    val bookId = int(name = "id_knihy")
    val optionId = int(name = "id_moznosti")
}