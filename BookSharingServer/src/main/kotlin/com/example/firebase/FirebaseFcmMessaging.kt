/**
 * File: FirebaseFcmMessaging.kt
 * Authors: Martin Baláž, Akash Kamati
 * Original code: https://github.com/akash251/Push-Notification-Ktor-Server/blob/master/src/main/kotlin/com/akash/firebase/FirebaseFcmMessaging.kt
 * Description: This file have one function, that is sending notifications to clients using Firebase Cloud Messaging (FCM)
 */

package com.example.firebase

import com.example.mysql.entity.EntityUser
import com.example.util.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.dsl.*

/**
 * This function sends a notification to a user with the given user ID,
 * using the Firebase Cloud Messaging service. The notification content
 * is determined by the given notification type. The function retrieves
 * the user's device token from the database and sets it as the target
 * for the notification message.
 *
 * @param notificationType The type of the notification to send (8 types at the moment)
 * @param db The database connection used for retrieving the user's token
 * @param userId The ID of the user to send the notification to
 * @return A string indicating whether the notification was sent successfully
 */
suspend fun sendNotification(notificationType: Int, db: Database, userId: Int) : String {
    return withContext(Dispatchers.IO) {
        // Create a new Firebase Cloud Messaging (FCM) message builder
        val dataMessage = Message.builder()
        // Set the message title and body based on the notificationType parameter
        when (notificationType) { // TODO you could add some $variables to notification text (like book names, or user names)
            BORROW_BOOK -> {
                dataMessage
                    .putData("title", "Někdo si chce půjčit vaši knihu!")
                    .putData("body", "Po kliknutí na oznámení odpovězte na nabídku.")
            }

            CONTACT_OWNER -> {
                dataMessage
                    .putData("title", "Můžete kontaktovat majitele knihy.")
                    .putData("body", "Po kliknutí si zobrazte kontakty majitele knihy a můžete se domluvit na výměně knihy."
                    )

            }

            CONTACT_BORROWER -> {
                dataMessage
                    .putData("title", "Můžete kontaktovat půjčujícího.")
                    .putData("body", "Po kliknutí si zobrazte kontakty půjčujícího a můžete se domluvit na výměně knihy."
                    )

            }

            OWNER_DECLINED_BORROW -> {
                dataMessage
                    .putData("title", "Vaše žádost o knihu byla odmítnuta.")
                    .putData("body", "Po kliknutí zobrazíte podrobnosti o odmítnutí.")

            }

            EVALUATE_OWNER -> {
                dataMessage
                    .putData("title", "Je čas napsat recenzi!")
                    .putData("body", "Po kliknutí ohodnoťte a napište recenzi majiteli knihy vaší nedávné výpůjčky.")

            }

            EVALUATE_BORROWER -> {
                dataMessage
                    .putData("title", "Je čas napsat recenzi!")
                    .putData("body", "Po kliknutí ohodnoťte a napište recenzi na půjčujícího vaší knihy z nedávné výpůjčky."
                    )

            }

            TIME_LIMIT_EXPIRED -> {
                dataMessage
                    .putData("title", "Vaší knize vypršel časový limit!")
                    .putData("body", "Po kliknutí nám dejte vědět o vrácení knihy.")

            }

            BOOK_IS_NOW_AVAILABLE -> {
                dataMessage
                    .putData("title", "Kniha byla uvolněna!")
                    .putData("body", "Po kliknutí si můžete konečně vypůjčit vaši vysněnou knihu.")

            }
        }

        // Find the FCM registration token of the receiver
        val token = db.from(EntityUser)
            .select()
            .where {
                (EntityUser.userId eq userId)
            }
            .map {
                it[EntityUser.token]
            }
            .firstOrNull()

        // If a token is found, set it for the message
        if (token != null) {
            dataMessage.setToken(token)
        }
        // Try to send the notification and return result (Success or Failed)
        try {
            FirebaseMessaging.getInstance().send(dataMessage.build())
            "Success"
        } catch (e: Exception) {
            "Failed"
        }
    }
}