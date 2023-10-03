/**
 * File: RegisterFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Register fragment, that is handling register functionality.
 */

package com.example.booksharingapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.AuthApi
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.AuthRepository
import com.example.booksharingapp.databinding.FragmentRegisterBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.showSuccessSnackbar
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * This fragment is handling register functionality.
 */
class RegisterFragment : BaseFragment<AuthViewModel, FragmentRegisterBinding, AuthRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)

        // Observe registration response
        viewModel.registerResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it is Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    // On success navigate to login fragment, where user can log in.
                    lifecycleScope.launch {
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        showSuccessSnackbar(requireView(), "Váš účet byl úspěšně vytvořen.")
                    }
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    binding.progressbar.visible(false)
                    handleApiError(it) { register() }
                }
            }
        })

        // Listeners to erase errors in password fields once text changes
        binding.etPasswordAgainReg.editText?.addTextChangedListener {
            binding.etPasswordAgainReg.error = null
        }

        binding.etPasswordReg.editText?.addTextChangedListener {
            binding.etPasswordReg.error = null
        }

        // Navigate back to login fragment on link click
        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // On register button validate input and in success, try register through register function
        binding.btnRegister.setOnClickListener {
            if (validateInput()) {
                register()
            }
        }
    }

    /**
     * This function is validating input, that is user attempting to send to register himself using button.
     * @return True if validating is successful, or false if validation failed
     */
    private fun validateInput() : Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$".toRegex()
        if (!binding.etEmailReg.editText?.text.toString().matches(emailRegex)) {
            // Email format is not in correct format
            binding.etEmailReg.error = "Zadejte platný e-mail"
            binding.etEmailReg.requestFocus()

            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
            return false
        } else binding.etEmailReg.error = null

        if (binding.etEmailReg.editText?.text.toString().length > 320) {
            // Email is too long
            binding.etEmailReg.error = "Zadejte kratší e-mail jak 320 znaků"
            binding.etEmailReg.requestFocus()

            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
            return false
        } else binding.etEmailReg.error = null

        if (binding.etUsernameReg.editText?.text.toString().length > 25) {
            // Username is too long
            binding.etUsernameReg.error = "Zadejte kratší uživatelské jméno jak 25 znaků"
            binding.etUsernameReg.requestFocus()

            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
            return false
        } else binding.etUsernameReg.error = null

        if (binding.etUsernameReg.editText?.text.toString().length < 4) {
            // Username is too short
            binding.etUsernameReg.error = "Zadejte délší uživatelské jméno jak 4 znaky"
            binding.etUsernameReg.requestFocus()

            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
            return false
        } else binding.etUsernameReg.error = null

        // Phone number is optional, but it must contain 9 numbers
        if (binding.etPhoneReg.editText?.text.toString().isNotBlank()) {
            if (binding.etPhoneReg.editText?.text.toString().length != 9 || !binding.etPhoneReg.editText?.text.toString().all { it.isDigit() }) {
                binding.etPhoneReg.error = "Zadejte platné telefonní číslo, nebo žádné"
                binding.etPhoneReg.requestFocus()

                binding.passwordAgainError.isVisible = false
                binding.passwordError.isVisible = false
                return false
            } else binding.etPhoneReg.error = null
        } else binding.etPhoneReg.error = null

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        try {
            val dateOfBirth = LocalDate.parse(binding.etBirthdayReg.editText?.text.toString(), dateFormatter)
            if (dateOfBirth >= LocalDate.now()) {
                // Date of birth is after today
                binding.etBirthdayReg.error = "Zadejte platný datum narození"
                binding.etBirthdayReg.requestFocus()

                binding.passwordAgainError.isVisible = false
                binding.passwordError.isVisible = false
                return false
            } else binding.etBirthdayReg.error = null
        } catch (e: DateTimeParseException) {
            // Date of birth is not in the correct format
            binding.etBirthdayReg.error = "Zadejte datum narození ve správném formátu"
            binding.etBirthdayReg.requestFocus()

            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
            return false
        }

        // New password and old password ain't equal to each other
        if (binding.etPasswordReg.editText?.text.toString() != binding.etPasswordAgainReg.editText?.text.toString()) {
            // Empty password fields
            binding.etPasswordReg.editText?.text = null
            binding.etPasswordAgainReg.editText?.text = null
            // Show error
            binding.etPasswordAgainReg.error = "error"
            binding.etPasswordReg.error = "error"
            // Show error text and hide other
            binding.passwordError.isVisible = false
            binding.passwordAgainError.isVisible = true
            binding.passwordAgainError.text = requireContext().getString(R.string.password_equal)
            binding.etPasswordReg.requestFocus()
            return false
        } else {
            // Hide custom made errors
            binding.etPasswordReg.error = null
            binding.etPasswordAgainReg.error = null
            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
        }

        if (binding.etPasswordReg.editText?.text.toString().length > 30) {
            // Password is too long
            binding.etPasswordReg.editText?.text = null
            binding.etPasswordAgainReg.editText?.text = null
            binding.etPasswordReg.error = "error"
            binding.passwordError.isVisible = true
            binding.passwordAgainError.isVisible = false
            binding.passwordError.text = requireContext().getString(R.string.smaller_password)
            binding.etPasswordReg.requestFocus()
            return false
        } else {
            binding.etPasswordReg.error = null
            binding.etPasswordAgainReg.error = null
            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
        }

        if (binding.etPasswordReg.editText?.text.toString().length < 6) {
            // Password is too short
            binding.etPasswordReg.editText?.text = null
            binding.etPasswordAgainReg.editText?.text = null
            binding.etPasswordReg.error = "error"
            binding.passwordError.isVisible = true
            binding.passwordAgainError.isVisible = false
            binding.passwordError.text = requireContext().getString(R.string.bigger_password)
            binding.etPasswordReg.requestFocus()
            return false
        } else {
            binding.etPasswordReg.error = null
            binding.etPasswordAgainReg.error = null
            binding.passwordAgainError.isVisible = false
            binding.passwordError.isVisible = false
        }

        return true
    }

    /**
     * This function is attempting to register user with the given email, password, username, date of birth and optionally phone number.
     */
    private fun register() {
        // Remove whitespaces from begging and end of user's input
        val email = binding.etEmailReg.editText?.text.toString().trim()
        val password = binding.etPasswordReg.editText?.text.toString().trim()
        val username = binding.etUsernameReg.editText?.text.toString().trim()
        val phoneNumber = binding.etPhoneReg.editText?.text.toString().trim().toIntOrNull()
        val dateOfBirth = binding.etBirthdayReg.editText?.text.toString().trim()
        // This function is called to register a new user
        viewModel.register(email, password, username, phoneNumber, dateOfBirth)
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegisterBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)
}