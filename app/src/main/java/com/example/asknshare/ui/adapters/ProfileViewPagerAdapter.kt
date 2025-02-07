package com.example.asknshare.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.asknshare.ui.fragments.ProfileSetupFirstFragment
import com.example.asknshare.ui.fragments.ProfileSetupSecondFragment
import com.example.asknshare.ui.fragments.ProfileSetupThirdFragment

class ProfileViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3 // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileSetupFirstFragment()
            1 -> ProfileSetupSecondFragment()
            2 -> ProfileSetupThirdFragment()
            else -> ProfileSetupFirstFragment()
        }
    }
}
