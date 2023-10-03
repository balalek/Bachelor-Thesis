/**
 * File: HomeFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Home fragment, that is showing books to logged-in user, filtered or normal.
 */

package com.example.booksharingapp.ui.home.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.Data
import com.example.booksharingapp.data.responses.Filter
import com.example.booksharingapp.databinding.FragmentHomeBinding
import com.example.booksharingapp.ui.GlobalVariables.cameFromNotifications
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.visible
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * This fragment is showing books to logged-in user, filtered or normal.
 */
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding, CombinedRepository>() {

    lateinit var adapter: BookListAdapter
    var searchViewBug = false
    // Filter setting are empty at first
    var price : Int = -1
    var author : String = ""
    var availability : String = ""
    var borrowOptions : String = ""
    private var genre : String = ""


    override fun onPause() {
        super.onPause()
        // This is preventing filter settings to be erased, when navigating through application
        view?.findViewById<SearchView>(R.id.searchView)?.setOnQueryTextListener(null)
        searchViewBug = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.book_list)
        recyclerView?.layoutManager = LinearLayoutManager(activity)

        if (cameFromNotifications) {
            // When user click on arrived notification, it will open whole application again and set price to 0, so it will only show free books, so this is used to prevent that from happening
            arguments?.putInt("price", -1)
            cameFromNotifications = false
        }
        // Check if there are stored filter settings in arguments, store them in variables if there are some
        price = if (arguments?.getInt("price") != null && arguments?.getInt("price") != -1) arguments?.getInt("price")!! else -1
        author = if (arguments?.getString("author") != null && arguments?.getString("author") != "") arguments?.getString("author")!! else ""
        genre = if (arguments?.getString("genres") != null && arguments?.getString("genres") != "") arguments?.getString("genres")!! else ""
        borrowOptions = if (arguments?.getString("options") != null && arguments?.getString("options") != "") arguments?.getString("options")!! else ""
        availability = if (arguments?.getString("availability") != null && arguments?.getString("availability") != "") arguments?.getString("availability")!! else ""

        if ((price == -1 &&
                author == "" &&
                genre == "" &&
                borrowOptions == "" &&
                availability == "") || arguments == null) {
            // Empty filter settings, show all books
            getBooks(recyclerView)
        } else {
            val filter = Filter(
                author,
                price,
                availability.split(".").toMutableList(),
                borrowOptions.split(".").toMutableList(),
                genre.split(".").toMutableList()
            )
            // Show filtered books with stored filter settings
            getFilteredBooks(recyclerView, filter)
        }

        // Swipe from up to bottom, to refresh fragment
        binding.swipeRefreshLayout.setOnRefreshListener {
            if ((price == -1 &&
                        author == "" &&
                        genre == "" &&
                        borrowOptions == "" &&
                        availability == "") || arguments == null
            ) {
                getBooks(recyclerView)
            } else {
                val filter = Filter(
                    author,
                    price,
                    availability.split(".").toMutableList(),
                    borrowOptions.split(".").toMutableList(),
                    genre.split(".").toMutableList()
                )
                getFilteredBooks(recyclerView, filter)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        // Set a listener for SearchView's query text submission event
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Once user submits his text, call this function to find book by that submitted text
                viewModel.searchBooks(query)
                // Observe the searchBooks LiveData for any changes in response to the submitted text
                viewModel.searchBooks.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is Resource.Success -> {
                            // Retrieve the filtered book list from the response data
                            val filteredBookList = it.value.data
                            // Create an adapter with the filtered book list and a listener for book clicks
                            adapter = BookListAdapter(
                                filteredBookList,
                                object : BookListAdapter.OnBookClickListener {
                                    override fun onBookClick(position: Int) {
                                        val clickedItem = filteredBookList[position]
                                        // Navigate to the BookDetailsFragment with the clicked book's ID as an argument
                                        val action =
                                            HomeFragmentDirections.actionNavToHomeToBookDetailsFragment(
                                                clickedItem.bookId
                                            )
                                        findNavController().navigate(action)
                                    }
                                })
                            // Set the RecyclerView's adapter to the newly created adapter with the filtered book list
                            recyclerView?.adapter = adapter
                            // Hide the progress bar when books are retrieved and shown successfully
                            binding.progressbar.visible(false)
                        }
                        is Resource.Loading -> {
                            // Show the progress bar while books are being retrieved
                            binding.progressbar.visible(true)
                        }
                        is Resource.Failure -> {
                            // Handle any API errors and hide the progress bar
                            handleApiError(it)
                            binding.progressbar.visible(false)
                        }
                    }
                })
                return true
            }

            // Get all books again once user empties search bar
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "" && !searchViewBug) {
                    getBooks(recyclerView)
                }
                searchViewBug = false
                return true
            }
        })

        binding.filterIcon.setOnClickListener {
            // Navigate to the FilterFragment with filter settings as an arguments
            val action = HomeFragmentDirections.actionNavToHomeToFilterFragment(price,
                    author,
                    genre,
                    borrowOptions,
                    availability)
            findNavController().navigate(action)
        }

        binding.fabAddBook.setOnClickListener {
            // Navigate to the AddOrEditBookFragment to add a new book
            findNavController().navigate(R.id.toAddBook)
        }

        // Find navigation view from the activity and set the item selection listener
        val navView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            // Create a handler to delay the closing of the drawer for 50 milliseconds
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)
            }, 50)
            // Check which item is clicked and perform the corresponding action
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    // Logout the user
                    logout()
                    true
                }
                // Navigate user to selected destination fragment
                else -> NavigationUI.onNavDestinationSelected(menuItem, navController = findNavController())
            }
        }

        // Set header (in menu) components
        val headerLayout = navView.getHeaderView(0)
        val profileImage = headerLayout.findViewById<ImageView>(R.id.profile_image)
        val profileName = headerLayout.findViewById<TextView>(R.id.user_name)
        val profileEmail = headerLayout.findViewById<TextView>(R.id.email)

        // Call this function to get header information (in menu) and observe LavaData object for any changes in response
        viewModel.getMenuInfo()
        viewModel.menu.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    updateMenuUI(it.value.data, profileImage, profileEmail, profileName)
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })
    }

    /**
     * This function gets and shows all books from server.
     * @param recyclerView The RecyclerView to show the books in
     */
    private fun getBooks(recyclerView: RecyclerView) {
        viewModel.getBooks()
        viewModel.books.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    val bookList = it.value.data
                    adapter = BookListAdapter(
                        bookList,
                        object : BookListAdapter.OnBookClickListener {
                            override fun onBookClick(position: Int) {
                                val clickedItem = bookList[position]
                                val action =
                                    // Navigate to the BookDetailsFragment with the clicked book's ID as an argument
                                    HomeFragmentDirections.actionNavToHomeToBookDetailsFragment(
                                        clickedItem.bookId
                                    )
                                findNavController().navigate(action)
                            }
                        })
                    recyclerView.adapter = adapter

                    binding.progressbar.visible(false)
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    handleApiError(it) { getBooks(recyclerView) }
                    binding.progressbar.visible(false)
                }
            }
        })
    }

    /**
     * This function gets and shows filtered books from server.
     * @param recyclerView The RecyclerView to show the books in
     * @param filter The filter settings that are sent in request to server to get filtered books in response
     */
    private fun getFilteredBooks(recyclerView: RecyclerView, filter: Filter?) {
        viewModel.getFilteredBooks(filter)
        viewModel.filteredBooks.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    val bookList = it.value.data
                    adapter = BookListAdapter(
                        bookList,
                        object : BookListAdapter.OnBookClickListener {
                            override fun onBookClick(position: Int) {
                                val clickedItem = bookList[position]
                                val action =
                                    HomeFragmentDirections.actionNavToHomeToBookDetailsFragment(
                                        clickedItem.bookId
                                    )
                                findNavController().navigate(action)
                            }
                        })
                    recyclerView.adapter = adapter

                    binding.progressbar.visible(false)
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

    /**
     * This function updates header (in menu) with logged-in user's information
     * @param user The user object that came from server as response to getMenuInfo request
     * @param profileImage The component, where will be logged-in user's image showed
     * @param profileEmail The component, where will be logged-in user's e-mail showed
     * @param profileName The component, where will be logged-in user's name showed
     */
    private fun updateMenuUI(user: Data, profileImage: ImageView, profileEmail: TextView, profileName: TextView) {

        if (user.profilePic != null) {
            // Decode Base64 encoded profile image, if there is any image
            val decodedBytes = Base64.getDecoder().decode(user.profilePic)
            // Show picture in profileImage component
            Glide.with(this)
                .load(decodedBytes)
                .into(profileImage)
        }
        // Set text to logged-in user's data
        profileEmail.text = user.email
        profileName.text = user.name

    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}