package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.asknshare.R
import com.example.asknshare.databinding.FragmentProfileSetupFirstBinding
import com.example.asknshare.databinding.FragmentProfileSetupSecondBinding
import com.google.android.material.chip.Chip

class ProfileSetupSecondFragment : Fragment() {

    private var _binding: FragmentProfileSetupSecondBinding? = null
    private val binding get() = _binding!!

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

        val expertiseList = listOf(
            "Software Development", "Data Science", "Artificial Intelligence", "Machine Learning",
            "Cybersecurity", "Cloud Computing", "Web Development", "Mobile App Development",
            "Electrical Engineering", "Mechanical Engineering", "Civil Engineering", "Chemical Engineering",
            "Biotechnology", "Pharmaceutical Sciences", "Environmental Science", "Physics", "Mathematics",
            "Economics", "Psychology", "Political Science", "Sociology", "Linguistics", "Architecture",
            "Business Administration", "Marketing", "Finance", "Law", "Medicine", "Education", "Journalism",
            "Fine Arts", "Performing Arts", "Game Design", "Animation", "Digital Marketing", "Renewable Energy",
            "Nanotechnology", "Aerospace Engineering", "Marine Biology", "Astrophysics", "Neuroscience",
            "Anthropology", "Philosophy", "History", "Literature", "Geography", "Statistics", "Robotics",
            "Material Science", "Quantum Computing", "Blockchain Technology"
        )

        val chipGroup = binding.chipGroupExpertise
        expertiseList.forEach { expertise ->
            val chip = layoutInflater.inflate(R.layout.item_chip, chipGroup, false) as Chip
            chip.text = expertise
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    println("Selected Expertise: $expertise")
                } else {
                    println("Deselected Expertise: $expertise")
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