/**
 * File: RouteBook.kt
 * Authors: Martin Baláž
 * Description: This file contains the routing logic for all book-related endpoints, such as reading, creating, updating, and deleting books,
 *              but it also contains some notifications-related endpoints. The endpoints are secured using authentication, so that only
 *              authorized users can access them
 */

package com.example.route

import com.example.firebase.sendNotification
import com.example.model.Book
import com.example.model.BookCard
import com.example.model.BookInfo
import com.example.model.User
import com.example.model.notifications.IsConfirmed
import com.example.model.notifications.IsReturned
import com.example.mysql.DbConnection
import com.example.mysql.entity.*
import com.example.security.JwtConfig
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
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Sets up the routing for all endpoints related to books in the application, but also some notifications.
 * This function is called along with routeUser() from main application file to configure the routes.
 * All endpoints related to books and notifications are prefixed with "/book" or "/books" to ensure consistency.
 * The function uses the Ktor DSL to define the routing and handle requests.
 */
fun Application.routeBook() {
    // Creating database instance, which I will be using to work with my local database
    val db: Database = DbConnection.getDatabaseInstance()
    routing {

        /**
         * Books endpoint for getting books from database and sending them to user. Also send TIME_LIMIT_EXPIRED notifications to users, that haven't got it.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/books")
            {
                // Extract JWT token from request header and get user ID
                val bearerToken = call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                /* START OF TIME_LIMIT_NOTIFICATION SECTION */
                // Because the main screen in app is the most used one, then in here it will check for expired borrowed books and sends notifications to users
                val listOfNotReturnedBooksButAlreadyNotified = db.from(EntityUserNotifications)
                    .select()
                    .where {
                        (EntityUserNotifications.notificationId eq TIME_LIMIT_EXPIRED)
                    }
                    .map {
                        Book (
                            bookId = it[EntityUserNotifications.bookId]
                        )
                    }

                val listOfNotReturnedBooks = db.from(EntityBook)
                    .select()
                    .where {
                        EntityBook.borrowedTo.isNotNull()
                    }
                    .map {
                        Book (
                            bookId = it[EntityBook.bookId],
                            borrowedDate = it[EntityBook.borrowedDate],
                            maxBorrowedTime = it[EntityBook.maxBorrowedTime],
                            borrowedFrom = it[EntityBook.borrowedFrom],
                            borrowedTo = it[EntityBook.borrowedTo]
                        )
                    }

                // Filter only books, which owners didn't receive return notification yet
                val listOfNotReturnedBooksToNotify = listOfNotReturnedBooks
                    .distinctBy { it.bookId }
                    .filter { book ->
                        listOfNotReturnedBooksButAlreadyNotified.none { it.bookId == book.bookId }
                    }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val today = LocalDate.now()

                for (book in listOfNotReturnedBooksToNotify) {
                    val parser = LocalDate.parse(book.borrowedDate.toString(), formatter)
                    val maxBorrowedTime = book.maxBorrowedTime.toString().toLong()

                    // Delete books, which max time borrowed + 14 days as reserve has expired
                    if (parser.plusDays(maxBorrowedTime + DAYS_TO_STORE_BOOK_TO_LATE_RETURN).isBefore(today)){

                        // Get path of book image from database
                        val currentPath = db.from(EntityBook)
                            .select(EntityBook.picture)
                            .where {
                                EntityBook.bookId eq book.bookId!!
                            }
                            .map {
                                it[EntityBook.picture]
                            }
                            .singleOrNull()

                        // Delete book from database
                        val rowsAffected = db.delete(EntityBook)
                        {
                            it.bookId eq book.bookId!!
                        }

                        if (rowsAffected == 0) {
                            // If book was not found, respond with error message
                            call.respond(
                                HttpStatusCode.NotFound,
                                GenericResponse(
                                    isSuccess = false,
                                    data = "Daná kniha nebyla nalezena"
                                )
                            )
                        } else {
                            // If book was deleted, delete also image on server side
                            val currentFile = File("/${System.getProperty("user.dir")}/$currentPath")
                            currentFile.delete()
                        }
                    // Send return late book notifications to all owners of expired book max borrowed time limit
                    } else if ((parser.plusDays(maxBorrowedTime).isBefore(today)) || (parser.plusDays(maxBorrowedTime).isEqual(today))) {
                        // Create return late book notification for owner of the book
                        val timeExpiredNotification = db.insertAndGenerateKey(EntityUserNotifications)
                        {
                            set(it.bookId, book.bookId)
                            set(it.firstUserId, book.borrowedFrom)
                            set(it.secondUserId, book.borrowedTo)
                            set(it.notificationId, TIME_LIMIT_EXPIRED)
                            val timestamp = Timestamp(System.currentTimeMillis())
                            set(it.arrivalDate, timestamp.toString())
                        } as Int
                        if (timeExpiredNotification > 0) {
                            // Send return late book notification to owner of the book
                            val status = sendNotification(TIME_LIMIT_EXPIRED, db, book.borrowedFrom!!)
                            if (status != "Success") {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně posláno")
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně vytvořeno")
                            )
                        }
                    }
                }
                /* END OF TIME_LIMIT_NOTIFICATION SECTION */

                val dateOfBirth = getDateOfBirth(db, userId)

                // Receive all books if logged-in user is an adult, else return only books, that are for all age categories
                val bookList = db.from(EntityBook)
                    .innerJoin(EntityUser, on = EntityBook.borrowedFrom eq EntityUser.userId)
                    .select()
                    .where {
                        (EntityBook.accessibility eq "pro vsechny") or
                                ((EntityBook.accessibility eq "nad 18") and (dateOfBirth <= LocalDate.now().minusYears(18)))
                    }
                    // Order by unborrowed books, and then by borrowed books, those books order by owner average score, and lastly order by book name
                    .orderBy(EntityBook.borrowedDate.isNull().desc(), EntityUser.score.desc(), EntityBook.name.asc())
                    .map {
                        BookCard(
                            bookId = it[EntityBook.bookId],
                            name = it[EntityBook.name],
                            author = it[EntityBook.author],
                            price = it[EntityBook.price],
                            picture = encodeImageToBase64(it[EntityBook.picture]),
                            borrowedDate = it[EntityBook.borrowedDate],
                            maxBorrowedTime = it[EntityBook.maxBorrowedTime],
                            username = it[EntityUser.username],
                            score = it[EntityUser.score]
                        )
                    }

                if (bookList.isNotEmpty())
                    // If book list isn't empty, respond with success message and bookList
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = bookList)
                    )
                else
                    call.respond(
                        HttpStatusCode.NotFound,
                        GenericResponse(isSuccess = true, data = null)
                    )
            }
        }

        /**
         * Books search endpoint for getting books from database by searched book name and sending them to user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/books/{bookName}")
            {
                val bearerToken = call.request.headers["Authorization"]
                    ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val dateOfBirth = getDateOfBirth(db, userId)

                // Extract the searched word/s from the request parameters and convert it to a String
                val bookName = call.parameters["bookName"]
                val searchedBookNameStr = bookName.toString()

                // Roughly same approach as getting all books, but now we are only showing those books, that contains string from search bar
                val bookListWithSearchedWordInName = db.from(EntityBook)
                    .innerJoin(EntityUser, on = EntityBook.borrowedFrom eq EntityUser.userId)
                    .select()
                    .where {
                        (((EntityBook.accessibility eq "pro vsechny") or
                                ((EntityBook.accessibility eq "nad 18") and
                                        (dateOfBirth <= LocalDate.now().minusYears(18)))) and
                                        ((searchedBookNameStr eq EntityBook.name) or
                                                (EntityBook.name like "%$searchedBookNameStr%")))
                    }
                    .orderBy(EntityBook.borrowedDate.isNull().desc(),
                        EntityUser.score.desc(), EntityBook.name.asc())
                    .map {
                        BookCard(
                            bookId = it[EntityBook.bookId],
                            name = it[EntityBook.name],
                            author = it[EntityBook.author],
                            price = it[EntityBook.price],
                            picture = encodeImageToBase64(it[EntityBook.picture]),
                            borrowedDate = it[EntityBook.borrowedDate],
                            maxBorrowedTime = it[EntityBook.maxBorrowedTime],
                            username = it[EntityUser.username],
                            score = it[EntityUser.score]
                        )
                    }

                if (bookListWithSearchedWordInName.isNotEmpty())
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = bookListWithSearchedWordInName)
                    )
                else
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = true, data = "Taková kniha zde není")
                    )
            }
        }

        /**
         * Books filter endpoint for getting books from database based on user-defined filter settings and sending them to user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/books/filter") //TODO finish it
            {
                val bearerToken = call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val dateOfBirth = getDateOfBirth(db, userId)
                // Extract all filter settings from the request parameters
                val price = call.parameters["cena"]?.toInt() ?: -1
                val author = call.parameters["autor"] ?: ""
                val listOfGenres = call.parameters.getAll("zanr")
                val emptyGenreList = call.parameters["zanr"] == ""
                val listOfBorrowOptions = call.parameters.getAll("moznosti_pujceni")
                val emptyBorrowOptionsList = call.parameters["moznosti_pujceni"] == ""
                val listOfAvailability = call.parameters.getAll("dostupnost")
                val emptyAvailabilityList = call.parameters["dostupnost"] == ""
                var unborrowed = false
                var borrowed = false

                // Set variables to true, if specified String are contained in listOfAvailability
                if (!emptyAvailabilityList) {
                    unborrowed = "Volné" in listOfAvailability!!
                    borrowed = "Vypůjčené" in listOfAvailability
                }

                // Roughly same approach as getting all books, but now we are only showing those books, that are matching filter settings
                val bookListWithAppliedFilterSettings = db.from(EntityBook)
                    .innerJoin(EntityUser, on = EntityBook.borrowedFrom eq EntityUser.userId)
                    .innerJoin(EntityBookGenre, on = EntityBook.bookId eq EntityBookGenre.bookId)
                    .innerJoin(EntityGenre, on = EntityBookGenre.genreId eq EntityGenre.genreId)
                    .innerJoin(EntityBookOptions, on = EntityBook.bookId eq EntityBookOptions.bookId)
                    .innerJoin(EntityOptions, on = EntityBookOptions.optionId eq EntityOptions.optionId)
                    .select()
                    .where {
                        (((EntityBook.accessibility eq "pro vsechny") or
                                ((EntityBook.accessibility eq "nad 18") and (dateOfBirth <= LocalDate.now().minusYears(18)))) and
                                (EntityBook.price lessEq price) and
                                (EntityBook.author like "%$author%") and
                                ((emptyGenreList) or (EntityGenre.genre inList listOfGenres!!)) and
                                ((emptyBorrowOptionsList) or (EntityOptions.option inList listOfBorrowOptions!!)) and
                                ((emptyAvailabilityList) or (((unborrowed) and (EntityBook.borrowedDate.isNull())) or ((borrowed) and (EntityBook.borrowedDate.isNotNull())))))
                    }
                    .orderBy(EntityBook.borrowedDate.isNull().desc(), EntityUser.score.desc(), EntityBook.name.asc())
                    .map {
                        BookCard(
                            bookId = it[EntityBook.bookId],
                            name = it[EntityBook.name],
                            author = it[EntityBook.author],
                            price = it[EntityBook.price],
                            picture = encodeImageToBase64(it[EntityBook.picture]),
                            borrowedDate = it[EntityBook.borrowedDate],
                            maxBorrowedTime = it[EntityBook.maxBorrowedTime],
                            username = it[EntityUser.username],
                            score = it[EntityUser.score]
                        )
                    }
                    .distinct()

                if (bookListWithAppliedFilterSettings.isNotEmpty())
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = bookListWithAppliedFilterSettings)
                    )
                else
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = true, data = "Taková kniha zde není")
                    )
            }
        }

        /**
         * Delivery point endpoint for getting delivery point of logged-in user from database.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/book/deliveryPoint") {
                val bearerToken = call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Find out if user have handover place on his profile information and store it if yes
                val user = db.from(EntityUser)
                    .select()
                    .where {
                        EntityUser.userId eq userId
                    }
                    .map {
                        User(
                            placeHandover = it[EntityUser.placeHandover]
                        )
                    }
                    .firstOrNull()

                if(user != null)
                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = user)
                    )
                else
                    call.respond(
                        HttpStatusCode.NotFound,
                        GenericResponse(isSuccess = true, data = "Uživatel nebyl nalezen.")
                    )
            }
        }

        /**
         * Book endpoint for inserting book into database by logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book")
            {
                val bearerToken = call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Retrieve book details from the request body
                val book: Book = call.receive()

                val bookPicture = decodeBookPhotoFromBase64AndStoreIt(book.picture!!, userId)
                try {
                    // Insert book into EntityBook
                    val newBookId = db.insertAndGenerateKey(EntityBook)
                    {
                        set(it.borrowedFrom, userId)
                        set(it.name, book.name)
                        set(it.picture, bookPicture)
                        set(it.author, book.author)
                        set(it.condition, book.condition)
                        set(it.info, book.info)
                        set(it.price, book.price)
                        set(it.accessibility, book.accessibility)
                        set(it.maxBorrowedTime, book.maxBorrowedTime)
                    } as Int
                    if (newBookId > 0) {
                        // Insert all selected genres into EntityBookGenre
                        val genresInDatabase = db.from(EntityGenre)
                            .select(EntityGenre.genreId)
                            .where(EntityGenre.genre inList book.genre!!)
                            .map { it[EntityGenre.genreId] }
                        var check : Int? = null
                        var check2 : Int? = null
                        for (genreId in genresInDatabase) {
                            check = db.insert(EntityBookGenre) {
                                set(it.bookId, newBookId)
                                set(it.genreId, genreId)
                            }
                        }

                        // Insert all selected book borrow options into EntityOptions
                        val borrowOptionsInDatabase = db.from(EntityOptions)
                            .select(EntityOptions.optionId)
                            .where(EntityOptions.option inList book.option!!)
                            .map { it[EntityOptions.optionId] }
                        for (optionId in borrowOptionsInDatabase) {
                            check2 = db.insert(EntityBookOptions) {
                                set(it.bookId, newBookId)
                                set(it.optionId, optionId)
                            }
                        }

                        if(book.place != "") {
                            // If logged-in user changed handover place, then update it (all his books will have changed handover place)
                            db.update(EntityUser)
                            {
                                set(it.placeHandover, book.place)
                                where {
                                    (it.userId eq userId)
                                }
                            }
                        }
                        if(check != null && check2 != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                GenericResponse(isSuccess = true, data = null)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GenericResponse(isSuccess = false, data = "Chyba při přidávání žánru, nebo možnosti zapůjčení")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            GenericResponse(isSuccess = false, data = "Chyba při přidávání knihy")
                        )
                    }

                } catch (e: Exception) {
                    // If something went wrong, then delete the book photo from server side, that we created earlier
                    val currentFile = File("/${System.getProperty("user.dir")}/$bookPicture")
                    currentFile.delete()
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = "Chyba při přidávání knihy")
                    )
                }


            }
        }

        /**
         * Book edit endpoint for editing book in database by logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            put("/book/{bookId}")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                // Extract ID of book, we want to edit from the request parameter
                val bookIdStr = call.parameters["bookId"]
                val bookId = bookIdStr?.toInt() ?: -1

                val book: Book = call.receive()

                // Store path of original book image, in case of updating it
                val currentPath = db.from(EntityBook)
                    .select(EntityBook.picture)
                    .where {
                        EntityBook.bookId eq bookId
                    }
                    .map {
                        it[EntityBook.picture]
                    }
                    .singleOrNull()

                val currentPictureEncoded = encodeImageToBase64(currentPath)

                // Update the specified book
                val noOfRowsAffected = db.update(EntityBook)
                {
                    set(it.name, book.name)
                    set(it.author, book.author)
                    set(it.condition, book.condition)
                    set(it.info, book.info)
                    set(it.price, book.price)
                    set(it.accessibility, book.accessibility)
                    set(it.maxBorrowedTime, book.maxBorrowedTime)
                    if (book.picture != null && book.picture != currentPictureEncoded) {
                        set(it.picture, decodeBookPhotoFromBase64AndStoreIt(book.picture, userId))
                        // Delete the original picture, because it is updated with new one
                        val currentFile = File("/${System.getProperty("user.dir")}/$currentPath")
                        currentFile.delete()
                    }
                    where {
                        it.bookId eq bookId
                    }
                }

                if(book.place != "" && book.option!!.contains("Osobní předání")) {
                    // Update handover place, when person borrow option is selected
                    db.update(EntityUser)
                    {
                        set(it.placeHandover, book.place)
                        where {
                            (it.userId eq userId)
                        }
                    }
                }

                if (noOfRowsAffected > 0) {
                    // Delete all genres that were in database to this book before
                    db.delete(EntityBookGenre)
                    {
                        it.bookId eq bookId
                    }
                    // Set new genres to this book in database
                    val genresInDatabase = db.from(EntityGenre)
                        .select(EntityGenre.genreId)
                        .where(EntityGenre.genre inList book.genre!!)
                        .map { it[EntityGenre.genreId] }
                    var check : Int? = null
                    var check2 : Int? = null
                    for (genreId in genresInDatabase) {
                        check = db.insert(EntityBookGenre) {
                            set(it.bookId, bookId)
                            set(it.genreId, genreId)
                        }
                    }

                    // Delete all borrow book options that were in database to this book before
                    db.delete(EntityBookOptions)
                    {
                        it.bookId eq bookId
                    }
                    // Set new book borrow options to this book in database
                    val borrowOptionsInDatabase = db.from(EntityOptions)
                        .select(EntityOptions.optionId)
                        .where(EntityOptions.option inList book.option!!)
                        .map { it[EntityOptions.optionId] }
                    for (optionId in borrowOptionsInDatabase) {
                        check2 = db.insert(EntityBookOptions) {
                            set(it.bookId, bookId)
                            set(it.optionId, optionId)
                        }
                    }

                    if(check != null && check2 != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(isSuccess = true, data = null)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            GenericResponse(isSuccess = false, data = "Chyba při aktualizaci žánru, nebo možnosti zapůjčení")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        GenericResponse(
                            isSuccess = false,
                            data = "Daná kniha nebyla nalezena."
                        )
                    )
                }
            }
        }

        /**
         * Book information endpoint for getting closer information about selected book and sending them to logged-in user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            get("/book/{bookId}")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val authorizationToken = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(authorizationToken)

                val bookIdStr = call.parameters["bookId"]
                val bookId = bookIdStr?.toInt() ?: -1

                // Store all genres this book has into List
                val genres = db.from(EntityBookGenre)
                    .innerJoin(EntityGenre, on = EntityBookGenre.genreId eq EntityGenre.genreId)
                    .select(EntityGenre.genre)
                    .where { EntityBookGenre.bookId eq bookId }
                    .groupBy(EntityGenre.genre)
                    .map { it[EntityGenre.genre] }

                // Store all book borrow options this book has into List
                val borrowOptions = db.from(EntityBookOptions)
                    .innerJoin(EntityOptions, on = EntityBookOptions.optionId eq EntityOptions.optionId)
                    .select(EntityOptions.option)
                    .where { EntityBookOptions.bookId eq bookId }
                    .groupBy(EntityBookOptions.bookId, EntityOptions.option)
                    .map { it[EntityOptions.option] }

                // Store information about book, but also some information, that will be used to change the GUI of application
                val bookInfo = db.from(EntityBook)
                    .innerJoin(EntityUser, on = EntityBook.borrowedFrom eq EntityUser.userId)
                    .select()
                    .where {
                        (bookId eq EntityBook.bookId)
                    }
                    .map {
                        BookInfo(
                            name = it[EntityBook.name],
                            picture = encodeImageToBase64(it[EntityBook.picture]),
                            author = it[EntityBook.author],
                            borrowedFrom = it[EntityBook.borrowedFrom],
                            borrowedTo = it[EntityBook.borrowedTo],
                            userName = it[EntityUser.username],
                            profilePicture = encodeImageToBase64(it[EntityUser.profilePic]),
                            score = it[EntityUser.score],
                            condition = it[EntityBook.condition],
                            info = it[EntityBook.info],
                            price = it[EntityBook.price],
                            accessibility = it[EntityBook.accessibility],
                            place = it[EntityUser.placeHandover],
                            maxBorrowedTime = it[EntityBook.maxBorrowedTime],
                            genres = genres,
                            borrowOptions = borrowOptions,
                            // Set true if the book is logged-in user to borrow, or he has it borrowed from someone else
                            myBook = it[EntityBook.borrowedFrom] == userId || it[EntityBook.borrowedTo] == userId,
                        )
                    }
                    .firstOrNull()

                if (bookInfo != null) {
                    // Set true if logged-in user is already requesting the book to borrow it or is waiting for it to be available
                    bookInfo.requesting = db.from(EntityBook)
                        .leftJoin(EntityUserNotifications, on = EntityBook.bookId eq EntityUserNotifications.bookId)
                        .leftJoin(EntityUserBook, on = EntityBook.bookId eq EntityUserBook.bookId)
                        .select()
                        .where {
                            ((bookId eq EntityBook.bookId) and (((EntityUserNotifications.notificationId eq BORROW_BOOK) and (EntityUserNotifications.secondUserId eq userId)) or (EntityUserBook.userId eq userId)))
                        }
                        .map {
                            true
                        }
                        .firstOrNull()

                    // Set true if logged-in user is waiting for book to be available
                    bookInfo.requestingNotification = db.from(EntityBook)
                        .leftJoin(EntityUserNotifications, on = EntityBook.bookId eq EntityUserNotifications.bookId)
                        .leftJoin(EntityUserBook, on = EntityBook.bookId eq EntityUserBook.bookId)
                        .select()
                        .where {
                            (bookId eq EntityBook.bookId) and (EntityUserBook.userId eq userId) and (EntityBook.borrowedTo.isNull())
                        }
                        .map {
                            true
                        }
                        .firstOrNull()

                    if (bookInfo.requesting == null) bookInfo.requesting = false
                    if (bookInfo.requestingNotification == null) bookInfo.requestingNotification = false

                    call.respond(
                        HttpStatusCode.OK,
                        GenericResponse(isSuccess = true, data = bookInfo)
                    )
                } else
                    call.respond(
                        HttpStatusCode.NotFound,
                        GenericResponse(isSuccess = false, data = "Taková kniha zde není")
                    )
            }
        }
        /**
         * Book borrow endpoint for logged-in user asking owner of the book to borrow it.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book/{bookId}/borrow") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val authorizationToken = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(authorizationToken)

                val bookIdStr = call.parameters["bookId"]
                val bookId = bookIdStr?.toInt() ?: -1

                // Check if book is already borrowed
                val bookIsAlreadyBorrowed = db.from(EntityBook)
                    .select()
                    .where {
                        (EntityBook.bookId eq bookId) and (EntityBook.borrowedTo.isNotNull())
                    }
                    .map {
                        true
                    }
                    .firstOrNull()

                // This scenario could occur, if users were in "clickwar" for borrow book once notification about availability came.
                // Buttons wouldn't change, because fragment wouldn't be updated.
                if (bookIsAlreadyBorrowed != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        GenericResponse(isSuccess = false, data = "Kniha již byla bohužel vypůjčena.")
                    )
                } else {
                    // Owner of the book
                    val userToNotify = db.from(EntityBook)
                        .select()
                        .where {
                            EntityBook.bookId eq bookId
                        }
                        .map {
                            it[EntityBook.borrowedFrom]
                        }
                        .firstOrNull()

                    // If book that logged-in user wants to borrow has mail to post as checked borrow option
                    val needPsc = db.from(EntityBook)
                        .innerJoin(EntityBookOptions, on = EntityBook.bookId eq EntityBookOptions.bookId)
                        .select()
                        .where {
                            ((EntityBook.bookId eq bookId) and (EntityBookOptions.optionId eq MAIL_TO_POST))
                        }
                        .map {
                            true
                        }
                        .firstOrNull()

                    val havePsc = db.from(EntityUser)
                        .select()
                        .where {
                            ((EntityUser.userId eq userId) and (EntityUser.psc.isNotNull()))
                        }
                        .map {
                            true
                        }
                        .firstOrNull()

                    // Check if user who wants to borrow a book, must have a PSC on his contacts or no
                    if (needPsc == true && havePsc == null)
                        call.respond(
                            HttpStatusCode.Forbidden,
                            GenericResponse(isSuccess = false, data = "Musíte mít na profilu PSČ!")
                        )
                    else {
                        // User is valid, so here is created notification and sent to owner, so he could reject or accept borrow request
                        if (userToNotify != null) {
                            val borrowNotification = db.insertAndGenerateKey(EntityUserNotifications)
                            {
                                set(it.bookId, bookId)
                                set(it.firstUserId, userToNotify)
                                set(it.secondUserId, userId)
                                set(it.notificationId, BORROW_BOOK)

                                val timestamp = Timestamp(System.currentTimeMillis())
                                set(it.arrivalDate, timestamp.toString())
                            } as Int

                            if (borrowNotification > 0) {
                                val status = sendNotification(BORROW_BOOK, db, userToNotify)

                                if (status == "Success") {
                                    // If there is record in EntityUserBook of type book availability, then delete it
                                    db.delete(EntityUserBook)
                                    {
                                        (it.bookId eq bookId) and (it.userId eq userId)
                                    }

                                    call.respond(
                                        HttpStatusCode.OK,
                                        GenericResponse(isSuccess = true, data = null)
                                    )
                                } else {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně odesláno.")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Notify me endpoint for logged-in user who wants to be notified, once the book is available again.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book/{bookId}/notifyMe") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val authorizationToken = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(authorizationToken)

                val bookIdStr = call.parameters["bookId"]
                val bookId = bookIdStr?.toInt() ?: -1

                // Create record in EntityUserBook, which is a joining table that stores users and books they want
                val notifyMeNotification = db.insertAndGenerateKey(EntityUserBook)
                {
                    set(it.bookId, bookId)
                    set(it.userId, userId)
                } as Int
                if (notifyMeNotification > 0) {
                        call.respond(
                            HttpStatusCode.OK,
                            GenericResponse(isSuccess = true, data = null)
                        )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(isSuccess = false, data = "Nelze požádat o oznámení.")
                    )
                }
            }
        }

        /**
         * Return soon endpoint for returning book before max. time limit expired
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book/{bookId}/returnSoon")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val isReturned: IsReturned = call.receive()

                // Get ID of the book, we want to return
                val bookId = call.parameters["bookId"]?.toIntOrNull()
                if (bookId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    // If the max borrow time for book already expired, then tell user, that he should return book in notifications
                    val isBookBorrowExpired = db.from(EntityUserNotifications)
                        .select()
                        .where {
                            (EntityUserNotifications.bookId eq bookId) and (EntityUserNotifications.notificationId eq TIME_LIMIT_EXPIRED)
                        }
                        .map {
                            true
                        }
                        .firstOrNull() ?: false

                    if (isBookBorrowExpired) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            GenericResponse(
                                isSuccess = false,
                                data = "Knize vypršel časový limit, prosím vyřiďte vrácení knihy v oznámeních."
                            )
                        )
                    } else {
                        // Get ID of the borrower of the specified book
                        val borrowerId = db.from(EntityBook)
                            .select()
                            .where {
                                EntityBook.bookId eq bookId
                            }
                            .map {
                                it[EntityBook.borrowedTo]
                            }
                            .firstOrNull()

                        // Get name of the specified book
                        val bookName = db.from(EntityBook)
                            .select()
                            .where {
                                EntityBook.bookId eq bookId
                            }
                            .map {
                                it[EntityBook.name]
                            }
                            .firstOrNull()

                        if (borrowerId != null && bookName != null) {
                            // Now we can return book by setting borrowedTo and borrowedDate to null
                            val noOfRowsAffected = db.update(EntityBook)
                            {
                                set(it.borrowedTo, null)
                                set(it.borrowedDate, null)
                                where {
                                    it.bookId eq bookId
                                }
                            }

                            // Now that users aren't borrowing books between each other, we can delete contacts notifications
                            val rowsAffected = db.delete(EntityUserNotifications)
                            {
                                (((it.notificationId eq CONTACT_BORROWER) or (it.notificationId eq CONTACT_OWNER)) and (it.bookId eq bookId))
                            }

                            if (noOfRowsAffected > 0 && rowsAffected > 0) {
                                // Evaluate only borrower if the book was never borrowed
                                val evaluateBorrowerNotification = db.insertAndGenerateKey(EntityUserNotifications)
                                {
                                    set(it.bookName, bookName)
                                    set(it.firstUserId, userId)
                                    set(it.secondUserId, borrowerId)
                                    set(it.notificationId, EVALUATE_BORROWER)
                                    val timestamp = Timestamp(System.currentTimeMillis())
                                    set(it.arrivalDate, timestamp.toString())
                                } as Int
                                if (evaluateBorrowerNotification > 0) {
                                    // Secondly evaluate owner AND borrower if the book was returned
                                    if (isReturned.isReturned) {
                                        val evaluateOwnerNotification = db.insertAndGenerateKey(EntityUserNotifications)
                                        {
                                            set(it.bookName, bookName)
                                            set(it.firstUserId, borrowerId)
                                            set(it.secondUserId, userId)
                                            set(it.notificationId, EVALUATE_OWNER)
                                            val timestamp = Timestamp(System.currentTimeMillis())
                                            set(it.arrivalDate, timestamp.toString())
                                        } as Int
                                        if (evaluateOwnerNotification > 0) {
                                            // Send evaluation notifications to both users
                                            val status = sendNotification(EVALUATE_OWNER, db, borrowerId)
                                            val status2 = sendNotification(EVALUATE_BORROWER, db, userId)
                                            if (status != "Success" || status2 != "Success") {
                                                call.respond(
                                                    HttpStatusCode.BadRequest,
                                                    GenericResponse(
                                                        isSuccess = false,
                                                        data = "Oznámení nebylo úspěšně posláno."
                                                    )
                                                )
                                            }
                                        } else {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                GenericResponse(
                                                    isSuccess = false,
                                                    data = "Oznámení nebylo úspěšně vytvořeno."
                                                )
                                            )
                                        }
                                    } else {
                                        // Send evaluation notification only to owner of the book
                                        val status = sendNotification(EVALUATE_BORROWER, db, userId)
                                        if (status != "Success") {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                GenericResponse(
                                                    isSuccess = false,
                                                    data = "Oznámení nebylo úspěšně posláno"
                                                )
                                            )
                                        }
                                    }

                                    val listOfUsersToNotifyAboutAvailability = db.from(EntityUserBook)
                                        .select()
                                        .where {
                                            EntityUserBook.bookId eq bookId
                                        }
                                        .map {
                                            it[EntityUserBook.userId]
                                        }

                                    if (listOfUsersToNotifyAboutAvailability.isNullOrEmpty()) {
                                        // List is empty, which is okay, there doesn't have to be anyone who wants the book
                                        call.respond(
                                            HttpStatusCode.OK,
                                            GenericResponse(
                                                isSuccess = true,
                                                data = null
                                            )
                                        )
                                    } else {
                                        // Create notifications that book is available and send them to all users that requested it
                                        for (user in listOfUsersToNotifyAboutAvailability) {
                                            val availableBookNotification =
                                                db.insertAndGenerateKey(EntityUserNotifications)
                                                {
                                                    set(it.bookId, bookId)
                                                    set(it.firstUserId, user)
                                                    set(it.secondUserId, userId)
                                                    set(it.notificationId, BOOK_IS_NOW_AVAILABLE)
                                                    val timestamp = Timestamp(System.currentTimeMillis())
                                                    set(it.arrivalDate, timestamp.toString())
                                                } as Int
                                            if (availableBookNotification > 0) {
                                                val status = sendNotification(BOOK_IS_NOW_AVAILABLE, db, user!!)

                                                if (status != "Success") {
                                                    call.respond(
                                                        HttpStatusCode.BadRequest,
                                                        GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně odesláno.")
                                                    )
                                                }
                                            } else {
                                                call.respond(
                                                    HttpStatusCode.BadRequest,
                                                    GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně vytvořeno.")
                                                )
                                            }
                                        }
                                        call.respond(
                                            HttpStatusCode.OK,
                                            GenericResponse(isSuccess = true, data = null)
                                        )
                                    }
                                } else {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně vytvořeno.")
                                    )
                                }
                            } else {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    GenericResponse(isSuccess = false, data = "Kniha se nepodařila vrátit.")
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                GenericResponse(isSuccess = false, data = "Půjčující knihy nebyl nalezen.")
                            )
                        }
                    }
                }
            }
        }

        /**
         * Return late endpoint for logged-in user that got notification, in which he is supposed to say if the specified book was returned to him or not.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book/{bookId}/returnLate")
            {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val token = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(token)

                val isReturned: IsReturned = call.receive()

                val bookId = call.parameters["bookId"]?.toIntOrNull()
                if (bookId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    val borrowerId = db.from(EntityBook)
                        .select()
                        .where {
                            EntityBook.bookId eq bookId
                        }
                        .map {
                            it[EntityBook.borrowedTo]
                        }
                        .firstOrNull()

                    val bookName = db.from(EntityBook)
                        .select()
                        .where {
                            EntityBook.bookId eq bookId
                        }
                        .map {
                            it[EntityBook.name]
                        }
                        .firstOrNull()

                    if (borrowerId != null && bookName != null) {
                        val rowsAffected = if (isReturned.isReturned) {
                            // If owner checked, that book is returned, then by updating borrowedTo and borrowedDate to null, book will be available to users again
                            db.update(EntityBook)
                            {
                                set(it.borrowedTo, null)
                                set(it.borrowedDate, null)
                                where {
                                    it.bookId eq bookId
                                }
                            }
                        } else {
                            // Owner checked, that book wasn't returned, that means book is going to be deleted from database
                            val bookPhotoCurrentPath = db.from(EntityBook)
                                .select(EntityBook.picture)
                                .where {
                                    EntityBook.bookId eq bookId
                                }
                                .map {
                                    it[EntityBook.picture]
                                }
                                .singleOrNull()

                            val noOfRowsAffected = db.delete(EntityBook)
                            {
                                it.bookId eq bookId
                            }

                            if (noOfRowsAffected == 0) {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    GenericResponse(
                                        isSuccess = false,
                                        data = "Daná kniha nebyla nalezena"
                                    )
                                )
                            } else {
                                // Also delete book photo in server side
                                val currentFile = File("/${System.getProperty("user.dir")}/$bookPhotoCurrentPath")
                                currentFile.delete()
                            }
                            noOfRowsAffected
                        }

                        var noOfRowsAffected = 1
                        if (isReturned.isReturned) {
                            // Now that book is returned, delete contacts notifications between those two users
                            noOfRowsAffected = db.delete(EntityUserNotifications)
                            {
                                (((it.notificationId eq CONTACT_BORROWER) or (it.notificationId eq CONTACT_OWNER)) and (it.bookId eq bookId))
                            }
                        }

                        if (rowsAffected > 0 && noOfRowsAffected > 0) {
                            // Delete notification with type TIME_LIMIT_EXPIRED (return book late) for specified book
                            val noRowsAffected = if (isReturned.isReturned) {
                                db.delete(EntityUserNotifications)
                                {
                                    it.recordId eq isReturned.recordId
                                }
                            } else {
                                1 // Book was not returned, that means we must continue in code and sent evaluation notification to owner
                            }
                            if (noRowsAffected > 0) {
                                // Evaluate only borrower if the book was never returned
                                val evaluateBorrowerNotification =
                                    db.insertAndGenerateKey(EntityUserNotifications)
                                    {
                                        set(it.bookName, bookName)
                                        set(it.firstUserId, userId)
                                        set(it.secondUserId, borrowerId)
                                        set(it.notificationId, EVALUATE_BORROWER)
                                        val timestamp = Timestamp(System.currentTimeMillis())
                                        set(it.arrivalDate, timestamp.toString())
                                    } as Int
                                if (evaluateBorrowerNotification > 0) {
                                    // Secondly evaluate owner AND borrower if the book was returned
                                    if (isReturned.isReturned) {
                                        val evaluateOwnerNotification = db.insertAndGenerateKey(EntityUserNotifications)
                                        {
                                            //set(it.bookId, bookId)
                                            set(it.bookName, bookName)
                                            set(it.firstUserId, borrowerId)
                                            set(it.secondUserId, userId)
                                            set(it.notificationId, EVALUATE_OWNER)
                                            val timestamp = Timestamp(System.currentTimeMillis())
                                            set(it.arrivalDate, timestamp.toString())
                                        } as Int
                                        if (evaluateOwnerNotification > 0) {
                                            // Send both of them notifications
                                            val status = sendNotification(EVALUATE_OWNER, db, borrowerId)
                                            val status2 = sendNotification(EVALUATE_BORROWER, db, userId)
                                            if (status != "Success" || status2 != "Success") {
                                                call.respond(
                                                    HttpStatusCode.BadRequest,
                                                    GenericResponse(
                                                        isSuccess = false,
                                                        data = "Oznámení nebylo úspěšně posláno."
                                                    )
                                                )
                                            } else {
                                                val listOfUsersToNotifyAboutAvailability = db.from(EntityUserBook)
                                                    .select()
                                                    .where {
                                                        EntityUserBook.bookId eq bookId
                                                    }
                                                    .map {
                                                        it[EntityUserBook.userId]
                                                    }

                                                // List is empty, which is okay, we can respond successfully here
                                                if (listOfUsersToNotifyAboutAvailability.isNullOrEmpty()) {
                                                    call.respond(
                                                        HttpStatusCode.OK,
                                                        GenericResponse(
                                                            isSuccess = false,
                                                            data = null
                                                        )
                                                    )
                                                } else {
                                                    for (user in listOfUsersToNotifyAboutAvailability) {
                                                        // Create notifications that book is now available
                                                        val availableBookNotification =
                                                            db.insertAndGenerateKey(EntityUserNotifications)
                                                            {
                                                                set(it.bookId, bookId)
                                                                set(it.firstUserId, user)
                                                                set(it.secondUserId, userId)
                                                                set(it.notificationId, BOOK_IS_NOW_AVAILABLE)
                                                                val timestamp = Timestamp(System.currentTimeMillis())
                                                                set(it.arrivalDate, timestamp.toString())
                                                            } as Int
                                                        if (availableBookNotification > 0) {
                                                            // Send those notifications to all users, who wanted to be notified
                                                            val status3 = sendNotification(BOOK_IS_NOW_AVAILABLE, db, user!!)

                                                            if (status3 != "Success") {
                                                                call.respond(
                                                                    HttpStatusCode.BadRequest,
                                                                    GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně odesláno.")
                                                                )
                                                            }
                                                        } else {
                                                            call.respond(
                                                                HttpStatusCode.BadRequest,
                                                                GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně vytvořeno.")
                                                            )
                                                        }
                                                    }
                                                    call.respond(
                                                        HttpStatusCode.OK,
                                                        GenericResponse(isSuccess = true, data = null)
                                                    )
                                                }
                                            }
                                        } else {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                GenericResponse(
                                                    isSuccess = false,
                                                    data = "Oznámení nebylo úspěšně vytvořeno."
                                                )
                                            )
                                        }
                                    } else {
                                        // Book vas never returned, send evaluation only to owner
                                        val status = sendNotification(EVALUATE_BORROWER, db, userId)
                                        if (status != "Success") {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                GenericResponse(
                                                    isSuccess = false,
                                                    data = "Oznámení nebylo úspěšně posláno"
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
                                } else {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně vytvořeno.")
                                    )
                                }
                            } else {
                                call.respond(
                                    HttpStatusCode.NotFound,
                                    GenericResponse(isSuccess = false, data = "Oznámení nebylo nalezeno.")
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                GenericResponse(isSuccess = false, data = "Oznámení se neodstranila.")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(isSuccess = false, data = "Půjčující knihy nebyl nalezen.")
                        )
                    }
                }
            }
        }

        /**
         * Answer endpoint for logged-in user who have been asked if he borrows one of his book to user.
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            post("/book/{bookId}/answer") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val authorizationToken = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(authorizationToken)

                val answer: IsConfirmed = call.receive()

                // Book ID that is being requested for borrow
                val bookId = call.parameters["bookId"]?.toIntOrNull()
                if (bookId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    // User ID who is requesting the book from owner
                    val borrowerId = db.from(EntityUserNotifications)
                        .select()
                        .where {
                            (EntityUserNotifications.recordId eq answer.recordId)
                        }
                        .map {
                            it[EntityUserNotifications.secondUserId]
                        }
                        .singleOrNull()

                    if (borrowerId != null) {
                        val rowsAffected = if (answer.isConfirmed) {
                            // Delete all notifications with type BORROW_BOOK or BOOK_IS_NOW_AVAILABLE for this book
                            db.delete(EntityUserNotifications)
                            {
                                (it.bookId eq bookId) and ((it.notificationId eq BORROW_BOOK) or (it.notificationId eq BOOK_IS_NOW_AVAILABLE))
                            }
                        } else {
                            // Delete notification with type BORROW_BOOK for this book only for borrower (because he has been rejected)
                            db.delete(EntityUserNotifications)
                            {
                                (it.recordId eq answer.recordId)
                            }

                        }
                        if (rowsAffected == 0) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                GenericResponse(
                                    isSuccess = false,
                                    data = "Daná notifikace s žádostí o půjčku knihy nebyla nalezena"
                                )
                            )
                        } else if (rowsAffected > 0 && answer.isConfirmed) {
                            // Owner accepted the offer, so update borrowedTo and borrowedDate in database
                            val noOfRowsAffected = db.update(EntityBook)
                            {
                                set(it.borrowedTo, borrowerId)
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val borrowedDate = LocalDate.now().format(formatter)
                                set(it.borrowedDate, borrowedDate)
                                where {
                                    it.bookId eq bookId
                                }
                            }
                            if (noOfRowsAffected > 0) {
                                // Create notifications in DB so that users can contact each other
                                val contactOwnerNotification = db.insertAndGenerateKey(EntityUserNotifications)
                                {
                                    set(it.bookId, bookId)
                                    set(it.firstUserId, borrowerId)
                                    set(it.secondUserId, userId)
                                    set(it.notificationId, CONTACT_OWNER)
                                    val timestamp = Timestamp(System.currentTimeMillis())
                                    set(it.arrivalDate, timestamp.toString())
                                } as Int

                                val contactBorrowerNotification = db.insertAndGenerateKey(EntityUserNotifications)
                                {
                                    set(it.bookId, bookId)
                                    set(it.firstUserId, userId)
                                    set(it.secondUserId, borrowerId)
                                    set(it.notificationId, CONTACT_BORROWER)
                                    val timestamp = Timestamp(System.currentTimeMillis())
                                    set(it.arrivalDate, timestamp.toString())
                                } as Int

                                // Send contacts notification to owner and borrower to contact each other
                                if (contactOwnerNotification > 0 && contactBorrowerNotification > 0) {
                                    val status = sendNotification(CONTACT_OWNER, db, borrowerId)
                                    val status2 = sendNotification(CONTACT_BORROWER, db, userId)

                                    if (status == "Success" && status2 == "Success") {
                                        call.respond(
                                            HttpStatusCode.OK,
                                            GenericResponse(isSuccess = true, data = null)
                                        )
                                    } else {
                                        call.respond(
                                            HttpStatusCode.BadRequest,
                                            GenericResponse(
                                                isSuccess = false,
                                                data = "Oznámení nebyly úspěšně poslány."
                                            )
                                        )
                                    }
                                } else {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        GenericResponse(
                                            isSuccess = false,
                                            data = "Nastala chyba při vytváření kontaktovacích oznámení."
                                        )
                                    )
                                }
                            } else
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    GenericResponse(
                                        isSuccess = false,
                                        data = "Nastala chyba při aktualizaci knihy."
                                    )
                                )
                        } else {
                            // Create notification to borrower, that book borrow he asked for was rejected + create record in DB
                            val rejectNotification = db.insertAndGenerateKey(EntityUserNotifications)
                            {
                                set(it.bookId, bookId)
                                set(it.firstUserId, borrowerId)
                                set(it.secondUserId, userId)
                                set(it.notificationId, OWNER_DECLINED_BORROW)
                                val timestamp = Timestamp(System.currentTimeMillis())
                                set(it.arrivalDate, timestamp.toString())
                            } as Int
                            if (rejectNotification > 0) {
                                // Send to borrower rejected notification
                                val status = sendNotification(OWNER_DECLINED_BORROW, db, borrowerId)

                                if (status == "Success") {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        GenericResponse(isSuccess = true, data = null)
                                    )
                                } else {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        GenericResponse(isSuccess = false, data = "Oznámení nebylo úspěšně posláno")
                                    )
                                }
                            } else {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                    GenericResponse(
                                        isSuccess = false,
                                        data = "Nastala chyba při vytváření oznámení o odmítnutí výpůjčky."
                                    )
                                )
                            }
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(
                                isSuccess = false,
                                data = "ID půjčujícího nebylo nalezeno."
                            )
                        )
                    }
                }
            }
        }

        /**
         * Delete request endpoint for logged-in user who is either requesting for book borrow, or for notification, once the book is available
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            delete("/book/{bookId}/deleteRequest") {
                val bearerToken =
                    call.request.headers["Authorization"] ?: throw BadRequestException("No bearer token provided")
                val authorizationToken = bearerToken.removePrefix("Bearer ")
                val userId = JwtConfig.instance.extractUserId(authorizationToken)

                val bookId = call.parameters["bookId"]?.toIntOrNull()
                if (bookId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    // First check if logged-in user is requesting for notification, once the book is available
                    val isRequestingNotification = db.from(EntityUserBook)
                        .select()
                        .where {
                            (EntityUserBook.bookId eq bookId) and (EntityUserBook.userId eq userId)
                        }
                        .map {
                            it[EntityUserBook.waitId]
                        }
                        .singleOrNull()

                    if (isRequestingNotification == null) {
                        // Second optional check if logged-in user is requesting for book borrow
                        val isRequestingBook = db.from(EntityUserNotifications)
                            .select()
                            .where {
                                (EntityUserNotifications.bookId eq bookId) and (EntityUserNotifications.secondUserId eq userId) and (EntityUserNotifications.notificationId eq BORROW_BOOK)
                            }
                            .map {
                                it[EntityUserNotifications.recordId]
                            }
                            .singleOrNull()

                        if (isRequestingBook == null) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                GenericResponse(
                                    isSuccess = false,
                                    data = "V databázi není žádná informace o čekání na vyhodnocení požadavku."
                                )
                            )
                        } else {
                            // Delete record about requesting borrow book notification and send response
                            val rowsAffected = db.delete(EntityUserNotifications)
                            {
                                it.recordId eq isRequestingBook
                            }

                            if (rowsAffected == 0) {
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
                        }

                    } else {
                        // Delete record about requesting for notification, once book is available
                        val rowsAffected = db.delete(EntityUserBook)
                        {
                            it.waitId eq isRequestingNotification
                        }
                        // Delete also notification, that book is available
                        val noOfRowsAffected = db.delete(EntityUserNotifications)
                        {
                            (it.bookId eq bookId) and (it.notificationId eq BOOK_IS_NOW_AVAILABLE) and (it.firstUserId eq userId)
                        }

                        if (rowsAffected == 0 && noOfRowsAffected == 0) {
                            call.respond(
                                HttpStatusCode.NotFound,
                                GenericResponse(
                                    isSuccess = false,
                                    data = "Daná žádost o oznámení nebyla nalezena"
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

        /**
         * Book delete endpoint for logged-in user who wants to delete one of his book
         * User must send authentication (JWT) token, otherwise he won't be able to access this endpoint.
         */
        authenticate {
            delete("/book/{bookId}") {
                val bookId = call.parameters["bookId"]?.toIntOrNull()
                if (bookId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            isSuccess = false,
                            data = "Špatné ID knihy"
                        )
                    )
                } else {
                    val bookPhotoCurrentPath = db.from(EntityBook)
                        .select(EntityBook.picture)
                        .where {
                            EntityBook.bookId eq bookId
                        }
                        .map {
                            it[EntityBook.picture]
                        }
                        .singleOrNull()

                    // Delete book from database
                    val rowsAffected = db.delete(EntityBook) {
                        it.bookId eq bookId
                    }

                    if (rowsAffected == 0) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            GenericResponse(
                                isSuccess = false,
                                data = "Daná kniha nebyla nalezena"
                            )
                        )
                    } else {
                        // Delete book cover photo
                        val currentFile = File("/${System.getProperty("user.dir")}/$bookPhotoCurrentPath")
                        currentFile.delete()
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