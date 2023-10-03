/**
 * File: BorrowedBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BorrowedBooks which contains user's borrowed books on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's borrowed books on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BorrowedBooks (
    @field:SerializedName("id_knihy")
    val bookId : Int,
    @field:SerializedName("nazev")
    val bookName : String
) : Parcelable {
    // Constructor used for creating a BorrowedBooks object from a parcel
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
    companion object CREATOR : Parcelable.Creator<BorrowedBooks> {
        override fun createFromParcel(parcel: Parcel): BorrowedBooks {
            return BorrowedBooks(parcel)
        }

        override fun newArray(size: Int): Array<BorrowedBooks?> {
            return arrayOfNulls(size)
        }
    }
}