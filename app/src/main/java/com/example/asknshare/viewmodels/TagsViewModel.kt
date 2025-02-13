package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TagsViewModel: ViewModel() {

    private val _selectedTags = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedTags: LiveData<MutableSet<String>> get() = _selectedTags

    fun toggleTag(tag: String) {
        val currentTags = _selectedTags.value ?: mutableSetOf()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag) // Unselect tag
        } else {
            currentTags.add(tag) // Select tag
        }
        _selectedTags.value = currentTags
    }

    fun isTagSelected(tag: String): Boolean {
        return _selectedTags.value?.contains(tag) ?: false
    }
}