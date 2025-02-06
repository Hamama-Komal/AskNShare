package com.example.asknshare.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.asknshare.R
import com.example.asknshare.databinding.FragmentProfileSetupFirstBinding
import com.example.asknshare.databinding.FragmentProfileSetupSecondBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileSetupSecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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

}