/**
 * File: AddOrEditBookFragment.kt
 * Author: Martin Baláž
 * Description: This file contains AddOrEditBook fragment, that is handling adding and editing book functionality.
 */

package com.example.booksharingapp.ui.home.addOrEditBook

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.booksharingapp.databinding.FragmentAddBookBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is handling adding and editing book functionality.
 */
class AddOrEditBookFragment : BaseFragment<AddOrEditBookViewModel, FragmentAddBookBinding, BookRepository>() {

    // Image pick request code
    private val PICK_IMAGE_REQUEST_CODE = 1

    // Selected image URI
    private var selectedImageUri: Uri? = null
    private var base64StringWithoutWhitespace: String? = null
    private var author: String = ""
    private var name: String = ""
    private var state: String = ""
    private var price: Int = 0
    private var info: String = ""
    private var accessibility: String = ""
    private var maxBorrowedTime: Int = 0
    private var handoverPlace: String = ""
    private var borrowOptions: MutableList<String> = mutableListOf()
    private var selectedGenres: MutableList<String> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)
        // If there is bookId in arguments, it means user wants to edit the book, otherwise add one
        val bookId = arguments?.getInt("bookId")

        /* EDIT BOOK */
        if (bookId != null) {
            // Get all original information on the book
            viewModel.getBookInfo(bookId)
            viewModel.bookInfo.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        binding.invisibleLayout.isVisible = true
                        updateUI(it.value.data)
                    }
                    is Resource.Loading -> {
                        binding.progressbar.visible(true)
                    }
                    is Resource.Failure -> {
                        handleApiError(it) { viewModel.getBookInfo(bookId) }
                        binding.progressbar.visible(false)
                    }
                }
            })
        /* ADD BOOK */
        } else {
            viewModel.getDeliveryPlace()
            viewModel.deliveryPlace.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        binding.invisibleLayout.isVisible = true
                        // Set only place to handover, if user has any
                        binding.deliveryPlaceAdd.setText(it.value.data.placeHandover)
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

        // Select and show picture of book cover
        binding.addPhotoButton.setOnClickListener {
            openImagePicker()
        }

        // Put checked borrow options into list or remove unchecked ones from the list
        binding.personal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show delivery place edit text, when personal handover is checked
                binding.deliveryPlaceAdd.visibility = View.VISIBLE
                binding.deliveryPlaceText.visibility = View.VISIBLE
                binding.borrowOptionsText.error = null
                borrowOptions.add(getString(R.string.personal))
            } else {
                borrowOptions.remove(getString(R.string.personal))
                binding.deliveryPlaceAdd.visibility = View.GONE
                binding.deliveryPlaceText.visibility = View.GONE
            }
        }

        binding.mail.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.borrowOptionsText.error = null
                borrowOptions.add(getString(R.string.mail_it))
            } else borrowOptions.remove(getString(R.string.mail_it))
        }

        // Show price edit text, if corresponding radio box is set
        binding.priceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.priceFreeRadioButton.id -> {
                    binding.priceListLayout.visibility = View.GONE
                }
                binding.priceListRadioButton.id -> {
                    binding.priceListLayout.visibility = View.VISIBLE
                }
            }
        }

        // Can't focus on text views
        binding.genresText.setOnClickListener {
            binding.genresText.isFocusable = false
            binding.genresText.isFocusableInTouchMode = false
        }

        binding.borrowOptionsText.setOnClickListener {
            binding.borrowOptionsText.isFocusable = false
            binding.borrowOptionsText.isFocusableInTouchMode = false
        }

        binding.priceText.setOnClickListener {
            binding.priceText.isFocusable = false
            binding.priceText.isFocusableInTouchMode = false
        }

        binding.AccessText.setOnClickListener {
            binding.AccessText.isFocusable = false
            binding.AccessText.isFocusableInTouchMode = false
        }

        /* GENRES */
        // Add or remove genre to/from list depending on checked checkbox
        binding.povidka.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.povidka))
            } else selectedGenres.remove(getString(R.string.povidka))
        }

        binding.roman.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.roman))
            } else selectedGenres.remove(getString(R.string.roman))
        }

        binding.biografie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.biografie))
            } else selectedGenres.remove(getString(R.string.biografie))
        }

        binding.poezie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.poezie))
            } else selectedGenres.remove(getString(R.string.poezie))
        }

        binding.drama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.drama))
            } else selectedGenres.remove(getString(R.string.drama))
        }

        binding.publicistika.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.publicistika))
            } else selectedGenres.remove(getString(R.string.publicistika))
        }

        binding.komiks.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.komiks))
            } else selectedGenres.remove(getString(R.string.komiks))
        }

        binding.literatura.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.literatura_pro_deti_a_mladez))
            } else selectedGenres.remove(getString(R.string.literatura_pro_deti_a_mladez))
        }

        binding.baje.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.baje_myty_a_povesti))
            } else selectedGenres.remove(getString(R.string.baje_myty_a_povesti))
        }

        binding.divciRomany.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.divci_roman))
            } else selectedGenres.remove(getString(R.string.divci_roman))
        }

        binding.fantasy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.fantasy))
            } else selectedGenres.remove(getString(R.string.fantasy))
        }

        binding.sciFi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.scifi))
            } else selectedGenres.remove(getString(R.string.scifi))
        }

        binding.detektivka.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.detektivka))
            } else selectedGenres.remove(getString(R.string.detektivka))
        }

        binding.romanProZeny.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.roman_pro_zeny))
            } else selectedGenres.remove(getString(R.string.roman_pro_zeny))
        }

        binding.cestopis.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.cestopis))
            } else selectedGenres.remove(getString(R.string.cestopis))
        }

        binding.humor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.humor_a_satira))
            } else selectedGenres.remove(getString(R.string.humor_a_satira))
        }

        binding.literaturaFaktu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.literatura_faktu))
            } else selectedGenres.remove(getString(R.string.literatura_faktu))
        }

        binding.erotika.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.erotika))
            } else selectedGenres.remove(getString(R.string.erotika))
        }

        binding.horror.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.genresText.error = null
                selectedGenres.add(getString(R.string.horror))
            } else selectedGenres.remove(getString(R.string.horror))
        }

        // Hide error messages once radio box is set
        binding.priceFreeRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.priceText.error = null
        }

        binding.priceListRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.priceText.error = null
        }

        binding.accessAdultRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.AccessText.error = null
        }

        binding.accessAllRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.AccessText.error = null
        }

        // Handle button click
        binding.addBookBtn.setOnClickListener {
            if(validateInput()) {

                if (selectedImageUri != null) {
                    // Added image, or replaced old one
                    val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                    val imageBytes = inputStream?.readBytes()
                    val base64String = imageBytes?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                    base64StringWithoutWhitespace = base64String?.replace("\\s".toRegex(), "")
                    inputStream?.close()
                }

                if (bookId != null) {
                    // Call this function when on Edit Fragment (there is bookId in arguments)
                    viewModel.putBook(
                        bookId,
                        name,
                        base64StringWithoutWhitespace!!,
                        author,
                        state,
                        info,
                        price,
                        accessibility,
                        maxBorrowedTime,
                        selectedGenres.toList(),
                        borrowOptions.toList(),
                        handoverPlace
                    )
                } else {
                    // Call this function when on Add Fragment (there isn't bookId in arguments)
                    viewModel.postBook(
                        name,
                        base64StringWithoutWhitespace!!,
                        author,
                        state,
                        info,
                        price,
                        accessibility,
                        maxBorrowedTime,
                        selectedGenres.toList(),
                        borrowOptions.toList(),
                        handoverPlace
                    )
                }
                viewModel.postOrPutBook.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is Resource.Success -> {
                            binding.progressbar.visible(false)
                            // Navigate back once add/edit is successful
                            findNavController().navigateUp()
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

    }

    /**
     * This function is validating input for add/edit book, that is user attempting to submit using button.
     * @return True if validating is successful, or false if validation failed
     */
    private fun validateInput(): Boolean {
        if(binding.imageView.drawable.constantState == ContextCompat.getDrawable(requireContext(), R.drawable.placeholder_book_cover)?.constantState){
            // Check if there is image that isn't the placeholder
            binding.scrollView.smoothScrollTo(0, 0)
            binding.errorIcon.isVisible = true
            return false
        }

        if (binding.bookNameAdd.text.isBlank()) {
            // Empty name
            binding.bookNameAdd.error = "Zadejte název knihy"
            binding.bookNameAdd.requestFocus()
            return false
        }

        if (binding.bookNameAdd.text.toString().trim().length > 100) {
            // Too long name
            binding.bookNameAdd.error = "Zadejte kratší název knihy"
            binding.bookNameAdd.requestFocus()
            return false
        }

        if (binding.authorNameAdd.text.isBlank()) {
            binding.authorNameAdd.error = "Zadejte jméno autora"
            binding.authorNameAdd.requestFocus()
            return false
        }

        if (binding.authorNameAdd.text.toString().trim().length > 100) {
            binding.authorNameAdd.error = "Zadejte kratší jméno autora"
            binding.authorNameAdd.requestFocus()
            return false
        }

        if (binding.bookStateAdd.text.isBlank()) {
            binding.bookStateAdd.error = "Zadejte stav knihy"
            binding.bookStateAdd.requestFocus()
            return false
        }

        if (binding.bookStateAdd.text.toString().trim().length > 50) {
            binding.bookStateAdd.error = "Zadejte kratší stav knihy"
            binding.bookStateAdd.requestFocus()
            return false
        }

        if (binding.bookInfoAdd.text.toString().isNotBlank()) {
            if (binding.bookInfoAdd.text.toString().length > 1000) {
                // Too long content, if there is any
                binding.bookInfoAdd.error = "Zadejte kratší popis knihy"
                binding.bookInfoAdd.requestFocus()
                return false
            }
        }

        if (!binding.povidka.isChecked && !binding.roman.isChecked && !binding.biografie.isChecked && !binding.poezie.isChecked && !binding.drama.isChecked && !binding.sciFi.isChecked && !binding.fantasy.isChecked && !binding.publicistika.isChecked && !binding.komiks.isChecked && !binding.baje.isChecked && !binding.divciRomany.isChecked && !binding.detektivka.isChecked && !binding.romanProZeny.isChecked && !binding.cestopis.isChecked && !binding.humor.isChecked && !binding.literaturaFaktu.isChecked && !binding.erotika.isChecked && !binding.literatura.isChecked && !binding.horror.isChecked) {
            // no genre selected
            binding.genresText.error = "Zadejte žánr/y knihy"
            binding.genresText.requestFocus()
            return false
        }

        if(!binding.personal.isChecked && !binding.mail.isChecked) {
            binding.borrowOptionsText.error = "Zadejte možnosti zapůjčení"
            binding.borrowOptionsText.requestFocus()
            return false
        }

        if(binding.personal.isChecked && binding.deliveryPlaceAdd.text.isBlank()) {
            binding.deliveryPlaceAdd.error = "Zadejte místo předání"
            binding.deliveryPlaceAdd.requestFocus()
            return false
        }

        if(binding.personal.isChecked && binding.deliveryPlaceAdd.text.toString().trim().length > 100) {
            binding.deliveryPlaceAdd.error = "Zadejte kratší místo předání"
            binding.deliveryPlaceAdd.requestFocus()
            return false
        }

        if(binding.priceRadioGroup.checkedRadioButtonId == -1) {
            // No radio box selected
            binding.priceText.error = "Vyberte cenu výpůjčky"
            binding.priceText.requestFocus()
            return false
        }

        if(binding.priceListRadioButton.isChecked && binding.priceListEditText.text.isBlank()) {
            binding.priceListEditText.error = "Zadejte cenu výpůjčky"
            binding.priceListEditText.requestFocus()
            return false
        }

        if(binding.priceListRadioButton.isChecked) {
            if (!checkNumbersRange(1, 100)) return false
        }

        if(binding.accessRadioGroup.checkedRadioButtonId == -1) {
            binding.AccessText.error = "Zadejte přístupnost"
            binding.AccessText.requestFocus()
            return false
        }

        if(binding.maxBorrowLengthAdd.text.isBlank()) {
            binding.maxBorrowLengthAdd.error = "Zadejte max. délku zapůjčení"
            binding.maxBorrowLengthAdd.requestFocus()
            return false
        }

        if(!checkNumbersRange(7, 365)) return false

        // Add values to variables
        name = binding.bookNameAdd.text.toString().trim()
        author = binding.authorNameAdd.text.toString().trim()
        state = binding.bookStateAdd.text.toString().trim()
        info = if (binding.bookInfoAdd.text.isNotBlank()) binding.bookInfoAdd.text.toString().trim() else ""
        price = if (binding.priceListRadioButton.isChecked) binding.priceListEditText.text.toString().toInt() else 0
        accessibility = if (binding.accessAdultRadioButton.isChecked) "nad 18" else "pro vsechny"
        maxBorrowedTime = binding.maxBorrowLengthAdd.text.toString().toInt()
        handoverPlace = if (binding.personal.isChecked) binding.deliveryPlaceAdd.text.toString().trim() else ""
        return true
    }

    /**
     * This function is checking range of max borrowed time being in range 7-365 and price in 0-100.
     * @param firstNumber The number that correspond to beginning of the range
     * @param secondNumber The number that correspond to ending of the range
     * @return True if everything is in range, or false if values are out of range
     */
    private fun checkNumbersRange(firstNumber: Int, secondNumber: Int) : Boolean {
        if(firstNumber == 7 && secondNumber == 365) {
            // Check max borrow time range
            if(binding.maxBorrowLengthAdd.text.toString().toInt() < firstNumber || binding.maxBorrowLengthAdd.text.toString().toInt() > secondNumber) {
                binding.maxBorrowLengthAdd.error = "Zadejte hodnotu v rozmezí 7-365"
                binding.maxBorrowLengthAdd.requestFocus()
                return false
            }
        } else {
            // Check price range
            if(binding.priceListEditText.text.toString().toInt() < firstNumber || binding.priceListEditText.text.toString().toInt() > secondNumber) {
                binding.priceListEditText.error = "Zadejte hodnotu v rozmení 1-100"
                binding.priceListEditText.requestFocus()
                return false
            }
        }
        return true
    }

    /**
     * This function will open the image picker to select an image
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    // Handle result of image pick intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            updateImageView()
        }
    }

    /**
     * This function updates the image view with the selected image.
     */
    private fun updateImageView() {
        binding.imageView.visibility = View.VISIBLE
        binding.imageView.setImageURI(selectedImageUri)
        binding.errorIcon.isVisible = false
    }

    /**
     * This function fills with data all the fields, that were populated on add book or previous edit.
     * @param bookInfo The object that is containing all book details to be shown
     */
    private fun updateUI(bookInfo: BookInfo) {
        // Show picture using Glide
        base64StringWithoutWhitespace = bookInfo.picture
        val decodedBytes = java.util.Base64.getDecoder().decode(base64StringWithoutWhitespace)
        Glide.with(this)
            .load(decodedBytes)
            .into(binding.imageView)

        name = bookInfo.name
        binding.bookNameAdd.setText(name)
        author = bookInfo.author
        binding.authorNameAdd.setText(author)
        state = bookInfo.condition
        binding.bookStateAdd.setText(state)
        info = bookInfo.info ?: ""
        binding.bookInfoAdd.setText(info)

        // Create map of genres
        val checkboxGenreMap = mapOf(
            binding.povidka to R.string.povidka,
            binding.roman to R.string.roman,
            binding.biografie to R.string.biografie,
            binding.poezie to R.string.poezie,
            binding.drama to R.string.drama,
            binding.publicistika to R.string.publicistika,
            binding.komiks to R.string.komiks,
            binding.literatura to R.string.literatura_pro_deti_a_mladez,
            binding.baje to R.string.baje_myty_a_povesti,
            binding.divciRomany to R.string.divci_roman,
            binding.fantasy to R.string.fantasy,
            binding.sciFi to R.string.scifi,
            binding.detektivka to R.string.detektivka,
            binding.romanProZeny to R.string.roman_pro_zeny,
            binding.cestopis to R.string.cestopis,
            binding.humor to R.string.humor_a_satira,
            binding.literaturaFaktu to R.string.literatura_faktu,
            binding.erotika to R.string.erotika,
            binding.horror to R.string.horror
        )

        // Find selected genres from list in map, and check them
        for (entry in checkboxGenreMap) {
            if (bookInfo.genres.contains(getString(entry.value))) {
                entry.key.isChecked = true
            }
        }

        // Create map of borrow options
        val checkboxBorrowOptionsMap = mapOf(
            binding.personal to R.string.personal,
            binding.mail to R.string.mail_it
        )

        // Find selected borrow options from list in map, and check them
        for (entry in checkboxBorrowOptionsMap) {
            if (bookInfo.borrowOptions.contains(getString(entry.value))) {
                entry.key.isChecked = true
            }
        }

        handoverPlace = bookInfo.place ?: ""
        binding.deliveryPlaceAdd.setText(handoverPlace)

        // Check price radio box
        price = bookInfo.price
        if (price == 0) binding.priceFreeRadioButton.isChecked = true
        else {
            binding.priceListRadioButton.isChecked = true
            binding.priceListEditText.setText(price.toString())
        }

        // Check availability box
        accessibility = bookInfo.accessibility
        if (getString(R.string.adult_only) == accessibility) binding.accessAdultRadioButton.isChecked = true
        else binding.accessAllRadioButton.isChecked = true

        maxBorrowedTime = bookInfo.maxBorrowedTime
        binding.maxBorrowLengthAdd.setText(maxBorrowedTime.toString())
    }

    override fun getViewModel() = AddOrEditBookViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddBookBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): BookRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(BookApi::class.java, token)
        return BookRepository(api)
    }

}