/**
 * File: EntityOptions.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a borrow options entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * This object represents the database table "moznosti_zapujceni" in the MySQL database
 */
object EntityOptions: Table<Nothing>(tableName = "moznosti_zapujceni") {
    val optionId = int(name = "id_moznosti").primaryKey()
    val option = varchar(name = "typ")
}