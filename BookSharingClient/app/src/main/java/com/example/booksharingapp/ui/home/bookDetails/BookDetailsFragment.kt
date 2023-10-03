/**
 * File: BookDetailsFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Book details fragment, that is showing selected book information and its also handling buttons for borrow, notify, or cancel request.
 */

package com.example.booksharingapp.ui.home.bookDetails

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.BookApi
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.BookRepository
import com.example.booksharingapp.data.responses.BookInfo
import com.example.booksharingapp.databinding.FragmentBookDetailsBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.enable
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.showErrorSnackbar
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * This fragment is either showing selected book information and its also handling buttons for borrow, notify, or cancel request.
 */
class BookDetailsFragment : BaseFragment<BookDetailsViewModel, FragmentBookDetailsBinding, BookRepository>() {

    private var isBorrowed = false
    var userId : Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.constraintLayout.isVisible = false
        binding.progressbar.visible(false)

        // Receive selected book ID, that will be used in future requests
        val bookId = arguments?.getInt("bookId")

        // Call this function to receive book information
        viewModel.getBookInfo(bookId!!)
        viewModel.bookInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    // Get ID of owner of this book
                    userId = it.value.data.borrowedFrom
                    updateUI(it.value.data)
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

        // Pop-up window for Report
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpView = inflater.inflate(R.layout.report_pop_up_window, null)
        val popUpWindow = PopupWindow(popUpView, WRAP_CONTENT, WRAP_CONTENT, true)
        val reportEditText = popUpView.findViewById<EditText>(R.id.reportAdd)
        val sendButton = popUpView.findViewById<Button>(R.id.reportButton)
        val close = popUpView.findViewById<ImageView>(R.id.closeIconReport)

        // Close pop-up window
        close.setOnClickListener {
            popUpWindow.dismiss()
            binding.parentConstraintLayout.alpha = 1f
        }

        sendButton.setOnClickListener {
            val reportText = reportEditText.text.toString()

            // TODO send report /book/{bookId}/report

            popUpWindow.dismiss()
            binding.parentConstraintLayout.alpha = 1f
        }

        // Close pop-up window when clicked outside of it
        popUpWindow.setOnDismissListener {
            popUpWindow.dismiss()
            binding.parentConstraintLayout.alpha = 1f
        }

        // Show pop-up window to report user on unappropriated book
        binding.report.setOnClickListener {
            binding.parentConstraintLayout.alpha = 0.2f
            popUpWindow.showAtLocation(binding.report, Gravity.CENTER, 0, 0)
        }

        // Pop-up window for borrowing a book
        val inflater2 = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpView2 = inflater2.inflate(R.layout.borrow_book_pop_up_window, null)
        val popUpWindow2 = PopupWindow(popUpView2, WRAP_CONTENT, WRAP_CONTENT, true)
        val borrow = popUpView2.findViewById<Button>(R.id.confirm_borrow_btn)
        val cancel = popUpView2.findViewById<Button>(R.id.cancel_borrow_btn)

        cancel.setOnClickListener {
            popUpWindow2.dismiss()
            binding.parentConstraintLayout.alpha = 1f
        }

        borrow.setOnClickListener {
            // Call this function to ask for borrow book
            viewModel.borrowBook(bookId)
            viewModel.bookToBorrow.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        switchButtons()
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

            popUpWindow2.dismiss()
            binding.parentConstraintLayout.alpha = 1f

        }

        popUpWindow2.setOnDismissListener {
            popUpWindow2.dismiss()
            binding.parentConstraintLayout.alpha = 1f
        }

