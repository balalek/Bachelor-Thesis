/**
 * File: BookListAdapter.kt
 * Author: Martin Baláž
 * Description: This file contains Book list adapter, that is responsible for creating and displaying the view of book cards and setting them clickable.
 */

package com.example.booksharingapp.ui.home.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booksharingapp.R
import com.example.booksharingapp.data.responses.BookCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is responsible for displaying the view of book cards and setting them clickable.
 * @param bookList The list of BookCard objects to display
 * @param listener The listener to handle clicks on book cards
 */
class BookListAdapter(private val bookList: List<BookCard>, private val listener: OnBookClickListener) : RecyclerView.Adapter<BookListAdapter.BookViewHolder>() {

    /**
     * This is inner class which is responsible for holding the views that make up an individual book card.
     * It also implements View.OnClickListener to allow clicking on a book card.
     * @param itemView The view for displaying a single book card
     */
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val bookImage: ImageView = itemView.findViewById(R.id.book_image)
        val bookName: TextView = itemView.findViewById(R.id.book_name)
        val bookAuthor: TextView = itemView.findViewById(R.id.book_author)
        val bookPrice: TextView = itemView.findViewById(R.id.book_price)
        val bookAvailability: TextView = itemView.findViewById(R.id.book_availability)
        val ownerName: TextView = itemView.findViewById(R.id.owner_name)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)

        // The constructor sets the click listener on the book card view
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onBookClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Select the card view from xml files
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.main_book_cards_view, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = bookList[position]

        // Decodes the Base64-encoded picture and loads it into the bookImage ImageView using the Glide library
        val decodedBytes = Base64.getDecoder().decode(currentBook.picture)
        Glide.with(holder.itemView.context)
            .load(decodedBytes)
            .into(holder.bookImage)
        // Sets the book name, author, price, availability, owner name, and rating views in the holder to the corresponding values from the BookCard object
        holder.bookName.text = currentBook.name
        holder.bookAuthor.text = currentBook.author
        holder.bookPrice.text = if(currentBook.price == 0) "Zdarma" else "${currentBook.price} Kč/den"
        holder.bookAvailability.text = if (currentBook.borrowedDate == null) {
            "K dispozici ihned"
        } else {
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val borrowedDateParsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentBook.borrowedDate)
            calendar.time = borrowedDateParsed!!
            calendar.add(Calendar.DATE, currentBook.maxBorrowedTime)
            calendar.add(Calendar.DATE, 14) // Add 14 days in case user won't respond to late return book
            "Dostupná nejpozději od ${dateFormatter.format(calendar.time)}"
        }
        holder.ownerName.text = currentBook.username
        holder.ratingBar.rating = currentBook.score.toFloat()
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    /**
     * This is an interface for handling clicks on a book card.
     */
    interface OnBookClickListener {
        /**
         * This function is called when a book card is clicked.
         * @param position The position of the clicked book card
         */
        fun onBookClick(position: Int)
    }

}