/**
 * File: UserIdPrincipalForUser.kt
 * Authors: Belal Khan
 * Original code: https://github.com/probelalkhan/my-story-app-ktor-rest-api/blob/master/src/main/kotlin/net/simplifiedcoding/security/UserIdPrincipalForUser.kt
 * Description: This file have one data class, that represents the authenticated user's identity, which is stored as an integer ID.
 */

package com.example.security

import io.ktor.server.auth.*

/**
 * This data class represents the authenticated user's identity, which is stored as an integer ID.
 */
data class UserIdPrincipalForUser(val id: Int): Principal