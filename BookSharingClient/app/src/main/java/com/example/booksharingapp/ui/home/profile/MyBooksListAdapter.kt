/**
 * File: MyBooksListAdapter.kt
 * Author: Martin Baláž
 * Description: This file contains My books list adapter, that is responsible for creating and displaying the view of book cards and setting them clickable.
 *              It can also set menu to book cards and handle clicks on items in menu.
 */

package com.example.booksharingapp.ui.home.profile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.booksharingapp.R
import com.example.booksharingapp.data.responses.profile.BooksToReturn
import com.example.booksharingapp.data.responses.profile.BorrowedBooks
import com.example.booksharingapp.data.responses.profile.UnBorrowedBooks

/**
 * This class is responsible for creating and displaying the view of book cards and setting them clickable. It can also set menu to book cards and handle clicks on items in menu on logged-in user profile.
 * @param borrowedBooksList The list of BookCard objects to display that are borrowed by the user
 * @param unBorrowedBooksList The list of BookCard objects to display that are not borrowed by the user
 * @param booksToReturnList The list of BookCard objects to display that are to be returned
 * @param numberOfRecyclerView The unique number of the recycler view in which the adapter is set
 * @param isCurrentUser A boolean value to check if the logged-in user is on his profile
 * @param context The context of the fragment calling this adapter
 * @param listener The listener to handle clicks on book cards
 * @param menuListener The listener to handle clicks on menu options of book cards
 * @param viewModel The ViewModel to handle live data of lists
 */
