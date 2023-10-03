/**
 * File: Utils.kt
 * Authors: Martin Baláž
 * Description: This file contains important constants and multiple helpful functions, which are mainly working with images
 *              and base64 coding and also function for getting user profile information to avoid duplicating long code
 */

package com.example.util

import com.example.model.User
import com.example.model.profile.*
import com.example.mysql.entity.EntityBook
import com.example.mysql.entity.EntityReviews
import com.example.mysql.entity.EntityUser
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Constants for notification type
const val BORROW_BOOK = 1
const val CONTACT_OWNER = 2
const val CONTACT_BORROWER = 3
const val OWNER_DECLINED_BORROW = 4
const val EVALUATE_OWNER = 5
const val EVALUATE_BORROWER = 6
const val TIME_LIMIT_EXPIRED = 7
const val BOOK_IS_NOW_AVAILABLE = 8

// Constants for borrow option
const val PERSONAL_HANDOVER = 1
const val MAIL_TO_POST = 2

// Constant that hold number of days before TIME_LIMIT_EXPIRED notification expires
const val DAYS_TO_STORE_BOOK_TO_LATE_RETURN = 14

/**
 * This function is encoding profile avatars and book pictures to Base64
 * @param imagePath The path of image on server side, stored in DB
 * @return The encoded image in Base64 String
 */
fun encodeImageToBase64(imagePath: String?): String? {
    // In case of profile avatars, which doesn't have to exists, return null
    if(imagePath == null) return null
    // Read all bytes from the specified image path
    val imageBytes = Files.readAllBytes(Paths.get("/${System.getProperty("user.dir")}/$imagePath"))
    // Encode the image bytes to Base64 and return the encoded String
    return Base64.getEncoder().encodeToString(imageBytes)
}

/**
 * This function is decoding book cover image from Base64 and stores it on server side
 * @param encodedBookPhoto The encoded Base64 String of book cover photo
 * @param userId The ID of user, who added the book to the system
 * @return The path to the image, that will be stored in DB
 */
fun decodeBookPhotoFromBase64AndStoreIt(encodedBookPhoto : String, userId: Int) : String {
    // Decodes the Base64-encoded book photo String back to bytes
    val decodedBytes = Base64.getDecoder().decode(encodedBookPhoto)

    // Set up the directory structure for saving the book photo
    val storageDirectory = File("/${System.getProperty("user.dir")}/images")
    val userDirectory = File(storageDirectory, userId.toString())
    val booksDirectory = File(userDirectory, "books")

    // Create user directory if it doesn't exist
    if (!userDirectory.exists()) {
        userDirectory.mkdir()
    }

    // Create books directory if it doesn't exist
    if (!booksDirectory.exists()) {
        booksDirectory.mkdir()
    }

    // Generate a unique file name for the saved image
    var fileName = "book_image.jpg"
    var imageFile = File(booksDirectory, fileName)
    var counter = 1
    while (imageFile.exists()) {
        fileName = "book_image_$counter.jpg" // Add suffix to file name
        imageFile = File(booksDirectory, fileName)
        counter++
    }

    // Write the decoded bytes to the image file
    imageFile.writeBytes(decodedBytes)

    // Return the path to the saved image, that will be stored in DB
    return "images/$userId/books/$fileName"
}

/**
 * This function is decoding profile picture from Base64 and stores it on server side
 * @param encodedBookPhoto The encoded Base64 String of profile picture
 * @param userId The ID of user, who added his avatar to the system
 * @return The path to the image, that will be stored in DB
 */
fun decodeProfilePhotoFromBase64AndStoreIt(encodedProfilePhoto : String, userId: Int) : String {
    // Decodes the Base64-encoded profile photo String back to bytes
    val decodedBytes = Base64.getDecoder().decode(encodedProfilePhoto)

    // Set up the directory structure for saving the profile photo
    val storageDirectory = File("/${System.getProperty("user.dir")}/images")
    val userDirectory = File(storageDirectory, userId.toString())

    // Create user directory if it doesn't exist
    if (!userDirectory.exists()) {
        userDirectory.mkdir()
    }

    // Generate a unique file name for the saved image
    var fileName = "profile_picture.jpg"
    var imageFile = File(userDirectory, fileName)
    var counter = 1
    while (imageFile.exists()) {
        fileName = "profile_picture_$counter.jpg" // Add suffix to file name
        imageFile = File(userDirectory, fileName)
        counter++
    }

    // Write the decoded bytes to the image file
    imageFile.writeBytes(decodedBytes)

    // Return the path to the saved image, that will be stored in DB
    return "images/$userId/$fileName"
}

/**
 * This function retrieves date of birth of logged-in user from DB
 * @param db The instance of DB, on which is being worked
 * @param userId The ID of user, whose date of birth we are getting
 * @return The date of birth of user in this format: YYYY-MM-DD
 */
fun getDateOfBirth(db : Database, userId : Int) : LocalDate{
    // Retrieve the date of birth of logged-in user from the database using a query
    val result = db.from(EntityUser)
        .select(EntityUser.birthday)
        .where {
            EntityUser.userId eq userId
        }
        .map {
            User(
                birthday = it[EntityUser.birthday].toString()
            )
        }.firstOrNull()

    // Format the date string retrieved from the database and return a LocalDate object
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(result?.birthday, formatter)
}

