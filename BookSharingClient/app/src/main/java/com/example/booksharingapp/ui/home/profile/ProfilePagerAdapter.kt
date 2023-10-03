/**
 * File: ProfilePagerAdapter.kt
 * Author: Martin Baláž
 * Description: This file contains Profile pager adapter, that is responsible for creating instances of 3 fragments (tabs) for the user's profile page.
 */

package com.example.booksharingapp.ui.home.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.booksharingapp.data.responses.profile.Profile

/**
 * This class is responsible for creating instances of 3 fragments (tabs) for the user's profile page.
 * @param fm The FragmentManager instance to use
 * @param user The Profile object containing the user's profile information
 * @param isCurrentUser A boolean flag indicating whether the user is the current user
 */
class ProfilePagerAdapter(fm: FragmentManager, private val user: Profile, private val isCurrentUser: Boolean) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val tabTitles = arrayOf("Kontakty", "Recenze", "Knihy")

    override fun getItem(position: Int): Fragment {
        return when (position) {
            // The first fragment (0) will be always opened first
            0 -> ContactsFragment.newInstance(user.myContacts, isCurrentUser)
            1 -> ReviewsFragment.newInstance(user.myReviews)
            2 -> MyBooksFragment.newInstance(isCurrentUser)
            else -> throw IllegalStateException("Špatná pozice tabů")
        }
    }

    override fun getCount(): Int {
        return tabTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}
