package com.example.asknshare.repo

import android.net.Uri
import com.example.asknshare.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object UserProfileRepo {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    // Fetch user profile data using email instead of UID
    fun fetchUserProfile(onUserDataFetched: (Map<String, Any?>) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onUserDataFetched(emptyMap())
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            onUserDataFetched(emptyMap())
            return
        }

        val usersRef = firebaseDatabase.reference.child(Constants.USER_NODE)
        usersRef.orderByChild(Constants.EMAIL).equalTo(userEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userData = mapOf(
                                Constants.USER_NAME to userSnapshot.child("user_name").getValue(String::class.java),
                                Constants.FULL_NAME to userSnapshot.child("full_name").getValue(String::class.java),
                                Constants.PROFESSION to userSnapshot.child("profession").getValue(String::class.java),
                                Constants.DOB to userSnapshot.child("dob").getValue(String::class.java),
                                Constants.GENDER to userSnapshot.child("gender").getValue(String::class.java),
                                Constants.ORGANIZATION to userSnapshot.child("organization").getValue(String::class.java),
                                Constants.LOCATION to userSnapshot.child("location").getValue(String::class.java),
                                Constants.BIO to userSnapshot.child("bio").getValue(String::class.java),
                                Constants.PROFILE_PIC to userSnapshot.child("profile_pic").getValue(String::class.java)
                            )
                            onUserDataFetched(userData)
                            return
                        }
                    } else {
                        onUserDataFetched(emptyMap())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onUserDataFetched(emptyMap())
                }
            })
    }

    // Update user profile data
    fun updateUserProfile(userData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onComplete(false)
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrEmpty()) {
            onComplete(false)
            return
        }

        val usersRef = firebaseDatabase.reference.child(Constants.USER_NODE)
        usersRef.orderByChild(Constants.EMAIL).equalTo(userEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            userSnapshot.ref.updateChildren(userData)
                                .addOnSuccessListener { onComplete(true) }
                                .addOnFailureListener { onComplete(false) }
                            return
                        }
                    } else {
                        onComplete(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
    }
}
