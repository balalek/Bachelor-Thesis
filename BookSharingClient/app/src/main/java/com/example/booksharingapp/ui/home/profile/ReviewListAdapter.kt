/**
 * File: ReviewListAdapter.kt
 * Author: Martin Baláž
 * Description: This file contains Review list adapter, that is responsible for creating and displaying the view of review cards and setting them clickable.
 */

package com.example.booksharingapp.ui.home.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booksharingapp.R
import com.example.booksharingapp.data.responses.profile.MyReviews
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is responsible for displaying the view of review cards and setting them clickable.
 * @param reviewList The list of ReviewCard objects to display
 * @param listener The listener to handle clicks on reviews cards
 */
class ReviewListAdapter(private val reviewList: List<MyReviews>, private val listener: OnReviewClickListener) : RecyclerView.Adapter<ReviewListAdapter.ReviewViewHolder>() {

    /**
     * This is inner class which is responsible for holding the views that make up an individual review card.
     * It also implements View.OnClickListener to allow clicking on a book card.
     * @param itemView The view for displaying a single review card
     */
    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val evaluatorProfilePicture: CircleImageView = itemView.findViewById(R.id.evaluator_profile_image)
        val evaluatorName: TextView = itemView.findViewById(R.id.nameOfEvaluator)
        val ratingBar: RatingBar = itemView.findViewById(R.id.review_rating_bar)
        val evaluationDate: TextView = itemView.findViewById(R.id.dateOfReview)
        val reviewContent: TextView = itemView.findViewById(R.id.review_content)
        val layoutContent: LinearLayout = itemView.findViewById(R.id.content_review)

        // The constructor sets the click listener on the review card view
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onReviewClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Select the card view from xml files
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.reviews_cards_view, parent, false)
        return ReviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val currentReview = reviewList[position]

        if (!currentReview.profilePicture.isNullOrEmpty()) {
            val decodedBytes = Base64.getDecoder().decode(currentReview.profilePicture)
            Glide.with(holder.itemView.context)
                .load(decodedBytes)
                .into(holder.evaluatorProfilePicture)
        } else {
            // User have no profile picture, show default empty profile picture
            holder.evaluatorProfilePicture.setImageResource(R.drawable.empty_profile)
        }
        holder.evaluatorName.text = currentReview.userName
        holder.ratingBar.rating = currentReview.score.toFloat()
        if (currentReview.content.isNullOrEmpty()) holder.layoutContent.isVisible = false
        else holder.reviewContent.text = currentReview.content

        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val evaluationDateParsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentReview.dateCreated)
        calendar.time = evaluationDateParsed!!
        // Show date, when was review submitted in cz format
        holder.evaluationDate.text = dateFormatter.format(calendar.time)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    /**
     * This is an interface for handling clicks on a review card.
     */
    interface OnReviewClickListener {
        /**
         * This function is called when a review card is clicked.
         * @param position The position of the clicked review card
         */
        fun onReviewClick(position: Int)
    }

}