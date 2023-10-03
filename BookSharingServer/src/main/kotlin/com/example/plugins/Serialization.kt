/**
 * File: Serialization.kt
 * Authors: Martin Baláž
 * Description: This file have one function, that configures content negotiation to use the Gson library for JSON serialization and deserialization
 */

package com.example.plugins

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

/**
 * Configures content negotiation to use the Gson library for JSON serialization and deserialization.
 */
fun Application.configureSerialization() {

    install(ContentNegotiation) {
        gson {

        }
    }

}
