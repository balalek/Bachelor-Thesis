/**
 * File: FilterFragment.kt
 * Author: Martin Baláž
 * Description: This file contains Filter fragment, where is user setting filter settings, to filter books in HomeFragment.
 */

package com.example.booksharingapp.ui.home.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.example.booksharingapp.R
import com.example.booksharingapp.data.network.BookApi
import com.example.booksharingapp.data.repository.BookRepository
import com.example.booksharingapp.databinding.FragmentFilterBinding
import com.example.booksharingapp.ui.base.BaseFragment
import com.example.booksharingapp.ui.visible
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * This fragment is where user is setting filter settings, to filter books in HomeFragment.
 */
class FilterFragment : BaseFragment<FilterViewModel, FragmentFilterBinding, BookRepository>() {

    private var author: String = ""
    private var price: Int = 100
    private var availability: MutableList<String> = mutableListOf()
    private var borrowOptions: MutableList<String> = mutableListOf()
    private var selectedGenres: MutableList<String> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide upper action bar that is normally shown
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE

        binding.progressbar.visible(false)

        // Check if there are stored filter settings in arguments, store them in variables if there are some
        price = if (arguments?.getInt("price") != -1) arguments?.getInt("price")!! else 100
        author = if (arguments?.getString("author") != "") arguments?.getString("author")!! else ""
        // Make lists of string split with '.'
        selectedGenres = if (arguments?.getString("genres") != "") arguments?.getString("genres")!!.split(".").toMutableList() else mutableListOf()
        borrowOptions = if (arguments?.getString("options") != "") arguments?.getString("options")!!.split(".").toMutableList() else mutableListOf()
        availability = if (arguments?.getString("availability") != "") arguments?.getString("availability")!!.split(".").toMutableList() else mutableListOf()

        // Set price slider and price text by price variable from arguments
        if (price != -1) {
            binding.priceSlider.setValues(price.toFloat())
            val maxPrice = binding.priceSlider.values[0].toInt()
            if (maxPrice != 0) binding.priceRangeText.text = getString(R.string.slider_values_range, maxPrice)
            else binding.priceRangeText.text = getString(R.string.value_free)
        }

        // Set author field by author variable from arguments
        if(author != "") {
            binding.filterAuthor.setText(author)
        }

        // Check all genres, that are in list variable from arguments
        if(selectedGenres.isNotEmpty()) {
            val checkBoxList = listOf(binding.povidka, binding.roman, binding.biografie, binding.poezie, binding.drama, binding.sciFi, binding.fantasy, binding.publicistika, binding.komiks, binding.baje, binding.divciRomany, binding.detektivka, binding.romanProZeny, binding.cestopis, binding.humor, binding.literaturaFaktu, binding.erotika, binding.literatura, binding.horror)
            for ((_, checkBox) in checkBoxList.withIndex()) {
                checkBox.isChecked = checkBox.text in selectedGenres
            }
        }

        // Check all borrow option, that are in list variable from arguments
        if(borrowOptions.isNotEmpty()) {
            val checkBoxList = listOf(binding.mail, binding.personal)
            for ((_, checkBox) in checkBoxList.withIndex()) {
                checkBox.isChecked = checkBox.text in borrowOptions
            }
        }

        // Check all availability options, that are in list variable from arguments
        if(availability.isNotEmpty()) {
            val checkBoxList = listOf(binding.free, binding.borrowed)
            for ((_, checkBox) in checkBoxList.withIndex()) {
                checkBox.isChecked = checkBox.text in availability
            }
        }

        // Leave FilterFragment to HomeFragment with original (previous) arguments
        binding.closeIcon.setOnClickListener {
            val action = FilterFragmentDirections.actionFilterFragmentToNavToHome(arguments?.getInt("price")!!, arguments?.getString("author")!!, arguments?.getString("genres")!!, arguments?.getString("options")!!, arguments?.getString("availability")!!)
            findNavController().navigate(action)
        }

        /* PRICE */
        binding.priceSlider.addOnChangeListener { slider, _, _ ->
            val maxPrice = slider.values[0].toInt()
            price = slider.values[0].toInt()
            // Change text on price slide
            if (maxPrice != 0) binding.priceRangeText.text = getString(R.string.slider_values_range, maxPrice)
            else binding.priceRangeText.text = getString(R.string.value_free)
        }

