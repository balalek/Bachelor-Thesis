/**
 * File: EntityBook.kt
 * Author: Martin Baláž
 * Description: This file defines a Ktorm table object representing a book entity in the MySQL database
 */

package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * This object represents the database table "kniha" in the MySQL database
 */
object EntityBook: Table<Nothing>(tableName = "kniha") {
    val bookId = int(name = "id_knihy").primaryKey()
    val borrowedFrom = int(name = "zapujcuje")
    val borrowedTo = int(name = "je_pujcena")
    val name = varchar(name = "nazev")
    val picture = varchar(name = "fotka")
    val author = varchar(name = "autor")
    val condition = varchar(name = "stav")
    val info = varchar(name = "popis")
    val price = int(name = "cena")
    val accessibility = varchar(name = "pristupnost")
    val maxBorrowedTime = int(name = "max_delka_zapujceni")
    val borrowedDate = varchar(name = "datum_zapujceni")
}