/**
 * File: BooksToReturn.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BooksToReturn which contains user's books to return on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's books to return on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BooksToReturn (
    @field:SerializedName("id_knihy")
    val bookId : Int,
    @field:SerializedName("nazev")
    val bookName : String,
    @field:SerializedName("pocet_dni")
    var days : Int
) : Parcelable {
    // Constructor used for creating a BooksToReturn object from a parcel
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(bookId)
        parcel.writeString(bookName)
        parcel.writeInt(days)
    }

    override fun describeContents(): Int {
        return 0
    }

    // Companion object used for creating instances of the Parcelable interface
    companion object CREATOR : Parcelable.Creator<BooksToReturn> {
        override fun createFromParcel(parcel: Parcel): BooksToReturn {
            return BooksToReturn(parcel)
        }

        override fun newArray(size: Int): Array<BooksToReturn?> {
            return arrayOfNulls(size)
        }
    }
}