/**
 * File: Application.kt
 * Authors: Martin Baláž
 * Description: This file have main function, that will start embedded Tomcat server on specified port and IP address,
 *              it will also initialize FirebaseAdmin SDK for sending notifications and configures security, routing and serialization
 */

package com.example

import com.example.firebase.FirebaseAdmin
import com.example.plugins.*
import com.example.security.configureSecurity
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*

fun main() {
    // Start the embedded Tomcat server on specified port and IP address
    //embeddedServer(Tomcat, port = 8080, host = "147.229.216.179") {
    embeddedServer(Tomcat, port = 8080, host = "192.168.0.101") {
        // Initialize FirebaseAdmin SDK for sending notifications
        FirebaseAdmin.init()
        configureSecurity()
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}
