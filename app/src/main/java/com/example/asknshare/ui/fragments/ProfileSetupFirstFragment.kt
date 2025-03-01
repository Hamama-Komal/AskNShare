package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.asknshare.R
import com.example.asknshare.R.*
import com.example.asknshare.data.local.DataStoreHelper
import com.example.asknshare.databinding.FragmentProfileSetupFirstBinding
import com.example.asknshare.viewmodels.ProfileSetUpViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ProfileSetupFirstFragment : Fragment() {

    private var _binding: FragmentProfileSetupFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataStoreHelper: DataStoreHelper
    private val profileSetupViewModel: ProfileSetUpViewModel by activityViewModels()


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

        // Fetch and set email from DataStore
        lifecycleScope.launch {
            dataStoreHelper.userEmail.collect { email ->
                if (!email.isNullOrEmpty()) {
                    profileSetupViewModel.setEmail(email)
                }
            }

            dataStoreHelper.username.collect { username ->
                if (!username.isNullOrEmpty()) {
                    binding.textfieldUsername.setText(username)
                }
            }


        }

        // Text Change Listener for Username
        binding.textfieldUsername.addTextChangedListener { editable ->
            profileSetupViewModel.setUsername(editable.toString())
        }

        // Text Change Listener for Full name
        binding.textfieldFullname.addTextChangedListener { editable ->
            profileSetupViewModel.setFullName(editable.toString())
        }



        // Set up autocomplete for profession/role
        setupRoleAutocomplete()
    }

    private fun setupRoleAutocomplete() {
        // List of roles/professions
        val roles: List<String> = resources.getStringArray(array.roles_list).toList()

        // Create an adapter for the AutoCompleteTextView
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roles
        )

        // Set the adapter to the AutoCompleteTextView
        binding.textfieldRole.setAdapter(adapter)

        binding.textfieldRole.setOnItemClickListener { parent, _, position, _ ->
            val selectedRole = parent.getItemAtPosition(position).toString()
            profileSetupViewModel.setProfession(selectedRole)
        }

        // Also listen for manual role entry
        binding.textfieldRole.addTextChangedListener { editable ->
            profileSetupViewModel.setProfession(editable.toString())
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}