package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.databinding.FragmentProfileSetupFirstBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ProfileSetupFirstFragment : Fragment() {

    private var _binding: FragmentProfileSetupFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataStoreHelper: DataStoreHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSetupFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DataStoreHelper
        dataStoreHelper = DataStoreHelper(requireContext())

        // Load user data from DataStore
        lifecycleScope.launch {
            val email = dataStoreHelper.userEmail.first() ?: "default@example.com"
            val username = dataStoreHelper.username.first() ?: "DefaultUser"

            // Set the username in the UI
            binding.textfieldUsername.setText(username)
            binding.textfieldUsername.isEnabled = false


            // Optionally use the email if needed
            println("Email: $email")
        }

        // Set up autocomplete for profession/role
        setupRoleAutocomplete()
    }

    private fun setupRoleAutocomplete() {
        // List of roles/professions
        val roles = listOf(
            "Software Engineer", "Data Scientist", "Product Manager", "UX/UI Designer", "DevOps Engineer",
            "Marketing Specialist", "Teacher", "Student", "Doctor", "Lawyer", "Entrepreneur", "Researcher",
            "Engineer", "Scientist", "Analyst", "Consultant", "Designer", "Developer", "Architect", "Writer",
            "Artist", "Musician", "Athlete", "Activist", "Volunteer", "Intern", "Lab Technician", "Pharmacist",
            "Therapist", "Counselor", "Librarian", "Curator", "Animator", "Game Developer", "Social Media Manager",
            "Financial Analyst", "Policy Advisor", "Environmentalist", "Historian", "Philosopher", "Linguist",
            "Translator", "Biologist", "Chemist", "Physicist", "Mathematician", "Statistician", "Economist",
            "Psychologist", "Sociologist", "Anthropologist", "Geographer", "Astronomer", "Neuroscientist",
            "Robotics Engineer", "AI Specialist", "Blockchain Developer", "Renewable Energy Engineer",
            "Nanotechnologist", "Aerospace Engineer", "Marine Biologist", "Astrophysicist", "Material Scientist"
        )

        // Create an adapter for the AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roles
        )

        // Set the adapter to the AutoCompleteTextView
        binding.textfieldRole.setAdapter(adapter)

        // Optional: Handle item selection
        binding.textfieldRole.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roles[position]
            println("Selected Role: $selectedRole")
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}