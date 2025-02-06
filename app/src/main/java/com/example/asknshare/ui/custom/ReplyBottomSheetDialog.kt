package com.example.asknshare.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.asknshare.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReplyBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reply_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val publishReplyButton = view.findViewById<Button>(R.id.button_publish_reply)
        publishReplyButton.setOnClickListener {
            dismiss() // Close the bottom sheet after submitting reply
        }
    }
}