/**
 * This function retrieves all information that belongs to user's profile
 * @param userId The ID of user, whose information we are retrieving
 * @param db The instance of DB, on which is being worked
 * @param loggedInUserId The ID of logged-in user (null or -1 means userId == loggedInUserId)
 * @return The data class called Profile, which contains user's contacts, reviews and books
 */
fun getProfileForUser(userId : Int, db : Database, loggedInUserId: Int?) : Profile {
    // Fill lists of borrowedBooks and unborrowedBooks from specified user and store it in MyBooks
    val myBooks = MyBooks(
        borrowedBooks = db.from(EntityBook)
            .select()
            .where {
                (EntityBook.borrowedTo.isNotNull()) and (EntityBook.borrowedFrom eq userId)
            }
            .groupBy(EntityBook.bookId)
            .orderBy(EntityBook.name.asc())
            .map {
                BorrowedBooks(
                    bookId = it[EntityBook.bookId],
                    bookName = it[EntityBook.name]
                )
            },
        unborrowedBooks =
        db.from(EntityBook)
            .select()
            .where {
                (EntityBook.borrowedTo.isNull()) and (EntityBook.borrowedFrom eq userId)
            }
            .groupBy(EntityBook.bookId)
            .orderBy(EntityBook.name.asc())
            .map {
                UnBorrowedBooks(
                    bookId = it[EntityBook.bookId],
                    bookName = it[EntityBook.name]
                )
            }
    )

    if (loggedInUserId == null || loggedInUserId == -1) {
        // If logged-in user requested profile information, store also books that he borrowed from someone into list + days countdown to return book
        myBooks.booksToReturn =
            db.from(EntityBook)
                .select()
                .where {
                    (EntityBook.borrowedTo eq userId) and (EntityBook.borrowedDate.isNotNull())
                }
                .groupBy(EntityBook.bookId)
                .orderBy(EntityBook.name.asc())
                .map {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val today = LocalDate.now()
                    val parser = LocalDate.parse(it[EntityBook.borrowedDate].toString(), formatter)
                    val maxBorrowedTime = it[EntityBook.maxBorrowedTime].toString().toLong()
                    // Days between today and borrowed book date + max borrowed time + 14 days as backup
                    val daysBetween = ChronoUnit.DAYS.between(today, parser.plusDays(maxBorrowedTime + DAYS_TO_STORE_BOOK_TO_LATE_RETURN))
                    BooksToReturn(
                        bookId = it[EntityBook.bookId],
                        bookName = it[EntityBook.name],
                        days = daysBetween.toInt()
                    )
                }
    }

    // Fill list with reviews that were made to specified user
    val myReviews = db.from(EntityReviews)
        .innerJoin(EntityUser, on = EntityReviews.createdBy eq EntityUser.userId)
        .select()
        .where {
            (EntityReviews.createdOn eq userId)
        }
        .groupBy(EntityReviews.reviewId)
        .orderBy(EntityReviews.dateCreated.desc())
        .map {
            MyReviews(
                createdBy = it[EntityReviews.createdBy],
                userName = it[EntityUser.username],
                score = it[EntityReviews.score],
                dateCreated = it[EntityReviews.dateCreated].toString(),
                content = it[EntityReviews.content],
                profilePicture = encodeImageToBase64(it[EntityUser.profilePic])
            )
        }

    var isUserPrivilegedToSeeContacts = false
    if (loggedInUserId != null && loggedInUserId != -1) {
        // If we aren't on logged-in user profile, then check if logged-in user and second user are borrowing something between each other
        val checkIfUserIsPrivilegedToSeeContacts = db.from(EntityBook)
            .select()
            .where {
                (((EntityBook.borrowedFrom eq userId) and (EntityBook.borrowedTo eq loggedInUserId)) or
                        ((EntityBook.borrowedFrom eq loggedInUserId) and (EntityBook.borrowedTo eq userId)))
            }
            .map {
                it[EntityBook.bookId]
            }
            .firstOrNull()

        // Logged-in user can see contacts only of user, with whom he is borrowing something
        if(checkIfUserIsPrivilegedToSeeContacts != null) isUserPrivilegedToSeeContacts = true
    }

    // Fill MyContacts object with user's contacts
    val myContacts = db.from(EntityUser)
        .select()
        .where {
            (EntityUser.userId eq userId)
        }
        .map {
            MyContacts(
                myUserName = it[EntityUser.username],
                secondEmail = it[EntityUser.secondEmail],
                phoneNumber = it[EntityUser.phoneNumber],
                myProfilePicture = encodeImageToBase64(it[EntityUser.profilePic]),
                handoverPlace = it[EntityUser.placeHandover],
                psc = it[EntityUser.psc],
                myScore = it[EntityUser.score],
                isUserPrivilegedToSeeContacts = isUserPrivilegedToSeeContacts,
                clickedOnLoggedInUser = loggedInUserId == -1
            )
        }
        .firstOrNull()

    return Profile(myBooks = myBooks, myReviews = myReviews, myContacts = myContacts)
}
