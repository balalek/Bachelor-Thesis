/**
 * File: MyBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyBooks which contains user's books on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
/**
 * This data class represents user's books on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyBooks (
    @field:SerializedName("volne_knihy")
    val unborrowedBooks: List<UnBorrowedBooks>,
    @field:SerializedName("vypujcene_knihy")
    val borrowedBooks:  List<BorrowedBooks>,
    @field:SerializedName("knihy_k_navraceni")
    var booksToReturn: List<BooksToReturn>
) : Parcelable
