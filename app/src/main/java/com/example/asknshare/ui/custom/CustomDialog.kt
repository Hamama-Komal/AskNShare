package com.example.asknshare.ui.custom

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.asknshare.databinding.CustomDialogBinding

class CustomDialog(
    private val context: Context,
    private val title: String,
    private val subtitle: String,
    private val positiveButtonText: String,
    private val negativeButtonText: String,
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
) {

    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val binding = CustomDialogBinding.inflate(inflater, null, false)
        builder.setView(binding.root)

        // Set title and subtitle
        binding.tvTitle.text = title
        binding.tvSubtitle.text = subtitle

        // Set button texts
        binding.btnPositive.text = positiveButtonText
        binding.btnNegative.text = negativeButtonText

        // Create and show the dialog
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Positive button click listener
        binding.btnPositive.setOnClickListener {
            onPositiveClick.invoke()
            alertDialog.dismiss()
        }

        // Negative button click listener
        binding.btnNegative.setOnClickListener {
            onNegativeClick.invoke()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}