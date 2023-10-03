/**
 * File: ChangePasswordFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Change password fragment, that is handling change password functionality.
 */

package com.example.booksharingapp.ui.home.changePassword

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.databinding.FragmentChangePasswordBinding
import com.example.booksharingapp.ui.*
import com.example.booksharingapp.ui.base.BaseFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * This fragment is handling change password functionality.
 */
class ChangePasswordFragment : BaseFragment<ChangePasswordViewModel, FragmentChangePasswordBinding, CombinedRepository>() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)

        // Get the DrawerLayout from the activity's layout
        val drawerLayout: DrawerLayout? = activity?.findViewById(R.id.drawer_layout)

        // Enable the navigation drawer menu to open and close with the app bar
        toggle = ActionBarDrawerToggle(this.activity, drawerLayout, R.string.open, R.string.close)
        drawerLayout?.addDrawerListener(toggle)
        // Synchronize the state of the ActionBarDrawerToggle with the state of the DrawerLayout
        toggle.syncState()

        // Allow the navigation drawer menu to open and close when the user clicks the app bar icon
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider {
            // Implement the onCreateMenu() method with an empty body, as this functionality is not needed
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Ensure that the navigation drawer menu opens and closes as expected
                if (toggle.onOptionsItemSelected(menuItem)) {
                    return true
                }
                return false
            }
        }, viewLifecycleOwner)

        binding.progressbar.visible(false)
        binding.btnChange.enable(false)

        // Observe changes in LiveData changePasswordResponse
        viewModel.changePasswordResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it is Resource.Loading)
            when(it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    lifecycleScope.launch {
                        // Delete content from text fields and show success snackbar to user
                        binding.etActualPasswordChange.editText?.setText("")
                        binding.etNewPasswordChange.editText?.setText("")
                        binding.etNewPasswordAgainChange.editText?.setText("")
                        showSuccessSnackbar(requireView(), "Heslo bylo úspěšně změněno")
                    }
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    binding.progressbar.visible(false)
                    binding.etActualPasswordChange.editText?.text = null
                    binding.etActualPasswordChange.requestFocus()
                    handleApiError(it)
                }
            }
        })

        // Enable change password button after all 3 text fields are filled!
        binding.etNewPasswordAgainChange.editText?.addTextChangedListener {
            val password = binding.etActualPasswordChange.editText?.text.toString().trim()
            val newPassword = binding.etNewPasswordChange.editText?.text.toString().trim()
            val newPasswordAgain = binding.etNewPasswordAgainChange.editText?.text.toString().trim()
            binding.etNewPasswordChange.error = null
            binding.btnChange.enable(password.isNotEmpty() && newPassword.isNotEmpty() && newPasswordAgain.isNotEmpty())
        }

        binding.etNewPasswordChange.editText?.addTextChangedListener {
            val password = binding.etActualPasswordChange.editText?.text.toString().trim()
            val newPassword = binding.etNewPasswordChange.editText?.text.toString().trim()
            val newPasswordAgain = binding.etNewPasswordAgainChange.editText?.text.toString().trim()
            binding.etNewPasswordChange.error = null
            binding.btnChange.enable(password.isNotEmpty() && newPassword.isNotEmpty() && newPasswordAgain.isNotEmpty())
        }

        binding.etActualPasswordChange.editText?.addTextChangedListener {
            val password = binding.etActualPasswordChange.editText?.text.toString().trim()
            val newPassword = binding.etNewPasswordChange.editText?.text.toString().trim()
            val newPasswordAgain = binding.etNewPasswordAgainChange.editText?.text.toString().trim()
            binding.etNewPasswordChange.error = null
            binding.btnChange.enable(password.isNotEmpty() && newPassword.isNotEmpty() && newPasswordAgain.isNotEmpty())
        }

        // Handle button click
        binding.btnChange.setOnClickListener {
            if (validateInput()) {
                changePassword()
            }
        }
    }

    /**
     * This function is trimming text fields and if password are matching, then it will call changePassword function from ViewModel
     */
    private fun changePassword() {
        val password = binding.etActualPasswordChange.editText?.text.toString().trim()
        val newPassword = binding.etNewPasswordChange.editText?.text.toString().trim()
        val newPasswordAgain = binding.etNewPasswordAgainChange.editText?.text.toString().trim()

        if(newPassword == newPasswordAgain) viewModel.changePassword(password, newPassword)
        else  {
            // Passwords are not matching, delete text fields
            binding.etNewPasswordChange.editText?.text = null
            binding.etNewPasswordAgainChange.editText?.text = null
            binding.etNewPasswordChange.requestFocus()
            showErrorSnackbar(requireView(), "Nová hesla se neshodují!")
        }
    }

    /**
     * This function is validating input, that is user attempting to send to change his password using button.
     * @return True if validating is successful, or false if validation failed
     */
    private fun validateInput() : Boolean {

        // Too long password
        if (binding.etNewPasswordChange.editText?.text.toString().length > 30) {
            binding.etNewPasswordChange.editText?.text = null
            binding.etNewPasswordAgainChange.editText?.text = null
            binding.etNewPasswordChange.error = "error"
            binding.passwordError.isVisible = true
            binding.passwordError.text = requireContext().getString(R.string.smaller_password)
            binding.etNewPasswordChange.requestFocus()
            return false
        } else {
            binding.etNewPasswordChange.error = null
            binding.passwordError.isVisible = false
        }

        // Too short password
        if (binding.etNewPasswordChange.editText?.text.toString().length < 6) {
            binding.etNewPasswordChange.editText?.text = null
            binding.etNewPasswordAgainChange.editText?.text = null
            binding.etNewPasswordChange.error = "error"
            binding.passwordError.isVisible = true
            binding.passwordError.text = requireContext().getString(R.string.bigger_password)
            binding.etNewPasswordChange.requestFocus()
            return false
        } else {
            binding.etNewPasswordChange.error = null
            binding.passwordError.isVisible = false
        }

        return true
    }

    override fun getViewModel() = ChangePasswordViewModel::class.java

    override fun getFragmentBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
    ) = FragmentChangePasswordBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
    val token = runBlocking {  userPreferences.authToken.first() }
    val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
    return CombinedRepository(api)
    }

}