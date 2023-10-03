/**
 * File: ContactsFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Contacts fragment, that is either showing user's contacts data (if he have something borrowed with logged-in user),
 *              or its handling change contacts for logged-in user.
 */

package com.example.booksharingapp.ui.home.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.profile.MyContacts
import com.example.booksharingapp.databinding.FragmentContactsBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.showSuccessSnackbar
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * This fragment is either showing user's contacts data (if he have something borrowed with logged-in user), or its handling change contacts for logged-in user.
 */
class ContactsFragment: BaseFragment<ProfileViewModel, FragmentContactsBinding, CombinedRepository>() {

    // Image pick request code
    private val PICK_IMAGE_REQUEST_CODE = 1

    // Selected image URI
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.progressbar.visible(false)
        // Receive data from profile fragment
        val contacts: MyContacts? = arguments?.getParcelable("contacts")
        val isCurrentUser = arguments?.getBoolean("isCurrentUser") ?: false

        if (isCurrentUser) {
            // Show editable fields with filled data
            binding.myContactsLayout.isVisible = true
            binding.otherUserContactsLayout.isVisible = false

            if (!contacts?.myProfilePicture.isNullOrEmpty()) {
                val decodedBytes = Base64.getDecoder().decode(contacts!!.myProfilePicture)
                Glide.with(this)
                    .load(decodedBytes)
                    .into(binding.myProfileImage)
            }

            binding.myUsernameAdd.setText(contacts?.myUserName)
            if (contacts?.myScore != null) binding.reviewRatingBar.rating = contacts.myScore.toFloat()
            binding.mySecondEmailAdd.setText(contacts?.secondEmail)
            if (contacts?.handoverPlace != null) binding.myHandoverPlaceAdd.setText(contacts.handoverPlace)
            if (contacts?.psc != 0) binding.myPostalCodeAdd.setText(contacts?.psc.toString())
            if (contacts?.phoneNumber != 0) binding.myPhoneAdd.setText(contacts?.phoneNumber.toString())

            // Add profile picture
            binding.addMyPhotoButton.setOnClickListener {
                openImagePicker()
            }

            binding.storeContactsBtn.setOnClickListener {
                if(validateInput()) {
                    var base64StringWithoutWhitespace: String? = null
                    if (selectedImageUri != null) {
                        // Encode profile picture into base64 String
                        val inputStream =
                            requireContext().contentResolver.openInputStream(selectedImageUri!!)
                        val imageBytes = inputStream?.readBytes()
                        val base64String = imageBytes?.let {
                            android.util.Base64.encodeToString(
                                it,
                                android.util.Base64.DEFAULT
                            )
                        }
                        base64StringWithoutWhitespace =
                            base64String?.replace("\\s".toRegex(), "")
                        inputStream?.close()
                    }
                    val handoverPlace = binding.myHandoverPlaceAdd.text.toString().takeIf { it.isNotEmpty() }
                    // Send contacts data to server to change/update them
                    viewModel.changeContacts(base64StringWithoutWhitespace, binding.myUsernameAdd.text.toString(), binding.mySecondEmailAdd.text.toString(), handoverPlace, binding.myPostalCodeAdd.text.toString().toIntOrNull(), binding.myPhoneAdd.text.toString().toIntOrNull())
                    viewModel.changeContactsResponse.observe(viewLifecycleOwner, Observer {
                        when (it) {
                            is Resource.Success -> {
                                binding.progressbar.visible(false)
                                showSuccessSnackbar(requireView(),"Vaše kontakty byly úspěšně změněny!")
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
        } else {
            // Everyone can see user's profile picture, his name and his average score
            binding.myContactsLayout.isVisible = false
            binding.otherUserContactsLayout.isVisible = true

            if (!contacts?.myProfilePicture.isNullOrEmpty()) {
                val decodedBytes = Base64.getDecoder().decode(contacts!!.myProfilePicture)
                Glide.with(this)
                    .load(decodedBytes)
                    .into(binding.userProfileImage)
            }

            binding.userUsername.text = contacts?.myUserName
            if (contacts?.myScore != null) binding.userReviewRatingBar.rating = contacts.myScore.toFloat()

            if (contacts?.isUserPrivileged == true) {
                // Simply show uneditable contacts information of selected user
                binding.visibleContacts.isVisible = true
                binding.invisibleContacts.isVisible = false

                binding.userEmailAdd.text = contacts?.secondEmail
                if (contacts?.handoverPlace != null)  {
                    binding.placeHandoverVisibility.isVisible = true
                    binding.userHandoverPlaceAdd.text = contacts.handoverPlace
                }
                if (contacts?.psc != 0) {
                    binding.postalCodeVisibility.isVisible = true
                    binding.userPscAdd.text = contacts?.psc.toString()
                }
                if (contacts?.phoneNumber != 0) {
                    binding.phoneNumberVisibility.isVisible = true
                    binding.userPhoneAdd.text = contacts?.phoneNumber.toString()
                }

            } else {
                // Show information, that logged-in user cannot see other user's contacts, because the ain't borrowing anything between each other
                binding.visibleContacts.isVisible = false
                binding.invisibleContacts.isVisible = true
            }
        }

    }
    // This companion object is responsible for creating an instance of the ContactsFragment
    companion object {
        /**
         * This function creates instance of ContactsFragment with applied arguments.
         * @param contacts The contacts to display in the fragment
         * @param isCurrentUser A boolean flag indicating whether the profile belongs to logged-in user
         * @return A new instance of the ContactsFragment
         */
        fun newInstance(contacts: MyContacts, isCurrentUser: Boolean) =
            ContactsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("contacts", contacts)
                    putBoolean("isCurrentUser", isCurrentUser)
                }
            }
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
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            updateImageView()
        }
    }

    /**
     * This function updates the image view with the selected image
     */
    private fun updateImageView() {
        binding.myProfileImage.visibility = View.VISIBLE
        binding.myProfileImage.setImageURI(selectedImageUri)
    }

    /**
     * This function is validating input for contacts, that is user attempting to change/update using button.
     * @return True if validating is successful, or false if validation failed
     */
    private fun validateInput(): Boolean {

        if (binding.myUsernameAdd.text.isBlank()) {
            binding.myUsernameAdd.error = "Zadejte své uživatelské jméno"
            binding.myUsernameAdd.requestFocus()
            return false
        }

        if (binding.myUsernameAdd.text.length < 4) {
            binding.myUsernameAdd.error = "Zadejte délší uživatelské jméno jak 4 znaky"
            binding.myUsernameAdd.requestFocus()
            return false
        }

        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$".toRegex()
        if (!binding.mySecondEmailAdd.text.matches(emailRegex)) {
            // Email format is invalid
            binding.mySecondEmailAdd.error = "Zadejte platný e-mail"
            binding.mySecondEmailAdd.requestFocus()
            return false
        }

        if (binding.mySecondEmailAdd.text.length > 320) {
            // Email format is too long
            binding.mySecondEmailAdd.error = "Zadejte kratší e-mail jak 320 znaků"
            binding.mySecondEmailAdd.requestFocus()
            return false
        }

        if(binding.myHandoverPlaceAdd.text.trim().length > 100) {
            binding.myHandoverPlaceAdd.error = "Zadejte kratší místo předání"
            binding.myHandoverPlaceAdd.requestFocus()
            return false
        }

        if (binding.myPostalCodeAdd.text.isNotBlank() && binding.myPostalCodeAdd.text.length != 5) {
            // Postal code must be exactly 5 numbers
            binding.myPostalCodeAdd.error = "Zadejte platné poštovní směrovací číslo"
            binding.myPostalCodeAdd.requestFocus()
            return false
        }

        if (binding.myPhoneAdd.text.isNotBlank() && (binding.myPhoneAdd.text.length != 9 || !binding.myPhoneAdd.text.all { it.isDigit() })) {
            // Phone number must be exactly 9 numbers
            binding.myPhoneAdd.error = "Zadejte platné telefonní číslo, nebo žádné"
            binding.myPhoneAdd.requestFocus()
            return false
        }

        return true
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentContactsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}