package com.example.asknshare.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.asknshare.R
import com.example.asknshare.databinding.FragmentProfileSetupFirstBinding
import com.example.asknshare.databinding.FragmentProfileSetupSecondBinding
import com.example.asknshare.viewmodels.ProfileSetUpViewModel
import com.google.android.material.chip.Chip

class ProfileSetupSecondFragment : Fragment() {

    private var _binding: FragmentProfileSetupSecondBinding? = null
    private val binding get() = _binding!!
    // Use the shared ViewModel
    private val profileSetupViewModel: ProfileSetUpViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentProfileSetupSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Chips for Area of Expertise
        setupExpertiseChips()


    }

    private fun setupExpertiseChips() {

        // Access the string array from resources
        val expertiseList: List<String> = resources.getStringArray(R.array.expertise_list).toList()

        val chipGroup = binding.chipGroupExpertise
        expertiseList.forEach { expertise ->
            val chip = layoutInflater.inflate(R.layout.item_chip, chipGroup, false) as Chip
            chip.text = expertise
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    profileSetupViewModel.updateSelectedExpertise(expertise, isChecked)
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.app_dark_blue)
                } else {
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.app_light_blue)
                }
            }
            chipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}