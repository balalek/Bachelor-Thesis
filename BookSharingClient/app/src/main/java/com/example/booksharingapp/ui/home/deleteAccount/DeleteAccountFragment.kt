/**
 * File: DeleteAccountFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Delete account fragment, that is handling delete account functionality.
 */

package com.example.booksharingapp.ui.home.deleteAccount

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.databinding.FragmentDeleteAccountBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is handling delete account functionality.
 */
class DeleteAccountFragment : BaseFragment<DeleteAccountViewModel, FragmentDeleteAccountBinding, CombinedRepository>() {

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

        // Cast the activity to a MenuHost and add a new MenuProvider to it
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

        binding.btnDeleteAccount.setOnClickListener {
            val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popUpView = inflater.inflate(R.layout.delete_account_pop_up_window, null)
            val popUpWindow = PopupWindow(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            val confirmButton = popUpView.findViewById<Button>(R.id.confirm_delete_btn)
            val cancelButton = popUpView.findViewById<Button>(R.id.cancel_delete_btn)

            // Show pop-up window to confirm delete account and reduce background visibility
            binding.parentDeleteAccountConstraintLayout.alpha = 0.2f
            popUpWindow.showAtLocation(binding.btnDeleteAccount, Gravity.CENTER, 0, 0)

            // Close pop-up window
            cancelButton.setOnClickListener {
                popUpWindow.dismiss()
                binding.parentDeleteAccountConstraintLayout.alpha = 1f
            }

            confirmButton.setOnClickListener {
                viewModel.deleteAccount()
                viewModel.account.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is Resource.Success -> {
                            // This will delete authentication token from device and navigate user to AuthActivity
                            logout()
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

            // Close pop-up window when clicked outside of it
            popUpWindow.setOnDismissListener {
                popUpWindow.dismiss()
                binding.parentDeleteAccountConstraintLayout.alpha = 1f
            }
        }
    }

    override fun getViewModel() = DeleteAccountViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDeleteAccountBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}