        /* AUTHOR */
        binding.filterAuthor.doAfterTextChanged { text ->
            author = text.toString()
        }

        /* GENRES */
        // Store checked genres into list, or remove them on unchecking
        binding.povidka.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.povidka)) else selectedGenres.remove(getString(R.string.povidka))
        }

        binding.roman.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.roman)) else selectedGenres.remove(getString(R.string.roman))
        }

        binding.biografie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.biografie)) else selectedGenres.remove(getString(R.string.biografie))
        }

        binding.poezie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.poezie)) else selectedGenres.remove(getString(R.string.poezie))
        }

        binding.drama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.drama)) else selectedGenres.remove(getString(R.string.drama))
        }

        binding.publicistika.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.publicistika)) else selectedGenres.remove(getString(R.string.publicistika))
        }

        binding.komiks.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.komiks)) else selectedGenres.remove(getString(R.string.komiks))
        }

        binding.literatura.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.literatura_pro_deti_a_mladez)) else selectedGenres.remove(getString(R.string.literatura_pro_deti_a_mladez))
        }

        binding.baje.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.baje_myty_a_povesti)) else selectedGenres.remove(getString(R.string.baje_myty_a_povesti))
        }

        binding.divciRomany.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.divci_roman)) else selectedGenres.remove(getString(R.string.divci_roman))
        }

        binding.fantasy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.fantasy)) else selectedGenres.remove(getString(R.string.fantasy))
        }

        binding.sciFi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.scifi)) else selectedGenres.remove(getString(R.string.scifi))
        }

        binding.detektivka.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.detektivka)) else selectedGenres.remove(getString(R.string.detektivka))
        }

        binding.romanProZeny.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.roman_pro_zeny)) else selectedGenres.remove(getString(R.string.roman_pro_zeny))
        }

        binding.cestopis.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.cestopis)) else selectedGenres.remove(getString(R.string.cestopis))
        }

        binding.humor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.humor_a_satira)) else selectedGenres.remove(getString(R.string.humor_a_satira))
        }

        binding.literaturaFaktu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.literatura_faktu)) else selectedGenres.remove(getString(R.string.literatura_faktu))
        }

        binding.erotika.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.erotika)) else selectedGenres.remove(getString(R.string.erotika))
        }

        binding.horror.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedGenres.add(getString(R.string.horror)) else selectedGenres.remove(getString(R.string.horror))
        }

        /* BORROW OPTIONS */
        binding.mail.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) borrowOptions.add(getString(R.string.mail_it)) else borrowOptions.remove(getString(R.string.mail_it))
        }

        binding.personal.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) borrowOptions.add(getString(R.string.personal)) else borrowOptions.remove(getString(R.string.personal))
        }

        /* AVAILABILITY */
        binding.free.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) availability.add(getString(R.string.free)) else availability.remove(getString(R.string.free))
        }

        binding.borrowed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) availability.add(getString(R.string.borrowed)) else availability.remove(getString(R.string.borrowed))
        }

        // Leave FilterFragment to HomeFragment with removed filter settings
        binding.deleteFilter.setOnClickListener {
            val action = FilterFragmentDirections.actionFilterFragmentToNavToHome(-1, "", "", "", "")
            findNavController().navigate(action)
        }

        // Leave FilterFragment to HomeFragment with selected filter settings
        binding.filterButton.setOnClickListener {
            val filterSelectedGenres = if(selectedGenres.isNotEmpty()) selectedGenres.joinToString(separator = ".") else ""
            val filterBorrowOptions = if(borrowOptions.isNotEmpty()) borrowOptions.joinToString(separator = ".") else ""
            val filterAvailability = if(availability.isNotEmpty()) availability.joinToString(separator = ".") else ""
            val action = FilterFragmentDirections.actionFilterFragmentToNavToHome(price, author, filterSelectedGenres, filterBorrowOptions, filterAvailability)
            findNavController().navigate(action)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show the regular toolbar (action bar) when the fragment is destroyed
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
    }

    override fun getViewModel() = FilterViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFilterBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): BookRepository {
        val token = runBlocking {  userPreferences.authToken.first() }
        val api = remoteDataSource.buildApi(BookApi::class.java, token)
        return BookRepository(api)
    }

}