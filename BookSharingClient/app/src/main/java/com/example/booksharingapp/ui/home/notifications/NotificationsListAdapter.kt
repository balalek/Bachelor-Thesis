/**
 * File: NotificationsListAdapter.kt
 * Author: Martin Baláž
 * Description: This file contains Notifications list adapter, that is responsible for creating and displaying the view of notification cards and setting them clickable.
 *              It is also responsible for resolving each notifications type from each other.
 */

package com.example.booksharingapp.ui.home.notifications

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booksharingapp.R
import com.example.booksharingapp.data.responses.notifications.Notifications
import com.example.booksharingapp.ui.*
import de.hdodenhof.circleimageview.CircleImageView
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * This class is responsible for creating and displaying the view of notification cards and setting them clickable.
 * It is also responsible for resolving each notifications type from each other.
 * @param notifications The list of all notifications to be displayed
 * @param viewModel The ViewModel to handle live data of lists
 * @param context The context of the fragment calling this adapter
 * @param navController The instance of navController, that is used to navigate to other fragments
 */
class NotificationsListAdapter (
    private val notifications: List<Notifications>,
    private val viewModel: NotificationsViewModel,
    private val context: Context,
    private val navController: NavController
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * This class is responsible for holding the views that make up a borrow book notification card.
     * @param itemView The view for displaying a single notification card
     */
    class BorrowBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_borrow_book_profile_image)
        val notificationText: TextView = itemView.findViewById(R.id.notification_borrow_book_text)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val acceptButton: AppCompatButton = itemView.findViewById(R.id.acceptButton)
        val rejectButton: AppCompatButton = itemView.findViewById(R.id.rejectButton)
        val userInfoButton: AppCompatButton = itemView.findViewById(R.id.userInformationButton)
        val buttonLayout: LinearLayout = itemView.findViewById(R.id.buttonLayout)
        val cardView: CardView = itemView.findViewById(R.id.notification_borrow_book_cards_view)
    }

    /**
     * This class is responsible for holding the views that make up a contact user notification card.
     * @param itemView The view for displaying a single notification card
     */
    class ContactUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_contact_user_profile_image)
        val notificationText: TextView = itemView.findViewById(R.id.notification_contact_user_text)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val cardView: CardView = itemView.findViewById(R.id.notification_contact_user_cards_view)
    }

    /**
     * This class is responsible for holding the views that make up a information about rejected book notification card.
     * @param itemView The view for displaying a single notification card
     */
    class RejectedBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_reject_book_profile_image)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val notificationText: TextView = itemView.findViewById(R.id.notification_reject_book_text)
        val cardView: CardView = itemView.findViewById(R.id.notification_reject_book_cards_view)
    }

    /**
     * This class is responsible for holding the views that make up a evaluate user notification card.
     * @param itemView The view for displaying a single notification card
     */
    class EvaluateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_evaluate_profile_image)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val notificationText: TextView = itemView.findViewById(R.id.notification_evaluate_text)
        val bookName: TextView = itemView.findViewById(R.id.book_name)
        val cardView: CardView = itemView.findViewById(R.id.notification_evaluate_cards_view)
        val ratingBar: RatingBar = itemView.findViewById(R.id.evaluate_rating_bar)
        val reviewAdd: EditText = itemView.findViewById(R.id.review_add)
        val evaluateBtn: Button = itemView.findViewById(R.id.evaluate_button)
        val errorRatingBarText: TextView = itemView.findViewById(R.id.rating_bar_error_text)
        val hiddenContent : FrameLayout = itemView.findViewById(R.id.hidden_content)
        val innerConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.inner_constraint_layout)
    }

    /**
     * This class is responsible for holding the views that make up a borrowed book time limit expired notification card.
     * @param itemView The view for displaying a single notification card
     */
    class TimeLimitExpiredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_time_limit_expired_profile_image)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val notificationText: TextView = itemView.findViewById(R.id.notification_time_limit_expired_text)
        val cardView: CardView = itemView.findViewById(R.id.notification_time_limit_expired_cards_view)
        val acceptButton: AppCompatButton = itemView.findViewById(R.id.acceptButton)
        val rejectButton: AppCompatButton = itemView.findViewById(R.id.rejectButton)
        val buttonLayout: LinearLayout = itemView.findViewById(R.id.buttonLayout)
    }

    /**
     * This class is responsible for holding the views that make up a information about book available notification card.
     * @param itemView The view for displaying a single notification card
     */
    class BookAvailableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: CircleImageView = itemView.findViewById(R.id.notification_book_available_profile_image)
        val date: TextView = itemView.findViewById(R.id.arrival_date)
        val notificationText: TextView = itemView.findViewById(R.id.notification_book_available_text)
        val cardView: CardView = itemView.findViewById(R.id.notification_book_available_cards_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // For each notification type select appropriate card view from resources
        return when(viewType) {
            BORROW_BOOK -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_borrow_book_cards_view,
                    parent,
                    false
                )
                BorrowBookViewHolder(view)
            }
            CONTACT_OWNER, CONTACT_BORROWER -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_contact_user_cards_view,
                    parent,
                    false
                )
                ContactUserViewHolder(view)
            }
            OWNER_DECLINED_BORROW -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_reject_book_cards_view,
                    parent,
                    false
                )
                RejectedBookViewHolder(view)
            }
            EVALUATE_OWNER, EVALUATE_BORROWER -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_evaluate_cards_view,
                    parent,
                    false
                )
                EvaluateViewHolder(view)
            }
            TIME_LIMIT_EXPIRED -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_time_limit_expired_cards_view,
                    parent,
                    false
                )
                TimeLimitExpiredViewHolder(view)
            }
            BOOK_IS_NOW_AVAILABLE -> {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.notification_book_available_cards_view,
                    parent,
                    false
                )
                BookAvailableViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val notification = notifications[position]
        // Get appropriate item view type according to notification type
        return when (notification.notificationId) {
            BORROW_BOOK -> {
                BORROW_BOOK
            }
            CONTACT_OWNER -> {
                CONTACT_OWNER
            }
            CONTACT_BORROWER -> {
                CONTACT_BORROWER
            }
            OWNER_DECLINED_BORROW -> {
                OWNER_DECLINED_BORROW
            }
            EVALUATE_OWNER -> {
                EVALUATE_OWNER
            }
            EVALUATE_BORROWER -> {
                EVALUATE_BORROWER
            }
            TIME_LIMIT_EXPIRED -> {
                TIME_LIMIT_EXPIRED
            }
            BOOK_IS_NOW_AVAILABLE -> {
                BOOK_IS_NOW_AVAILABLE
            }
            else -> {
                throw IllegalArgumentException("Invalid notification ID")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = notifications[position]

        // Set content of notifications card appropriate to their type
        when(holder.itemViewType) {
            BORROW_BOOK -> {
                val borrowBookViewHolder = holder as BorrowBookViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(borrowBookViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(borrowBookViewHolder.profilePicture)
                } else {
                    borrowBookViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                val notificationText = context.getString(R.string.borrow_book_text, notification.userName, notification.bookName)
                // This code is needed for <b> (bold) style type to appear on specific text
                borrowBookViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                borrowBookViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                // First we need to see the width of buttonLayout, that's why its visible before first click but still hidden behind card view
                borrowBookViewHolder.buttonLayout.visibility = if (!notification.isExpanded) View.VISIBLE else View.GONE

                borrowBookViewHolder.cardView.setOnClickListener {
                    notification.isExpanded = !notification.isExpanded
                    // Set animation of slide notification to left
                    val translationX = if (notification.isExpanded) -(borrowBookViewHolder.buttonLayout.width.toFloat()) else 0f
                    val anim = borrowBookViewHolder.cardView.animate().translationX(translationX).setDuration(200)
                    if (notification.isExpanded) {
                        anim.withEndAction {
                            // Show buttons on notification click
                            borrowBookViewHolder.buttonLayout.visibility = View.VISIBLE
                        }
                    } else {
                        anim.withEndAction {
                            // Hide buttons on notification click
                            borrowBookViewHolder.buttonLayout.visibility = View.GONE
                        }
                    }
                    anim.start()
                }

                borrowBookViewHolder.acceptButton.setOnClickListener {
                    viewModel.answerToBorrowBook(notification.bookId!!, true, notification.recordId!!)
                }

                borrowBookViewHolder.rejectButton.setOnClickListener {
                    viewModel.answerToBorrowBook(notification.bookId!!, false, notification.recordId!!)
                }

                borrowBookViewHolder.userInfoButton.setOnClickListener {
                    // Navigate to ProfileFragment of selected user to see his score
                    val action =
                        NotificationsFragmentDirections.actionNavNotificationsToNavProfile(
                            notification.userId!!
                        )
                    navController.navigate(action)
                }
            }
            CONTACT_OWNER, CONTACT_BORROWER -> {
                val contactUserViewHolder = holder as ContactUserViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(contactUserViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(contactUserViewHolder.profilePicture)
                } else {
                    contactUserViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                // Text depends on notification type
                if (holder.itemViewType == CONTACT_OWNER) {
                    val notificationText = context.getString(R.string.contact_owner_text, notification.bookName)
                    contactUserViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    val notificationText = context.getString(R.string.contact_borrower_text, notification.bookName)
                    contactUserViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                }

                contactUserViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                contactUserViewHolder.cardView.setOnClickListener {
                    // Navigate to ProfileFragment of selected user to see his contacts and contact him
                    val action =
                        NotificationsFragmentDirections.actionNavNotificationsToNavProfile(
                            notification.userId!!
                        )
                    navController.navigate(action)
                }
            }
            OWNER_DECLINED_BORROW -> {
                val rejectedBookViewHolder = holder as RejectedBookViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(rejectedBookViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(rejectedBookViewHolder.profilePicture)
                } else {
                    rejectedBookViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                val notificationText = context.getString(R.string.book_rejected_text, notification.bookName, notification.userName)
                rejectedBookViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                rejectedBookViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                rejectedBookViewHolder.cardView.setOnClickListener {
                    // Navigate to ProfileFragment of selected user to see his profile
                    val action =
                        NotificationsFragmentDirections.actionNavNotificationsToNavProfile(
                            notification.userId!!
                        )
                    navController.navigate(action)
                }
            }
            EVALUATE_OWNER, EVALUATE_BORROWER -> {
                val evaluateViewHolder = holder as EvaluateViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(evaluateViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(evaluateViewHolder.profilePicture)
                } else {
                    evaluateViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                evaluateViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                val notificationText = context.getString(R.string.evaluate_user_text, notification.userName)
                evaluateViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                evaluateViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate)

                val bookNameText = context.getString(R.string.evaluate_user_book_name_text, notification.bookName)
                evaluateViewHolder.bookName.text = Html.fromHtml(bookNameText, Html.FROM_HTML_MODE_LEGACY)

                // Score is mandatory, disable error, once user select some number of stars
                evaluateViewHolder.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                    evaluateViewHolder.errorRatingBarText.isVisible = false
                }

                // Show evaluation content on click, or hide it
                evaluateViewHolder.cardView.setOnClickListener {
                    evaluateViewHolder.hiddenContent.isVisible = !evaluateViewHolder.hiddenContent.isVisible
                }

                evaluateViewHolder.evaluateBtn.setOnClickListener {
                    if (evaluateViewHolder.ratingBar.rating == 0f) {
                        evaluateViewHolder.errorRatingBarText.isVisible = true

                        val constraintSet = ConstraintSet()
                        constraintSet.clone(evaluateViewHolder.innerConstraintLayout)

                        // Apply the modified constraints to the parent layout (TOP to BOTTOM)
                        constraintSet.connect(evaluateViewHolder.reviewAdd.id, ConstraintSet.TOP, evaluateViewHolder.errorRatingBarText.id, ConstraintSet.BOTTOM)
                        constraintSet.applyTo(evaluateViewHolder.innerConstraintLayout)
                    }
                    else {
                        // Call this function to review user, observation is handled in NotificationsFragment
                        viewModel.reviewUser(
                            notification.userId!!,
                            notification.recordId!!,
                            evaluateViewHolder.reviewAdd.text.toString().ifEmpty { null },
                            evaluateViewHolder.ratingBar.rating.toInt())
                    }

                }
            }
            TIME_LIMIT_EXPIRED -> {
                val timeLimitExpiredViewHolder = holder as TimeLimitExpiredViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(timeLimitExpiredViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(timeLimitExpiredViewHolder.profilePicture)
                } else {
                    timeLimitExpiredViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                val notificationText = context.getString(R.string.time_limit_expired_text, notification.bookName, notification.userName)
                timeLimitExpiredViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                timeLimitExpiredViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                // First we need to see the width of buttonLayout, that's why its visible before first click but still hidden behind card view
                timeLimitExpiredViewHolder.buttonLayout.visibility = if (!notification.isExpanded) View.VISIBLE else View.GONE

                timeLimitExpiredViewHolder.cardView.setOnClickListener {
                    notification.isExpanded = !notification.isExpanded
                    val translationX = if (notification.isExpanded) -(timeLimitExpiredViewHolder.buttonLayout.width.toFloat()) else 0f
                    val anim = timeLimitExpiredViewHolder.cardView.animate().translationX(translationX).setDuration(200)
                    if (notification.isExpanded) {
                        anim.withEndAction {
                            timeLimitExpiredViewHolder.buttonLayout.visibility = View.VISIBLE
                        }
                    } else {
                        anim.withEndAction {
                            timeLimitExpiredViewHolder.buttonLayout.visibility = View.GONE
                        }
                    }
                    anim.start()
                }

                timeLimitExpiredViewHolder.acceptButton.setOnClickListener {
                    viewModel.returnLate(notification.bookId!!, true, notification.recordId!!)
                }

                timeLimitExpiredViewHolder.rejectButton.setOnClickListener {
                    viewModel.returnLate(notification.bookId!!, false, notification.recordId!!)
                }
            }
            BOOK_IS_NOW_AVAILABLE -> {
                val bookAvailableViewHolder = holder as BookAvailableViewHolder

                if (!notification.profilePicture.isNullOrEmpty()) {
                    val decodedBytes = Base64.getDecoder().decode(notification.profilePicture)
                    Glide.with(bookAvailableViewHolder.itemView.context)
                        .load(decodedBytes)
                        .into(bookAvailableViewHolder.profilePicture)
                } else {
                    bookAvailableViewHolder.profilePicture.setImageResource(R.drawable.empty_profile)
                }

                val notificationText = context.getString(R.string.book_is_available_text, notification.bookName)
                bookAvailableViewHolder.notificationText.text = Html.fromHtml(notificationText, Html.FROM_HTML_MODE_LEGACY)
                bookAvailableViewHolder.date.text = getNotificationArrivalTimeDate(notification.arrivalDate!!)

                bookAvailableViewHolder.cardView.setOnClickListener {
                    // Navigate to BookDetailsFragment of selected book to ask for borrow quickly
                    val action =
                        NotificationsFragmentDirections.actionNavNotificationsToBookDetailsFragment(
                            notification.bookId!!
                        )
                    navController.navigate(action)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    /**
     * This function returns a string representing the time elapsed between the given notification time and the current time.
     * @param notificationTime The timestamp of the notification arrival in "yyyy-MM-dd HH:mm:ss.S" format
     * @return A human-readable relative time or date string in Czech language
     */
    private fun getNotificationArrivalTimeDate(notificationTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
        val notificationDateTime = LocalDateTime.parse(notificationTime, formatter)
        val currentDateTime = LocalDateTime.now()
        val duration = Duration.between(notificationDateTime, currentDateTime)

        return when {
            duration.seconds < 2 -> "před ${duration.seconds} sekundou"
            duration.seconds < 60 -> "před ${duration.seconds} sekundami"
            duration.toMinutes() < 2 -> "před ${duration.toMinutes()} minutou"
            duration.toMinutes() < 60 -> "před ${duration.toMinutes()} minutami"
            duration.toHours() < 2 -> "před ${duration.toHours()} hodinou"
            duration.toHours() < 24 -> "před ${duration.toHours()} hodinami"
            else -> {
                // If the notification is older than 24 hours, return the date in Czech language
                val czechDateTime = ZonedDateTime.of(notificationDateTime, ZoneId.of("Europe/Prague"))
                val czechFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale("cs", "CZ"))
                czechDateTime.format(czechFormatter)
            }
        }
    }

}