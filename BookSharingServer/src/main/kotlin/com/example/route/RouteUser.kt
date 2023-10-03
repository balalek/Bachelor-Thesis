/**
 * File: RouteUser.kt
 * Authors: Martin Baláž
 * Description: This file contains the routing logic for all user-related endpoints, such as reading, creating, updating, and deleting users,
 *              but it also contains some notifications-related endpoints. The endpoints (except register and login) are secured using authentication,
 *              so that only authorized users can access them.
 */

package com.example.route

import com.example.firebase.sendNotification
import com.example.model.User
import com.example.model.notifications.IsReturned
import com.example.model.notifications.Notifications
import com.example.model.profile.*
import com.example.mysql.DbConnection
import com.example.mysql.entity.*
import com.example.security.JwtConfig
import com.example.security.hash
import com.example.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.io.File
import java.lang.Exception
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Sets up the routing for all endpoints related to users in the application, but also some notifications.
 * This function is called along with routeBook() from main application file to configure the routes.
 * All endpoints related to users and notifications are prefixed with "/user" or "/auth" to ensure consistency.
 * The function uses the Ktor DSL to define the routing and handle requests.
 */
fun Application.routeUser() {
    // Creating database instance, which I will be using to work with my local database
    val db: Database = DbConnection.getDatabaseInstance()
    routing {

        /**
         * Register endpoint for creating a new user
         */
        post("/auth/register")
        {
            // Retrieve user details from the request body
            val user: User = call.receive()

            //val email = user.email
            //val token = "123456789" // TODO make some random function

            // Convert user's date of birth string to LocalDate object
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateOfBirth = LocalDate.parse(user.birthday, formatter)
            try {
                // Attempt to insert the user into the database
                val noOfRowsAffected = db.insert(EntityUser)
                {
                    set(it.username, user.username)
                    set(it.email, user.email)
                    set(it.secondEmail, user.email)
                    set(it.phoneNumber, user.phoneNumber)
                    set(it.birthday, dateOfBirth)
                    set(it.password, hash(user.password))
                    //set(it.verificationToken, token)
                }
                if (noOfRowsAffected > 0) {
                    /*val token = JwtConfig.instance.createAccessToken(user.userId ?: -1)
                    user.authToken = token*/
                    //sendVerificationEmail(email, token)

                    // If user was successfully created, respond with success message
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(
                            isSuccess = true,
                            data = null
                        )
                    )
                } else {
                    // If user was not created, respond with error message
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = "Error to register the user")
                    )
                }

            } catch (e: Exception) {
                // If there was an error with the insertion because of duplicate email/username/phone number, respond with error message
                call.respond(
                    HttpStatusCode.Conflict,
                    GenericResponse(isSuccess = false, data = "Duplicate e-mail, phone number or username")
                )
            }


        }

        // TODO finish in future (E-mail verification)
        /*get("/verify-email") {
            val token = call.receive<VerifyEmailRequest>().token
            val user = db.from(EntityUser)
                .select()
                .where {
                    EntityUser.verificationToken eq token
                }
                .map {
                    User(
                        userId = it[EntityUser.userId]
                    )
                }
                .firstOrNull()
            if (user != null) {
                val noOfRowsAffected = db.update(EntityUser)
                {
                    set(it.verified, 1)
                    where {
                        it.verificationToken eq token
                    }
                }
                if (noOfRowsAffected > 0) {
                    call.respond(HttpStatusCode.OK, "Email verification successful")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Email verification failed")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid token")
            }
        }*/

        /**
         * Login endpoint for send authentication token to user if he is in database, and allow him to login in application
         */
        post("/auth/login")
        {
            val loginUser: User = call.receive()

            // Find user in database
            val user = db.from(EntityUser)
                .select()
                .where {
                    (EntityUser.email eq loginUser.email.toString()) and
                            (EntityUser.password eq hash(loginUser.password.toString()))//and
                    //(EntityUser.verified eq 1)
                }
                .map {
                    User(
                        userId = it[EntityUser.userId],
                        email = it[EntityUser.email],
                        phoneNumber = it[EntityUser.phoneNumber],
                        birthday = it[EntityUser.birthday].toString(),
                        password = it[EntityUser.password],
                        token = it[EntityUser.token]
                    )
                }
                .firstOrNull()

            if (user != null) {
                // If user was found, create new unique authentication token, that user will send to server each request, so he is authorized to do so
                val jwtoken = JwtConfig.instance.createAccessToken(user.userId ?: -1)
                user.authToken = jwtoken

                if (user.token != loginUser.token) {
                    // If registration token of user's device doesn't equal to the one in DB, then replace it with new device token (Or it's the first login)
                    val noOfRowsAffected = db.update(EntityUser)
                    {
                        set(it.token, loginUser.token)
                        where {
                            (it.userId eq user.userId!!)
                        }
                    }

                    if (noOfRowsAffected > 0) {
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(isSuccess = true, data = user)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(isSuccess = false, data = null)
                        )
                    }

                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = user)
                    )
                }
            // If there was an error with login because user wasn't found in DB, respond with error message
            } else
                call.respond(
                    HttpStatusCode.Unauthorized,
                    GenericResponse(isSuccess = false, data = null)
                )
        }
        /**
         * Menu information endpoint for getting profile picture, e-mail and username and sending it back to user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/user/menuInfo")
            {
                // Extract JWT token from request header and get user ID
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Retrieve user information from database
                val user = db.from(EntityUser)
                    .select()
                    .where {
                        EntityUser.userId eq userId
                    }
                    .map {
                        User(
                            email = it[EntityUser.email],
                            username = it[EntityUser.username],
                            profilePic = encodeImageToBase64(it[EntityUser.profilePic])
                        )
                    }
                    .firstOrNull()

                // Respond with user information if found, otherwise with error message
                if (user != null)
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = user)
                    )
                else
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = null)
                    )
            }
        }

        /**
         * User endpoint for getting profile information for logged-in user as contacts, reviews and books.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/user")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // get all profile information from function and send null as loggedInUserId, which means it is logged-in user
                val profile = getProfileForUser(userId, db, null)

                // TODO delete
                //if (profile.myBooks?.borrowedBooks!!.isNotEmpty() || profile.myBooks.unborrowedBooks!!.isNotEmpty() || profile.myBooks.booksToReturn!!.isNotEmpty() || profile.myContacts != null || profile.myReviews!!.isNotEmpty())
                if (profile.myContacts != null)
                    // If there is something to send, then send it (all users must have contacts!)
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = profile)
                    )
                else
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = null)
                    )
            }
        }

        /**
         * User endpoint for getting profile information for logged-in or different user as contacts, reviews and books.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/user/{userId}")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val loggedInUserId = JwtConfig.instance.extractUserId(token)

                // Extract the clicked-on user ID from the request parameters and convert it to an integer
                val userIdStr = call.parameters["userId"]
                val userId = userIdStr?.toInt() ?: -1

                val profile = if (userId == loggedInUserId)
                    // If user clicked on himself, then make sure the function knows it by sending -1 to loggedInUserId
                    getProfileForUser(userId, db, -1)
                else
                    getProfileForUser(userId, db, loggedInUserId)

                // TODO delete
                //if (profile.myBooks?.borrowedBooks!!.isNotEmpty() || profile.myBooks.unborrowedBooks!!.isNotEmpty() || profile.myContacts != null || profile.myReviews!!.isNotEmpty())
                if (profile.myContacts != null)
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = profile)
                    )
                else
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = null)
                    )
            }
        }

        /**
         * Review user endpoint for reviewing user, after book has been returned (soon or late), or not returned, or not borrowed at all
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/user/{userId}/review") // TODO in future I could check if review from user to user already exists and simply replace it
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val review: MyReviews = call.receive()

                // Get ID of user, we want to review
                val reviewedUserId = call.parameters["userId"]?.toIntOrNull()
                if (reviewedUserId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    // Create review to specified user from logged-in user
                    val reviewUser = db.insertAndGenerateKey(EntityReviews)
                    {
                        set(it.createdBy, userId)
                        set(it.createdOn, reviewedUserId)
                        set(it.content, review.content)
                        set(it.score, review.score)
                        val timestamp = Timestamp(System.currentTimeMillis())
                        set(it.dateCreated, timestamp.toString())
                    } as Int
                    if (reviewUser > 0) {
                        // Calculate user's new average score
                        val scores = db.from(EntityReviews)
                            .select(EntityReviews.score)
                            .where {
                                EntityReviews.createdOn eq reviewedUserId
                            }
                            .map {
                                it[EntityReviews.score]
                            }

                        val totalScore = scores.sumOf { it!! }
                        val averageScore = if (scores.isNotEmpty()) totalScore.toDouble() / scores.size else null

                        // Update user's average score
                        val rowsAffected = db.update(EntityUser)
                        {
                            set(it.score, averageScore)
                            where {
                                it.userId eq reviewedUserId
                            }
                        }

                        if (rowsAffected > 0) {
                            // Delete this notification, from which logged-in user sent review to the specified user
                            val noOfRowsAffected = db.delete(EntityUserNotifications)
                            {
                                it.recordId eq review.recordId!!
                            }

                            if (noOfRowsAffected == 0) {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    GenericResponse(
                                        isSuccess = false,
                                        data = "Daná žádost o půjčku knihy nebyla nalezena"
                                    )
                                )
                            } else {
                                call.respond(
                                    HttpStatusCode.OK,
                                    GenericResponse(
                                        isSuccess = true,
                                        data = null
                                    )
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GenericResponse(isSuccess = false, data = "Skóre uživatele nemohlo být aktualizováno.")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            GenericResponse(isSuccess = false, data = "Recenze nemohla být vytvořena.")
                        )
                    }
                }
            }
        }

        /**
         * Change password endpoint for changing password to logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            put("/user/changePassword")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val loggedUser: User = call.receive()

                // Find user in DB and check if old passwords are matching, then update it with new hashed password
                val noOfRowsAffected = db.update(EntityUser)
                {
                    set(it.password, hash(loggedUser.newPassword))
                    where {
                        (it.userId eq userId) and (it.password eq hash(loggedUser.password.toString()))
                    }
                }

                if (noOfRowsAffected > 0) {
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = null)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = "Nesprávné aktuální heslo")
                    )
                }
            }
        }

        /**
         * Contacts endpoint for updating logged-in user's profile contacts
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            put("/user/contacts")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val loggedUser: MyContacts = call.receive()

                // Find where is profile picture of logged-in user stored in server side
                val oldPhotoPath = db.from(EntityUser)
                    .select(EntityUser.profilePic)
                    .where {
                        EntityUser.userId eq userId
                    }
                    .map {
                        it[EntityUser.profilePic]
                    }
                    .singleOrNull()

                // Encode profile image to Base64 if it exists
                val currentPictureEncoded = encodeImageToBase64(oldPhotoPath)

                // If logged-in user is offering a book with personal borrow option checked, then he can´t delete it from contacts
                val isHandoverPlaceNeeded = db.from(EntityBook)
                    .innerJoin(EntityUser, on = EntityBook.borrowedFrom eq EntityUser.userId)
                    .innerJoin(EntityBookOptions, on = EntityBook.bookId eq EntityBookOptions.bookId)
                    .innerJoin(EntityOptions, on = EntityBookOptions.optionId eq EntityOptions.optionId)
                    .select()
                    .where {
                        (EntityUser.userId eq userId) and (EntityOptions.option eq "Osobní předání")
                    }
                    .map {
                        it[EntityBook.bookId]
                    }
                    .firstOrNull()

                // If logged-in user is borrowing a book from someone that has mail to post option checked, then he can't delete PSC from contacts
                val isPostalCodeNeeded = db.from(EntityBook)
                    .innerJoin(EntityBookOptions, on = EntityBook.bookId eq EntityBookOptions.bookId)
                    .innerJoin(EntityOptions, on = EntityBookOptions.optionId eq EntityOptions.optionId)
                    .select()
                    .where{
                        (EntityBook.borrowedTo eq userId) and (EntityOptions.option eq "Zaslání na odběrné místo")
                    }
                    .map {
                        it[EntityBook.bookId]
                    }
                    .firstOrNull()

                if (isHandoverPlaceNeeded != null && loggedUser.handoverPlace == "") {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Nesmíte odstranit místo pro předání knihy, když půjčujete knihu s takovou možností"
                        )
                    )
                } else if (isPostalCodeNeeded != null && loggedUser.psc == null) {
                    call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        GenericResponse(
                            isSuccess = false,
                            data = "Nesmíte odstranit PSČ, když si půjčujete knihu s takovou možností"
                        )
                    )
                } else {
                    // Now that everything is checked, we can update user's contacts
                    val noOfRowsAffected = db.update(EntityUser)
                    {
                        set(it.username, loggedUser.myUserName)
                        set(it.secondEmail, loggedUser.secondEmail)
                        set(it.placeHandover, loggedUser.handoverPlace)
                        set(it.phoneNumber, loggedUser.phoneNumber)
                        set(it.psc, loggedUser.psc)
                        if (loggedUser.myProfilePicture != null && loggedUser.myProfilePicture != currentPictureEncoded)
                            // Check profile picture, only if it has changed
                            set(it.profilePic, decodeProfilePhotoFromBase64AndStoreIt(loggedUser.myProfilePicture, userId))
                        where {
                            it.userId eq userId
                        }
                    }

                    if (noOfRowsAffected > 0) {
                        if (currentPictureEncoded != null && loggedUser.myProfilePicture != null && loggedUser.myProfilePicture != currentPictureEncoded) {
                            // Delete the old profile picture, if we added new one (unless it's the very first profile picture)
                            val oldFile = File("/${System.getProperty("user.dir")}/$oldPhotoPath")
                            oldFile.delete()
                        }
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(isSuccess = true, data = null)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            GenericResponse(
                                isSuccess = false,
                                data = "E-mail, uživatelské jméno, nebo telefonní číslo je již zabrané"
                            )
                        )
                    }
                }
            }
        }

        /**
         * Delete account endpoint for deleting account of logged-in user, but only if he isn't borrowing any book to/from someone.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            delete("/user/deleteAccount") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Check if user isn't borrowing any book to/from someone else
                val isUserDeletable = db.from(EntityBook)
                    .select()
                    .where {
                        (EntityBook.borrowedTo eq userId) or ((EntityBook.borrowedFrom eq userId) and (EntityBook.borrowedTo.isNotNull()))
                    }
                    .map {
                        it[EntityBook.bookId]
                    }
                    .firstOrNull()

                if (isUserDeletable != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Uživatel nesmí nikomu půjčovat knihu a nesmí mít ani žádnou vypůjčenou."
                        )
                    )
                } else {
                    // Delete user from User table, which should cascade delete, all other related things to him in database.
                    val rowsAffected = db.delete(EntityUser)
                    {
                        it.userId eq userId
                    }

                    if (rowsAffected == 0) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(
                                isSuccess = false,
                                data = "Daný uživatel nebyl nalezen"
                            )
                        )
                    } else {
                        // Delete recursively all imaged from logged-in user, meaning his profile image and all his books cover photos
                        val file = File("/${System.getProperty("user.dir")}/images/$userId")
                        try {
                            file.deleteRecursively()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(
                                isSuccess = true,
                                data = null
                            )
                        )
                    }
                }
            }
        }

        /**
         * Notifications endpoint for getting notifications and sending them to logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/user/notifications") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Get all notifications, that are meant for logged-in user
                val notificationsList = db.from(EntityUserNotifications)
                    .innerJoin(EntityUser, on = EntityUser.userId eq EntityUserNotifications.secondUserId)
                    .leftJoin(EntityBook, on = EntityBook.bookId eq EntityUserNotifications.bookId)
                    .select()
                    .where {
                        EntityUserNotifications.firstUserId eq userId
                    }
                    .orderBy(EntityUserNotifications.arrivalDate.desc())
                    .map {
                        // In case of review notifications, book may not exist anymore, in that case receive book name from notification
                        val bookName = if (it[EntityUserNotifications.bookId] != null) {
                            it[EntityBook.name]
                        } else {
                            it[EntityUserNotifications.bookName]
                        }
                        Notifications(
                            recordId = it[EntityUserNotifications.recordId],
                            notificationId = it[EntityUserNotifications.notificationId],
                            bookName = bookName,
                            bookId = it[EntityUserNotifications.bookId],
                            userName = it[EntityUser.username],
                            userId = it[EntityUser.userId],
                            profilePicture = encodeImageToBase64(it[EntityUser.profilePic]),
                            score = it[EntityUser.score],
                            arrivalDate = it[EntityUserNotifications.arrivalDate]
                        )
                    }

                if (notificationsList.isNotEmpty())
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = notificationsList)
                    )
                else
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = null)
                    )
            }
        }

        /**
         * Delete notification endpoint for deleting some specific notifications for logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            delete("/user/{notificationId}")
            {
                val notificationId = call.parameters["notificationId"]?.toIntOrNull()
                if (notificationId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID notifikace"
                        )
                    )
                } else {
                    // Delete notification from database
                    val rowsAffected = db.delete(EntityUserNotifications)
                    {
                        it.recordId eq notificationId
                    }

                    if (rowsAffected == 0) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(
                                isSuccess = false,
                                data = "Daná notifikace nebyla nalezena"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(
                                isSuccess = true,
                                data = null
                            )
                        )
                    }
                }
            }
        }
    }

}