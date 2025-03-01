package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.asknshare.R
import com.example.asknshare.databinding.FragmentProfileSetupSecondBinding
import com.example.asknshare.databinding.FragmentProfileSetupThirdBinding
import com.example.asknshare.viewmodels.ProfileSetUpViewModel
import com.google.android.material.chip.Chip

class ProfileSetupThirdFragment : Fragment() {

    private var _binding: FragmentProfileSetupThirdBinding? = null
    private val binding get() = _binding!!
    // Use the shared ViewModel
    private val profileSetupViewModel: ProfileSetUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileSetupThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up Chips for Interests/Hobbies
        setupInterestChips()
    }

    private fun setupInterestChips() {

        // Access the string array from resources
        val interestList: List<String> = resources.getStringArray(R.array.hobbies).toList()

        val chipGroup = binding.chipGroupInterest
        interestList.forEach { interest ->
            val chip = layoutInflater.inflate(R.layout.item_chip, chipGroup, false) as Chip
            chip.text = interest
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    profileSetupViewModel.updateSelectedInterests(interest, isChecked)
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