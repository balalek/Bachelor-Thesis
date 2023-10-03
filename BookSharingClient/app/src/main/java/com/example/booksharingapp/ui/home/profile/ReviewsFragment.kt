/**
 * File: ReviewsFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Reviews fragment, that is showing all reviews, that were made to selected user
 */

package com.example.booksharingapp.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.profile.MyReviews
import com.example.booksharingapp.databinding.FragmentReviewsBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is showing all reviews, that were made to selected user
 */
class ReviewsFragment : BaseFragment<ProfileViewModel, FragmentReviewsBinding, CombinedRepository>() {

    lateinit var adapter: ReviewListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressbar.visible(false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.reviews_list)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        // Receive data from profile fragment
        val reviewList = arguments?.getParcelableArrayList<MyReviews>("reviews") ?: emptyList()

        if (reviewList.isEmpty()) {
            // Show information, that there are no reviews
            binding.textForNonReviewedUsers.isVisible = true
            binding.reviewsList.isVisible = false
        } else {
            // Show review cards using Recycler View
            binding.textForNonReviewedUsers.isVisible = false
            binding.reviewsList.isVisible = true
            adapter = ReviewListAdapter(
                reviewList,
                object : ReviewListAdapter.OnReviewClickListener {
                    override fun onReviewClick(position: Int) {
                        val clickedItem = reviewList[position]
                        // Navigate to profile of the user, whose review was clicked on
                        val action =
                            ProfileFragmentDirections.actionNavProfileSelf(
                                clickedItem.createdBy
                            )
                        findNavController().navigate(action)
                    }
                })

            recyclerView.adapter = adapter
        }
    }
    // This companion object is responsible for creating an instance of the ReviewsFragment
    companion object {
        /**
         * This function creates instance of ReviewsFragment with applied arguments.
         * @param reviews The reviews to display in the fragment
         * @return A new instance of the ReviewsFragment
         */
        fun newInstance(reviews: List<MyReviews>) =
            ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("reviews", ArrayList(reviews))
                }
            }
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReviewsBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): CombinedRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(CombinedApi::class.java, token)
        return CombinedRepository(api)
    }

}