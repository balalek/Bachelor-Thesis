/**
 * File: ProfileFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Profile fragment, that is getting and then passing profile data to its children fragments, that are in profile pager.
 */

package com.example.booksharingapp.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.databinding.FragmentProfileBinding
import com.example.booksharingapp.ui.GlobalVariables
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.handleApiError
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is getting and then passing profile data to its children fragments, that are in profile pager.
 */
class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding, CombinedRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)

        if (arguments == null) {
            // Load loggedIn user's profile data
            viewModel.getUser()
            GlobalVariables.isOnMyProfile = true
            // Observe the user LiveData and update UI accordingly
            viewModel.user.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbar.visible(true)
                    }
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        val profileResponse = it.value.data
                        viewModel.updateBookList(profileResponse.myBooks.unborrowedBooks)
                        viewModel.updateBorrowedBookList(profileResponse.myBooks.borrowedBooks)
                        viewModel.updateBooksToReturn(profileResponse.myBooks.booksToReturn)
                        // Set up ViewPager and adapter
                        binding.viewPager.adapter =
                            ProfilePagerAdapter(childFragmentManager, profileResponse, true)
                        binding.tabLayout.setupWithViewPager(binding.viewPager)
                    }
                    is Resource.Failure -> {
                        handleApiError(it)
                    }
                }
            })
        } else {
            // Load user's profile data
            viewModel.getUserById(arguments?.getInt("userId")!!)
            GlobalVariables.isOnMyProfile = false
            // Observe the user LiveData and update UI accordingly
            viewModel.userById.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbar.visible(true)
                    }
                    is Resource.Success -> {
                        binding.progressbar.visible(false)
                        val profileResponse = it.value.data
                        viewModel.updateBookList(profileResponse.myBooks.unborrowedBooks)
                        viewModel.updateBorrowedBookList(profileResponse.myBooks.borrowedBooks)
                        // Set up ViewPager and adapter
                        if (it.value.data.myContacts.clickedOnLoggedInUser) {
                            viewModel.updateBooksToReturn(profileResponse.myBooks.booksToReturn)
                            binding.viewPager.adapter =
                                ProfilePagerAdapter(childFragmentManager, profileResponse, true)
                            binding.tabLayout.setupWithViewPager(binding.viewPager)
                        } else {
                            binding.viewPager.adapter =
                                ProfilePagerAdapter(childFragmentManager, profileResponse, false)
                            binding.tabLayout.setupWithViewPager(binding.viewPager)
                        }
                    }
                    is Resource.Failure -> {
                        handleApiError(it)
                    }
                }
            })
        }

    }

    /**
     * This function is making background less visible, when pop-up window is opened.
     */
    fun reduceVisibility() {
        binding.parentProfileConstraintLayout.alpha = 0.2f
    }

    /**
     * This function is making background normally visible, when pop-up window is closed.
     */
    fun increaseVisibility() {
        binding.parentProfileConstraintLayout.alpha = 1f
    }

    /**
     * This function can provide the same instance of ViewModel to children fragments, that are in profile pager.
     */
    fun getProfileViewModel(): ProfileViewModel {
        return viewModel
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}