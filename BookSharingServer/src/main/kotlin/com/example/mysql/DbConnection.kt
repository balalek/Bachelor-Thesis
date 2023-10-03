/**
 * File: DbConnection.kt
 * Authors: Martin Baláž
 * Description: This file have one object with function, that initialize database connection
 */

package com.example.mysql

import org.ktorm.database.Database

/**
 * This Object provides a single instance of a database connection.
 */
object DbConnection {

    private val db: Database? = null

    /**
     * This method returns the single instance of the database connection.
     * @return The single instance of the database connection
     */
    fun getDatabaseInstance(): Database {

        // If the database instance has not yet been initialized, create a new connection using the specified parameters
        return db ?: Database.connect(
            url = "jdbc:mysql://127.0.0.1:3306/bookshare",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = ""
        )
    }

}