/**
 * File: HomeActivity.kt
 * Author: Martin Baláž
 * Description: This file contains home activity for all authenticated screens.
 *              It also setups navigation between child fragments and its handling action bar items.
 *
 */

package com.example.booksharingapp.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.booksharingapp.databinding.ActivityHomeBinding
import com.example.booksharingapp.R
import com.example.booksharingapp.ui.GlobalVariables

/**
 * This class is for all authenticated screens.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set it as the content view
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Set up the navigation components
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        navController = navHostFragment.navController

        // Add a destination-specific NavigationCallback to set the label of AddOrEditBookFragment
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            if (destination.id == R.id.nav_add_book) {
                if (arguments?.containsKey("bookId") == true) {
                    destination.label = "Editovat knihu"
                } else {
                    destination.label = "Přidat knihu"
                }
            }
        }

        // Set up the navigation view with the navigation controller
        NavigationUI.setupWithNavController(binding.navView, navController)
        // Set up the action bar with the navigation controller and drawer layout
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu and add items to the action bar if it is present
        menuInflater.inflate(R.menu.nav_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Respond to item selections in the action bar
        return when (item.itemId) {
            R.id.notification_bell -> {
                // Navigate to the notifications fragment if not already there
                val currentDestinationId = navController.currentDestination?.id
                if (currentDestinationId != R.id.nav_notifications) {
                    navController.navigate(R.id.toNotifications)
                }
                true
            }
            R.id.profile_avatar_item -> {
                // Navigate to the profile fragment if not already there
                val currentDestinationId = navController.currentDestination?.id
                if (currentDestinationId != R.id.nav_profile || !GlobalVariables.isOnMyProfile) {
                    navController.navigate(R.id.toProfile)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Navigate back on back button pressed
        return NavigationUI.navigateUp(
            navController,
            binding.drawerLayout
        )
    }

}