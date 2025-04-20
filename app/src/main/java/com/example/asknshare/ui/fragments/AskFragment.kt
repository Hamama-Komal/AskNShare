package com.example.asknshare.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.asknshare.R
import com.example.asknshare.ui.activities.FullViewActivity
import com.example.asknshare.ui.activities.PostQuestionActivity
import com.example.asknshare.databinding.FragmentAskBinding
import com.example.asknshare.databinding.FragmentSearchBinding
import com.example.asknshare.ui.activities.AIChatActivity
import com.example.asknshare.ui.activities.SetUpProfileActivity

class AskFragment : Fragment() {

    private var _binding: FragmentAskBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAskBinding.inflate(inflater, container, false)


        binding.buttonPostQuestion.setOnClickListener {
            val intent = Intent(context, PostQuestionActivity::class.java)
            intent.putExtra("isAnonymous", false)
            startActivity(intent)
        }

         binding.buttonPostAnonymous.setOnClickListener {
             val intent = Intent(context, PostQuestionActivity::class.java)
             intent.putExtra("isAnonymous", true)
             startActivity(intent)
        }

         binding.buttonAi.setOnClickListener {
             startActivity(Intent(context, AIChatActivity::class.java))
        }


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}