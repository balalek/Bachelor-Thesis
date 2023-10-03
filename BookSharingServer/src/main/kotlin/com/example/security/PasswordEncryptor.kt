/**
 * File: PasswordEncryptor.kt
 * Authors: Belal Khan
 * Original code: https://github.com/probelalkhan/my-story-app-ktor-rest-api/blob/master/src/main/kotlin/net/simplifiedcoding/security/PasswordEncryptor.kt
 * Description: This file have one function, that generates an HMAC hash for the specified password
 */

package com.example.security

import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val SECRET_KEY = "" // TODO Couldn´t post it on github
private val ALGORITHM = "HmacSHA1"
private val HASH_KEY = hex(SECRET_KEY)
private val HMAC_KEY = SecretKeySpec(HASH_KEY, ALGORITHM)

/**
 * This function generates an HMAC hash for the specified password
 * @param password The password to generate an HMAC hash for
 * @return The generated HMAC hash
 * @throws NoSuchAlgorithmException if the specified algorithm is not supported
 * @throws InvalidKeyException if the specified key is invalid
 */
fun hash(password: String?): String{
    val hmac = Mac.getInstance(ALGORITHM)
    hmac.init(HMAC_KEY)
    return hex(hmac.doFinal(password?.toByteArray(Charsets.UTF_8)))
}
