/**
 * File: JwtConfig.kt
 * Authors: Belal Khan
 * Original code: https://github.com/probelalkhan/my-story-app-ktor-rest-api/blob/master/src/main/kotlin/net/simplifiedcoding/security/JwtConfig.kt
 * Description: This file have one class, that is responsible for JWT token creation, validation and parsing
 */

package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

/**
 * This class is responsible for JWT token creation, validation and parsing
 * @param secret The secret used for generating the JWT signature
 */
class JwtConfig private constructor(secret: String){

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    /**
     * This function creates an access token for a user with the specified ID
     * @param id The ID of the user to create the token for
     * @return The access token
     */
    fun createAccessToken(id : Int): String = JWT
        .create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(CLAIM, id)
        .sign(algorithm)

    /**
     * Extracts the user ID from a given JWT token
     * @param token The JWT token to extract the user ID from
     * @return The user ID
     * @throws JWTVerificationException if the token cannot be verified
     */
    fun extractUserId(token: String): Int {
        val jwt = verifier.verify(token)

        return jwt.getClaim(CLAIM).asInt()
    }

    companion object{
        private const val ISSUER = "book-sharing-app"
        private const val AUDIENCE = "book-sharing-app"
        const val CLAIM = "id"

        /**
         * The singleton instance of the [JwtConfig] class
         */
        lateinit var instance : JwtConfig
            private set

        /**
         * Initializes the [JwtConfig] singleton instance with the specified secret
         * @param secret The secret used for generating the JWT signature
         */
        fun initialize(secret: String){
            synchronized(this){
                if(!this::instance.isInitialized){
                    instance = JwtConfig(secret)
                }
            }
        }
    }

}