/**
 * File: Routing.kt
 * Authors: Martin Baláž
 * Description: This file have one function, that configures the routing for the application by setting up routes for the user and book APIs
 */

package com.example.plugins

import com.example.route.routeBook
import com.example.route.routeUser
import io.ktor.server.application.*

/**
 * Configures the routing for the application by setting up routes for the user and book APIs
 */
fun Application.configureRouting() {
    routeUser()
    routeBook()
}
