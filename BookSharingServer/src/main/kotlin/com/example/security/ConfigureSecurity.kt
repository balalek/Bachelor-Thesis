/**
 * File: ConfigureSecurity.kt
 * Authors: Belal Khan
 * Original code: https://github.com/probelalkhan/my-story-app-ktor-rest-api/blob/master/src/main/kotlin/net/simplifiedcoding/security/ConfigureSecurity.kt
 * Description: This file have one function, that configures security settings for the application
 */

package com.example.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/**
 * This function configures security settings for the application
 */
fun Application.configureSecurity(){
    // Initialize JWT configuration with an application name
    JwtConfig.initialize("book-sharing-app")

    // Install the Authentication feature with JWT authentication
    install(Authentication){
        jwt {
            // Set the verifier that will be used to verify the authenticity and integrity of JWT tokens during the authentication process.
            verifier(JwtConfig.instance.verifier)
            // Validate the JWT and create a UserIdPrincipalForUser if valid
            validate {
                val claim = it.payload.getClaim(JwtConfig.CLAIM).asInt()
                if (claim != null) {
                    UserIdPrincipalForUser(claim)
                } else {
                    null
                }
            }
        }
    }
}