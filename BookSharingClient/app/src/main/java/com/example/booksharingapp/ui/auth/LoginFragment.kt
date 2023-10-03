/**
 * File: LoginFragment.kt
 * Authors: Martin Baláž, Belal Khan
 * Original Code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/ui/auth/LoginFragment.kt
 * Description: This file contains Login fragment, that is handling login functionality.
 */

package com.example.booksharingapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.AuthApi
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.AuthRepository
import com.example.booksharingapp.databinding.FragmentLoginBinding
import com.example.booksharingapp.ui.*
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.home.HomeActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

/**
 * This fragment is handling login functionality.
 */
class LoginFragment : BaseFragment<AuthViewModel, FragmentLoginBinding, AuthRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide progress bar and disable login button initially
        binding.progressbar.visible(false)
        binding.btnLogin.enable(false)

        // Observe login response
        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it is Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    lifecycleScope.launch {
                        // Save auth token and start HomeActivity
                        viewModel.saveAuthToken(it.value.data.authToken!!)
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    binding.progressbar.visible(false)
                    // Handle API error and retry login on button click
                    handleApiError(it) { login() }
                }
            }
        })

        // Enable login button when both email and password fields are not empty
        binding.etPassword.editText?.addTextChangedListener {
            val email = binding.etEmail.editText?.text.toString().trim()
            binding.btnLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }

        binding.etEmail.editText?.addTextChangedListener {
            val password = binding.etPassword.editText?.text.toString().trim()
            binding.btnLogin.enable(password.isNotEmpty() && it.toString().isNotEmpty())
        }

        // Handle login button click
        binding.btnLogin.setOnClickListener{
            login()
        }

        // Navigate to RegisterFragment on register link click
        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    /**
     * This function is attempting to log in with the given email, password, and registration token.
     */
    private fun login() {
        // Remove whitespaces from begging and end of user's input
        val email = binding.etEmail.editText?.text.toString().trim()
        val password = binding.etPassword.editText?.text.toString().trim()

        // Get the instance of FirebaseMessaging and retrieve the token for the device
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val registrationToken = task.result
                viewModel.login(email, password, registrationToken.toString())
            } else {
                showErrorSnackbar(requireView(), "Chyba při získání unikátního kódu vašeho zařízení. Zkuste to prosím později.")
            }
        }

    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = AuthRepository(remoteDataSource.buildApi(AuthApi::class.java), userPreferences)

}