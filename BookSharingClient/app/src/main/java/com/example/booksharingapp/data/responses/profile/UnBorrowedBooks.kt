/**
 * File: UnBorrowedBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called UnBorrowedBooks which contains user's unborrowed books on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's unborrowed books on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class UnBorrowedBooks (
    @field:SerializedName("id_knihy")
    val bookId : Int,
    @field:SerializedName("nazev")
    val bookName : String
) : Parcelable {
    // Constructor used for creating a UnBorrowedBooks object from a parcel
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(bookId)
        parcel.writeString(bookName)
    }

    override fun describeContents(): Int {
        return 0
    }

    // Companion object used for creating instances of the Parcelable interface
    companion object CREATOR : Parcelable.Creator<UnBorrowedBooks> {
        override fun createFromParcel(parcel: Parcel): UnBorrowedBooks {
            return UnBorrowedBooks(parcel)
        }

        override fun newArray(size: Int): Array<UnBorrowedBooks?> {
            return arrayOfNulls(size)
        }
    }
}