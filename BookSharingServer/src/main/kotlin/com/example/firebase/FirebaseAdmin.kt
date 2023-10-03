/**
 * File: FirebaseAdmin.kt
 * Author: Akash Kamati
 * Original code: https://github.com/akash251/Push-Notification-Ktor-Server/blob/master/src/main/kotlin/com/akash/firebase/FirebaseAdmin.kt
 * Description: This file is used for initializing the Firebase app using the provided service account credentials
 */

package com.example.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.InputStream

/**
 * A helper object responsible for initializing the Firebase app using the provided service account credentials.
 *
 * The FirebaseAdmin.init() function must be called before using any Firebase services.
 */
object FirebaseAdmin {

    // The input stream containing the service account credentials JSON file
    private val serviceAccount : InputStream? =
        this::class.java.classLoader.getResourceAsStream("firebase_service_account.json")

    // The Firebase app initialization options
    private val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    /**
     * This function initializes the Firebase app using the provided service account credentials.
     * Must be called before using any Firebase services.
     * @return The Firebase app instance
     */
    fun init() : FirebaseApp = FirebaseApp.initializeApp(options)

}