class MyBooksListAdapter (
    private var borrowedBooksList: List<BorrowedBooks>?,
    private var unBorrowedBooksList: List<UnBorrowedBooks>?,
    private val booksToReturnList: List<BooksToReturn>?,
    private val numberOfRecyclerView: Int,
    private val isCurrentUser: Boolean,
    private val context: Context,
    private val listener: OnBooksClickListener,
    private val menuListener: OnBookOptionsClickListener,
    private val viewModel: ProfileViewModel
) : RecyclerView.Adapter<MyBooksListAdapter.BooksViewHolder>() {

    /**
     * This is inner class which is responsible for holding the views that make up an individual book card.
     * It also implements View.OnClickListener to allow clicking on a book card.
     * @param itemView The view for displaying a single book card
     */
    inner class BooksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val bookTitle: TextView = itemView.findViewById(R.id.book_title)
        private val bookOptions: ImageView = itemView.findViewById(R.id.book_options)
        val clockImage: ImageView = itemView.findViewById(R.id.return_img)
        val daysToReturn: TextView = itemView.findViewById(R.id.days_to_return)

        // The constructor sets the click listener on the book card view, it also sets click listener to menu dots on logged-in user profile
        init {
            itemView.setOnClickListener(this)
            if (unBorrowedBooksList != null && isCurrentUser) {
                bookOptions.isVisible = true
                bookOptions.setOnClickListener {
                    showBookOptionsPopup(bookOptions, unBorrowedBooksList!![adapterPosition], adapterPosition)
                }
            }
            if (borrowedBooksList != null && isCurrentUser) {
                bookOptions.isVisible = true
                bookOptions.setOnClickListener {
                    showBorrowedBookOptionsPopup(bookOptions, borrowedBooksList!![adapterPosition])
                }
            }
            if (booksToReturnList != null && isCurrentUser) {
                clockImage.isVisible = true
                daysToReturn.isVisible = true
            }

            // Hide menu dots, if this isn't logged-in user profile, or book list are empty
            if (!isCurrentUser || (unBorrowedBooksList == null) && (borrowedBooksList == null)) bookOptions.isVisible = false
        }

        override fun onClick(v: View?) {
            listener.onBooksClick(adapterPosition)
        }
    }

    /**
     * This function will show menu options on unborrowed books.
     * @param view The view to anchor the popup menu to
     * @param book The unborrowed book to show options for
     * @param position The position of the book in the list
     */
    private fun showBookOptionsPopup(view: View, book: UnBorrowedBooks?, position: Int) {
        // Create a new popup menu and inflate its options menu from a resource
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.book_options_menu, popup.menu)
        // Set a listener to handle clicks on the menu items
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // If the "Editovat" option was selected, notify the menu listener
                R.id.edit_book -> {
                    menuListener.onEditBookClicked(book!!.bookId)
                    true
                }
                R.id.delete_book -> {
                    // If the "Odstranit" option was selected, notify the menu listener
                    menuListener.onDeleteBookClicked(book!!.bookId, position)
                    true
                }
                else -> false
            }
        }
        // Show the popup menu
        popup.show()
    }

    /**
     * This function will show one menu option on borrowed books
     * @param view The view to anchor the popup menu to
     * @param book The borrowed book to show the "Return" option for
     */
    private fun showBorrowedBookOptionsPopup(view: View, book: BorrowedBooks?) {
        // Create a new popup menu and inflate its options menu from a resource
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.borrowed_book_options_menu, popup.menu)
        // Set a listener to handle clicks on the menu item
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.return_book -> {
                    // If the "Vrátit" option was selected, notify the menu listener
                    menuListener.onSoonReturnBookClicked(book!!.bookId)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        // Select the card view from xml files
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_books_view, parent, false)
        return BooksViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        when (numberOfRecyclerView) {
            // Set book name in book card while depending on RecyclerView type
            1 -> {
                val currentUnBorrowedBooks = unBorrowedBooksList?.get(position)
                holder.bookTitle.text = currentUnBorrowedBooks?.bookName
            }
            2 -> {
                val currentBorrowedBooks = borrowedBooksList?.get(position)
                holder.bookTitle.text = currentBorrowedBooks?.bookName
            }
            else -> {
                val currentBookToReturn = booksToReturnList?.get(position)
                holder.bookTitle.text = currentBookToReturn?.bookName

                // This is after borrow time expired for book, but before the after limit expires (14 days to answer on return notification)
                var isWhite = false
                if (currentBookToReturn!!.days - 14 >= 0) {
                    isWhite = true
                    currentBookToReturn.days = currentBookToReturn.days - 14
                }

                // Correct grammar depending on how many days left to return book
                val daysToReturnString =
                    when (currentBookToReturn.days) {
                        1 -> "${currentBookToReturn.days} den"
                        2, 3, 4 -> "${currentBookToReturn.days} dny"
                        else -> "${currentBookToReturn.days} dní"
                    }

                if (isWhite) currentBookToReturn.days = currentBookToReturn.days + 14

                // Show number of days until the book is removed from application, in red color, which means that books should have been already returned
                if (currentBookToReturn.days < 14) {
                    holder.daysToReturn.setTextColor(ContextCompat.getColor(context, R.color.notification_red));
                    holder.clockImage.setColorFilter(ContextCompat.getColor(context, R.color.notification_red), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                holder.daysToReturn.text = daysToReturnString

            }
        }
    }

    override fun getItemCount(): Int {
        // Calculate the total item count based on the lists available
        return if (borrowedBooksList != null && unBorrowedBooksList != null && booksToReturnList != null)
            borrowedBooksList!!.size + unBorrowedBooksList!!.size + booksToReturnList!!.size
        else if (borrowedBooksList != null && booksToReturnList != null)
            borrowedBooksList!!.size + booksToReturnList!!.size
        else if (unBorrowedBooksList != null && booksToReturnList != null)
            unBorrowedBooksList!!.size + booksToReturnList!!.size
        else if (borrowedBooksList != null)
            borrowedBooksList!!.size
        else if (unBorrowedBooksList != null)
            unBorrowedBooksList!!.size
        else if (booksToReturnList != null)
            booksToReturnList!!.size
        else
            0
    }

    /**
     * This function removes a book from the list of unborrowed books.
     * @param bookId The ID of the book to be removed
     */
    fun removeBook(bookId: Int) {
        val bookToRemove = unBorrowedBooksList?.firstOrNull { it.bookId == bookId }
        if (bookToRemove != null) {
            // Update adapter list
            val index = unBorrowedBooksList!!.indexOf(bookToRemove)
            unBorrowedBooksList = unBorrowedBooksList!!.toMutableList().apply { removeAt(index) }
            // Notify observers, that item was removed, to update UI
            notifyItemRemoved(index)
            // Update shared list among fragments with same ViewModel
            viewModel.updateBookList(unBorrowedBooksList!!)
        }
    }

    /**
     * This function actually removes a book from the list of borrowed books and return the book to be added into the list of unborrowed books.
     * @param bookId The ID of the book to be moved
     * @return The book object that was removed to be added into the unborrowed books list or null
     */
    fun moveBook(bookId: Int) : UnBorrowedBooks? {
        val bookToMove = borrowedBooksList?.firstOrNull {it.bookId == bookId}
        if (bookToMove != null) {
            // Update adapter list
            val index = borrowedBooksList!!.indexOf(bookToMove)
            borrowedBooksList = borrowedBooksList!!.toMutableList().apply { removeAt(index) }
            // Notify observers, that item was removed, to update UI
            notifyItemRemoved(index)
            // Update shared list among fragments with same ViewModel
            viewModel.updateBorrowedBookList(borrowedBooksList!!)

            // return removed unborrowed book object, so it could be added into borrowed books
            return UnBorrowedBooks(
                bookToMove.bookId,
                bookToMove.bookName
            )
        }
        return null
    }

    /**
     * This function adds a book to the list of unborrowed books.
     * @param bookToMove The book object to be added
     */
    fun addBook(bookToMove: UnBorrowedBooks) {
        // Update adapter list
        unBorrowedBooksList = unBorrowedBooksList!!.toMutableList().apply { add(bookToMove) }
        if (unBorrowedBooksList!!.size == 1) {
            // Notify observers, that item was added, to update UI
            notifyItemInserted(0)
        } else {
            // Notify observers, that item was changed, to update UI
            notifyItemRangeChanged(unBorrowedBooksList!!.size, itemCount)
        }
        // Update shared list among fragments with same ViewModel
        viewModel.updateBookList(unBorrowedBooksList!!)
    }

    /**
     * This is an interface for handling clicks on a book card.
     */
    interface OnBooksClickListener {
        /**
         * This function is called when a book card is clicked.
         * @param position The position of the clicked book card
         */
        fun onBooksClick(position: Int)
    }

    /**
     * This is an interface for handling menu item clicks on a book card.
     */
    interface OnBookOptionsClickListener {
        /**
         * This function is called when the "Editovat" menu item is clicked.
         * @param bookId The ID of the book being edited
         */
        fun onEditBookClicked(bookId: Int)

        /**
         * This function is called when the "Odstranit" menu item is clicked.
         * @param bookId The ID of the book being deleted
         * @param position The position of the book card being deleted
         */
        fun onDeleteBookClicked(bookId: Int, position: Int)

        /**
         * This function is called when the "Vrátit" menu item is clicked for a borrowed book.
         * @param bookId The ID of the borrowed book being returned soon
         */
        fun onSoonReturnBookClicked(bookId: Int)
    }

}