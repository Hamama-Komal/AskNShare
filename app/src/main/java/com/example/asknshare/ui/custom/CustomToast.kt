package com.example.asknshare.ui.custom

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.asknshare.R


fun showCustomToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        // Set the message
        val textView = layout.findViewById<TextView>(R.id.toast_message)
        textView.text = message

        // Build and show the toast
        val toast = Toast(context).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_LONG
            view = layout
        }
        toast.show()
}