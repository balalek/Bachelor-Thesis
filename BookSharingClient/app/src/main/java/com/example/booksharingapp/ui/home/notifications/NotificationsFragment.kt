/**
 * File: NotificationsFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Notifications fragment, that is showing logged-in user's notifications and sets up observers for LiveData, that are being changed in adapter.
 */

package com.example.booksharingapp.ui.home.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.databinding.FragmentNotificationsBinding
import com.example.booksharingapp.ui.GlobalVariables.cameFromNotifications
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.showSuccessSnackbar
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is showing logged-in user's notifications and sets up observers for LiveData, that are being changed in adapter.
 */
class NotificationsFragment : BaseFragment<NotificationsViewModel, FragmentNotificationsBinding, CombinedRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // By setting global variable to true, filter setting in HomeFragment won't be affected by notification click
        if (arguments?.getInt("price") == 0) cameFromNotifications = true
        binding.progressbar.visible(false)

        // Call this function to get logged-in user's notifications
        viewModel.getNotifications()
        viewModel.notifications.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)

                    if (it.value.data.isNullOrEmpty()) {
                        // Show message, that there are no notifications
                        binding.emptyNotifications.isVisible = true
                        binding.recyclerViewNotifications.isVisible = false
                    } else {
                        binding.emptyNotifications.isVisible = false
                        binding.recyclerViewNotifications.isVisible = true

                        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewNotifications)
                        // Create adapter instance for notifications
                        val adapter = NotificationsListAdapter(
                            it.value.data,
                            viewModel,
                            requireContext(),
                            findNavController()
                        )
                        // Set the RecyclerView's adapter to the newly created adapter with the notifications list
                        recyclerView.adapter = adapter
                    }
                }
                is Resource.Loading -> {
                    binding.progressbar.visible(true)
                }
                is Resource.Failure -> {
                    handleApiError(it) {viewModel.getNotifications()}
                    binding.progressbar.visible(false)
                }
            }
        })

        // Observe changes to the LiveData that is being updated by a function called in the adapter.
        viewModel.answer.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    // Download notifications again to update UI
                    viewModel.getNotifications()
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

        // Observe changes to the LiveData that is being updated by a function called in the adapter.
        viewModel.returnLate.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    // Download notifications again to update UI
                    viewModel.getNotifications()
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

        // Observe changes to the LiveData that is being updated by a function called in the adapter.
        viewModel.review.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.progressbar.visible(false)
                    // Download notifications again to update UI
                    viewModel.getNotifications()
                    showSuccessSnackbar(requireView(), "Uživatel byl úspěšně ohodnocen.")
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

    override fun getViewModel() = NotificationsViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentNotificationsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}