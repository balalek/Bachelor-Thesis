/**
 * File: MyBooksFragment.kt
 * Author: Martin Baláž
 * Description: This file contains My books fragment, that is showing user's unborrowed books and borrowed books, for logged-in user its showing also books to return with time left.
 *              In this file is also handled books card menu clicks.
 */

package com.example.booksharingapp.ui.home.profile

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.profile.*
import com.example.booksharingapp.databinding.FragmentMyBooksBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.showErrorSnackbar
import com.example.booksharingapp.ui.showSuccessSnackbar
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is showing user's unborrowed books and borrowed books, for logged-in user its showing also books to return with time left.
 * Its also handling book card clicks, or book card menu clicks.
 */
class MyBooksFragment : BaseFragment<ProfileViewModel, FragmentMyBooksBinding, CombinedRepository>(), MyBooksListAdapter.OnBookOptionsClickListener {

    private lateinit var adapterForUnBorrowedBooks: MyBooksListAdapter
    private lateinit var adapterForBorrowedBooks: MyBooksListAdapter
    private lateinit var adapterForBooksToReturn: MyBooksListAdapter
    lateinit var unBorrowedBooksList : List<UnBorrowedBooks>
    lateinit var borrowedBooksList : List<BorrowedBooks>
    lateinit var booksToReturnList : List<BooksToReturn>
    private lateinit var recyclerViewForUnBorrowedBooks: RecyclerView
    private lateinit var recyclerViewForBorrowedBooks: RecyclerView
    private lateinit var recyclerViewForBooksToReturn: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)
        // Receive data from profile fragment
        val isCurrentUser = arguments?.getBoolean("isCurrentUser") ?: false

        // Get the instance of parent fragment ViewModel
        viewModel = (requireParentFragment() as ProfileFragment).getProfileViewModel()

        unBorrowedBooksList = viewModel.books.value ?: emptyList()
        borrowedBooksList = viewModel.borrowedBooks.value ?: emptyList()
        booksToReturnList = viewModel.booksToReturn.value ?: emptyList()

        // Recycler View for unborrowed books
        recyclerViewForUnBorrowedBooks = view.findViewById(R.id.books_list)
        recyclerViewForUnBorrowedBooks.layoutManager = LinearLayoutManager(activity)
        // Recycler View for borrowed books
        recyclerViewForBorrowedBooks = view.findViewById(R.id.borrowed_books_list)
        recyclerViewForBorrowedBooks.layoutManager = LinearLayoutManager(activity)
        // Recycler View for books to return
        recyclerViewForBooksToReturn = view.findViewById(R.id.borrowed_books_to_return_list)
        recyclerViewForBooksToReturn.layoutManager = LinearLayoutManager(activity)

        checkEmptyBooksFragment(unBorrowedBooksList, borrowedBooksList, booksToReturnList)

        // Create adapter instance for unborrowed books
        adapterForUnBorrowedBooks = MyBooksListAdapter(
            null,
            unBorrowedBooksList,
            null,
            1,
            isCurrentUser,
            requireContext(),
            object : MyBooksListAdapter.OnBooksClickListener {
                override fun onBooksClick(position: Int) {
                    val clickedItem = unBorrowedBooksList[position]
                    // On book card click navigate to corresponding BookDetailsFragment
                    val action =
                        ProfileFragmentDirections.actionNavProfileToBookDetailsFragment(
                            clickedItem.bookId
                        )
                    findNavController().navigate(action)
                }
            }, this,
            viewModel)
        // Set the RecyclerView's adapter to the newly created adapter with the unborrowed books list
        recyclerViewForUnBorrowedBooks.adapter = adapterForUnBorrowedBooks

        // Create adapter instance for borrowed books
        adapterForBorrowedBooks = MyBooksListAdapter(
            borrowedBooksList,
            null,
            null,
            2,
            isCurrentUser,
            requireContext(),
            object : MyBooksListAdapter.OnBooksClickListener {
                override fun onBooksClick(position: Int) {
                    val clickedItem = borrowedBooksList[position]
                    val action =
                        ProfileFragmentDirections.actionNavProfileToBookDetailsFragment(
                            clickedItem.bookId
                        )
                    findNavController().navigate(action)
                }
            }, this,
            viewModel)
        recyclerViewForBorrowedBooks.adapter = adapterForBorrowedBooks

        // Create adapter instance for books to return
        adapterForBooksToReturn = MyBooksListAdapter(
            null,
            null,
            booksToReturnList,
            3,
            isCurrentUser,
            requireContext(),
            object : MyBooksListAdapter.OnBooksClickListener {
                override fun onBooksClick(position: Int) {
                    val clickedItem = booksToReturnList[position]
                    val action =
                        ProfileFragmentDirections.actionNavProfileToBookDetailsFragment(
                            clickedItem.bookId
                        )
                    findNavController().navigate(action)
                }
            }, this,
            viewModel)
        recyclerViewForBooksToReturn.adapter = adapterForBooksToReturn
    }
    // This companion object is responsible for creating an instance of the MyBooksFragment
    companion object {
        /**
         * This function creates instance of MyBooksFragment with applied arguments.
         * @param isCurrentUser A Boolean value, that says whether profile belongs to logged-in user
         * @return A new instance of the MyBooksFragment
         */
        fun newInstance(isCurrentUser: Boolean) =
            MyBooksFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isCurrentUser", isCurrentUser)
                }
            }
    }

    /**
     * This function is checking if user have any books borrowed (from him, or to him) and showing corresponding message if he doesn't have any.
     * Also each category has its name, and it is only shown, when there is some book in that category.
     * @param unBorrowedBooksList The list of user's unborrowed books
     * @param borrowedBooksList The list of user's borrowed books
     * @param booksToReturn The list of user's books to return
     */
    private fun checkEmptyBooksFragment(unBorrowedBooksList: List<UnBorrowedBooks>, borrowedBooksList: List<BorrowedBooks>, booksToReturn: List<BooksToReturn>) {
        // Hide all category names
        binding.borrowedBooks.isVisible = false
        binding.nonBorrowedBooks.isVisible = false
        binding.borrowedBooksToReturn.isVisible = false
        binding.booksList.isVisible = false
        binding.borrowedBooksList.isVisible = false
        binding.borrowedBooksToReturnList.isVisible = false
        binding.textForUserWithNoBooks.isVisible = false
        if (unBorrowedBooksList.isEmpty() && borrowedBooksList.isEmpty() && booksToReturn.isNullOrEmpty()) {
            // No books -> show message, that user have no books
            binding.textForUserWithNoBooks.isVisible = true
        } else {
            // Show name for categories, that have books in them
            if (unBorrowedBooksList.isNotEmpty()) {
                binding.nonBorrowedBooks.isVisible = true
                binding.booksList.isVisible = true
            }
            if (borrowedBooksList.isNotEmpty()) {
                binding.borrowedBooks.isVisible = true
                binding.borrowedBooksList.isVisible = true
            }
            if (booksToReturn.isNotEmpty()) {
                binding.borrowedBooksToReturn.isVisible = true
                binding.borrowedBooksToReturnList.isVisible = true
            }
        }
    }

    override fun onEditBookClicked(bookId: Int) {
        // Navigate user to AddOrEditBookFragment to edit selected book
        val action =
            ProfileFragmentDirections.actionNavProfileToNavAddBook(
                bookId
            )
        findNavController().navigate(action)
    }

    override fun onDeleteBookClicked(bookId: Int, position: Int) {
        // Call this function to delete selected book
        viewModel.deleteBook(bookId)
        viewModel.book.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    showSuccessSnackbar(requireView(), "Kniha byla úspěšně odstraněna.")
                    // Update UI, after book is deleted in database
                    adapterForUnBorrowedBooks.removeBook(bookId)
                    unBorrowedBooksList = viewModel.books.value ?: emptyList()
                    checkEmptyBooksFragment(viewModel.books.value ?: emptyList(), viewModel.borrowedBooks.value ?: emptyList(), viewModel.booksToReturn.value ?: emptyList())
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    showErrorSnackbar(requireView(), "Kniha nebyla odstraněna!")
                    handleApiError(it)
                    binding.progressbar.visible(false)
                }
            }
        })
    }

    override fun onSoonReturnBookClicked(bookId: Int) {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpView = inflater.inflate(R.layout.return_soon_pop_up_window, null)
        val popUpWindow = PopupWindow(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        val returnSoon = popUpView.findViewById<RadioButton>(R.id.return_soon_radio_button)
        val neverBorrowed = popUpView.findViewById<RadioButton>(R.id.never_borrowed_radio_button)
        val radioGroup = popUpView.findViewById<RadioGroup>(R.id.return_soon_radio_group)
        val confirmButton = popUpView.findViewById<Button>(R.id.confirm_btn)
        val cancelButton = popUpView.findViewById<Button>(R.id.cancel_btn)

        (requireParentFragment() as ProfileFragment).reduceVisibility()
        // Show pop-up window to return book soon
        popUpWindow.showAtLocation(binding.nonBorrowedBooks, Gravity.CENTER, 0, 0)

        // Close pop-up window
        cancelButton.setOnClickListener {
            popUpWindow.dismiss()
            (requireParentFragment() as ProfileFragment).increaseVisibility()
        }

        // Disable radio box error, after clicking on some
        returnSoon.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) returnSoon.error = null
        }

        neverBorrowed.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) returnSoon.error = null
        }

        confirmButton.setOnClickListener {
            if(radioGroup.checkedRadioButtonId == -1) {
                // Radio box wasn't selected, show error
                returnSoon.error = "Vyberte důvod dřívějšího vrácení."
                returnSoon.requestFocus()
            } else {
                var isReturned = false
                if (returnSoon.isChecked) isReturned = true
                // Call this function to return specified book soon
                viewModel.returnSoon(bookId, isReturned)
                viewModel.isReturned.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is Resource.Success -> {
                            binding.progressbar.visible(false)
                            showSuccessSnackbar(requireView(), "Kniha byla úspěšně vrácena.")
                            // Update UI, after book is returned in database -> remove book from borrowed book category
                            val moveBook = adapterForBorrowedBooks.moveBook(bookId)
                            if (moveBook != null) {
                                // Update lists, because book was moved from borrowed to borrowed
                                borrowedBooksList = viewModel.borrowedBooks.value ?: emptyList()
                                // Update UI, add the same book into unborrowed book category
                                adapterForUnBorrowedBooks.addBook(moveBook)
                                unBorrowedBooksList = viewModel.books.value ?: emptyList()
                            }
                            checkEmptyBooksFragment(viewModel.books.value ?: emptyList(), viewModel.borrowedBooks.value ?: emptyList(), viewModel.booksToReturn.value ?: emptyList())
                            // Close pop-up window
                            popUpWindow.dismiss()
                            (requireParentFragment() as ProfileFragment).increaseVisibility()
                        }
                        is Resource.Loading -> {
                            binding.progressbar.visible(true)
                        }
                        is Resource.Failure -> {
                            handleApiError(it)
                            binding.progressbar.visible(false)
                        }
                    }
                })
            }
        }

        // Close pop-up window when clicked outside of it
        popUpWindow.setOnDismissListener {
            popUpWindow.dismiss()
            (requireParentFragment() as ProfileFragment).increaseVisibility()
        }

    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMyBooksBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}