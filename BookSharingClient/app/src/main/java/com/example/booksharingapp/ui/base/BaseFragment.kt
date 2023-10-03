/**
 * File: BaseFragment.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/ui/base/BaseFragment.kt
 * Description: This file contains class, that provides a base implementation of Fragment for all child Fragments.
 */

package com.example.booksharingapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.booksharingapp.data.UserPreferences
import com.example.booksharingapp.data.network.RemoteDataSource
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.BaseRepository
import com.example.booksharingapp.ui.auth.AuthActivity
import com.example.booksharingapp.ui.startNewActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * This is a class to be used as the parent class for all fragments.
 * @param VM The type of ViewModel that will be used in this fragment, extending the BaseViewModel class
 * @param B The type of ViewBinding that will be used in this fragment
 * @param R The type of Repository that will be used in this fragment, extending the BaseRepository class
 */
abstract class BaseFragment<VM: BaseViewModel, B: ViewBinding, R: BaseRepository> : Fragment(){

    // Properties for user preferences, view binding, view model and remote data source
    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected val remoteDataSource = RemoteDataSource()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize user preferences, view binding and view model using corresponding methods
        userPreferences = UserPreferences(requireContext())
        binding = getFragmentBinding(inflater, container)
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        // Launch a coroutine to observe changes in user authentication token
        lifecycleScope.launch { userPreferences.authToken.first() }

        return binding.root
    }

    /**
     * This function is handling logout action in the fragment
     */
    fun logout() = lifecycleScope.launch{
        val authToken = userPreferences.authToken.first()
        val api = remoteDataSource.buildApi(CombinedApi::class.java, authToken)
        viewModel.logout(api)
        userPreferences.clear()
        requireActivity().startNewActivity(AuthActivity::class.java)
    }

    /**
     * This function is used to get the class of ViewModel used in this fragment.
     */
    abstract fun getViewModel() : Class<VM>

    /**
     * This function is used to get the ViewBinding used in this fragment.
     * @param inflater LayoutInflater to inflate the layout of the fragment
     * @param container ViewGroup container of the fragment
     * @return B An instance of ViewBinding class used in this fragment
     */
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : B

    /**
     * This function is used to get the Repository used in this fragment.
     * @return R An instance of Repository class used in this fragment
     */
    abstract fun getFragmentRepository() : R

}