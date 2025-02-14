package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ProfileSetUpViewModel : ViewModel() {
    // LiveData to store user data
    private val _username = MutableLiveData<String>()
    private val _fullName = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _dob = MutableLiveData<String>()
    private val _profession = MutableLiveData<String>()
    private val _expertise = MutableLiveData<List<String>>()
    private val _skills = MutableLiveData<List<String>>()
    private val _location = MutableLiveData<String>()
    private val _gender = MutableLiveData<String>()
    private val _organization = MutableLiveData<String>()
    private val _bio = MutableLiveData<String>()

    val username: LiveData<String> get() = _username
    val fullName: LiveData<String> get() = _fullName
    val email: LiveData<String> get() = _email
    val dob: LiveData<String> get() = _dob
    val profession: LiveData<String> get() = _profession
    val location: LiveData<String> get() = _location
    val gender: LiveData<String> get() = _gender
    val organization: LiveData<String> get() = _organization
    val bio: LiveData<String> get() = _bio


    // LiveData to store selected expertise
    private val _selectedExpertise = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedExpertise: LiveData<MutableList<String>> get() = _selectedExpertise

    // Function to add or remove expertise
    fun updateSelectedExpertise(expertise: String, isChecked: Boolean) {
        val currentList = _selectedExpertise.value ?: mutableListOf()
        if (isChecked) {
            if (!currentList.contains(expertise)) {
                currentList.add(expertise)
            }
        } else {
            currentList.remove(expertise)
        }
        _selectedExpertise.value = currentList
    }
     // LiveData to store selected expertise
    private val _selectedInterests = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedInterests: LiveData<MutableList<String>> get() = _selectedInterests

    // Function to add or remove expertise
    fun updateSelectedInterests(interest: String, isChecked: Boolean) {
        val currentList = _selectedInterests.value ?: mutableListOf()
        if (isChecked) {
            if (!currentList.contains(interest)) {
                currentList.add(interest)
            }
        } else {
            currentList.remove(interest)
        }
        _selectedInterests.value = currentList
    }



    // Functions to update user data
    fun setUsername(username: String) {
        _username.value = username
    }

    fun setFullName(fullName: String) {
        _fullName.value = fullName
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setDob(dob: String) {
        _dob.value = dob
    }

    fun setProfession(profession: String) {
        _profession.value = profession
    }

    fun setExpertise(expertise: List<String>) {
        _expertise.value = expertise
    }

    fun setSkills(skills: List<String>) {
        _skills.value = skills
    }

    fun setLocation(location: String) {
        _location.value = location
    }

    fun setGender(gender: String) {
        _gender.value = gender
    }

    fun setOrganization(organization: String) {
        _organization.value = organization
    }

    fun setBio(bio: String) {
        _bio.value = bio
    }
}