        // Open Google maps on button click
        binding.googleMapBtn.setOnClickListener {
            val searchString = binding.handoverPlaceAdd.text.toString()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${searchString}"))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Error if there isn't Google maps on device
                showErrorSnackbar(requireView(), "Google mapy nejsou nainstalovány ve vašem zařízení")
            }
        }

        binding.detailsBtn.setOnClickListener {
            if(isBorrowed) {
                // If the book is borrowed to someone already, button can be used to notify logged-in user, once its available
                viewModel.notifyMe(bookId)
                viewModel.bookToNotify.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is Resource.Success -> {
                            binding.progressbar.visible(false)
                            switchButtons()
                        }
                        is Resource.Loading -> {
                            binding.progressbar.visible(true)
                        }
                        is Resource.Failure -> {
                            handleApiError(it) {viewModel.notifyMe(bookId)}
                            binding.progressbar.visible(false)
                        }
                    }
                })
            } else {
                // Show pop-up window to explain user, how book borrowing works
                binding.parentConstraintLayout.alpha = 0.2f
                popUpWindow2.showAtLocation(binding.report, Gravity.CENTER, 0, 0)
            }

        }

        // User can click on owner's layout to be navigation into his profile
        binding.clickableLinearLayoutProfile.setOnClickListener {
            val action = BookDetailsFragmentDirections.actionBookDetailsFragmentToNavProfile(userId!!)
            findNavController().navigate(action)
        }

        binding.stornoBtn.setOnClickListener {
            // User will see cancel request button, once he asked for borrow on notification, on clicking that button, this function will be called
            viewModel.deleteRequest(bookId)
            viewModel.bookDeleteRequest.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        unSwitchButtons()
                    }
                    is Resource.Loading -> {
                        binding.progressbar.visible(true)
                    }
                    is Resource.Failure -> {
                        handleApiError(it) {viewModel.notifyMe(bookId)}
                        binding.progressbar.visible(false)
                    }
                }
            })
        }

    }

    /**
     * This function fills fragment with data, and it will show correct buttons, or even no buttons, if the book is borrowed to or from logged-in user.
     * @param bookInfo The object that is containing all book details to be shown
     */
    @SuppressLint("SuspiciousIndentation")
    private fun updateUI(bookInfo: BookInfo) {
        with(binding) {
            bookName.text = bookInfo.name
            author.text = bookInfo.author

            // Show book image
            val decodedBookPicture = Base64.getDecoder().decode(bookInfo.picture)
            Glide.with(requireContext())
                .load(decodedBookPicture)
                .into(bookPicture)

            // Show image of the owner of the book
            if(bookInfo.profilePicture != null) {
                val decodedOwnerAvatar = Base64.getDecoder().decode(bookInfo.profilePicture)
                Glide.with(requireContext())
                    .load(decodedOwnerAvatar)
                    .into(ownerProfilePicture)
            }

            // Default is red circle with cross in it, if following conditions are met, change icon to green circle with checkmark
            if(bookInfo.borrowOptions.contains(getString(R.string.personal)))
            Glide.with(requireContext())
                .load(R.drawable.ic_baseline_check_circle_24)
                .into(personalHandoverCheck)

            if(bookInfo.borrowOptions.contains(getString(R.string.send_to_post)))
            Glide.with(requireContext())
                .load(R.drawable.ic_baseline_check_circle_24)
                .into(sendToPostCheck)

            if(bookInfo.price == 0)
            Glide.with(requireContext())
                .load(R.drawable.ic_baseline_check_circle_24)
                .into(freeLoanCheck)

            if(bookInfo.accessibility == "nad 18")
            Glide.with(requireContext())
                .load(R.drawable.ic_baseline_check_circle_24)
                .into(adultOnlyCheck)

            ownerName.text = bookInfo.userName

            if (bookInfo.score != null) ownerRatingBar.rating = bookInfo.score.toFloat()

            // Show handover place, if there is any
            if (bookInfo.place == null || bookInfo.place == "") {
                handoverPlace.isVisible = false
                linearLayoutHandoverPlace.isVisible = false
            } else handoverPlaceAdd.text = bookInfo.place

            addGenresToTableLayout(bookInfo.genres)

            // Show book content information, if there is any
            if (bookInfo.info == null || bookInfo.info == "") {
                bookInfoText.isVisible = false
                bookInfoAdd.isVisible = false
            } else bookInfoAdd.text = bookInfo.info

            priceAdd.text = if(bookInfo.price == 0) "Zdarma" else "${bookInfo.price} Kč/den"
            conditionAdd.text = bookInfo.condition
            accessibilityAdd.text = if(bookInfo.accessibility == "pro vsechny") "Pro všechny" else "Nad 18 let"
            maxBorrowTimeAdd.text = getString(R.string.max_borrowed_time_string, bookInfo.maxBorrowedTime)

            // Show notify button
            if(bookInfo.borrowedTo != null) {
                detailsBtn.text = getString(R.string.notify_me)
                isBorrowed = true
            }

            if(bookInfo.myBook) {
                // Hide buttons, book is mine or borrowed to me
                report.isVisible = false
                footerView.isVisible = false
            } else if (bookInfo.requestingNotification) {
                // Do nothing, you should be able to request for book borrow. But if you won´t, you will still be requesting for notification
            } else if(bookInfo.requesting) {
                switchButtons()
            }

            // After everything is loaded, show the entire content of the fragment
            constraintLayout.isVisible = true
        }
    }

    /**
     * This function is showing this book genres in pretty formatted way, it will also make sure it fits on the screen and try to fill the whole space it have.
     * If it doesn't have space, it will go to the next line.
     * @param genreList The list of genres, that this book owns
     */
    private fun addGenresToTableLayout(genreList: List<String>) {
        val flowLayout = binding.flowLayout
        flowLayout.childSpacing = 8

        // Set UI and show each genre from list formatted
        for (item in genreList) {
            val textView = TextView(requireContext())
            textView.text = item
            textView.textSize = 16F
            textView.setBackgroundResource(R.drawable.genre_border)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 10, 10, 10)
            textView.layoutParams = params

            // Add genre to FlowLayout
            flowLayout.addView(textView)
        }
    }

    /**
     * This function will show cancel button for request, that user made before. It will also disable the request button.
     */
    private fun switchButtons() {
        binding.stornoBtn.isVisible = true
        binding.detailsBtn.enable(false)
        val layoutParams = binding.detailsBtn.layoutParams
        layoutParams.width = WRAP_CONTENT
        binding.detailsBtn.layoutParams = layoutParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.footerView)

        // Clear the start constraint of the button
        constraintSet.clear(binding.detailsBtn.id, ConstraintSet.START)

        // Apply the modified constraints to the parent layout
        constraintSet.applyTo(binding.footerView)
    }

    /**
     * This function will hide cancel button for request, if user canceled the request. It will also enable the request button.
     */
    private fun unSwitchButtons() {
        binding.stornoBtn.isVisible = false
        binding.detailsBtn.enable(true)
        val dpValue = 180 // Set the desired width in dp
        val scale = resources.displayMetrics.density // Get the device's screen density
        val pixelValue = (dpValue * scale + 0.5f).toInt() // Convert dp to pixels
        val layoutParams = binding.detailsBtn.layoutParams
        layoutParams.width = pixelValue // Set the button width in pixels
        binding.detailsBtn.layoutParams = layoutParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.footerView)

        // Apply the modified constraints to the parent layout
        constraintSet.connect(binding.detailsBtn.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.applyTo(binding.footerView)
    }

    override fun getViewModel() = BookDetailsViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentBookDetailsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): BookRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(BookApi::class.java, token)
        return BookRepository(api)
    }

}