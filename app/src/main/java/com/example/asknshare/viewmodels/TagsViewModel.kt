package com.example.asknshare.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class TagViewModel : ViewModel() {
    private val _tags = MutableLiveData<MutableList<String>>(mutableListOf())
    val tags: LiveData<List<String>> get() = _tags.map { it.toList() }

    fun addTag(tag: String) {
        val updatedTags = _tags.value ?: mutableListOf()
        if (!updatedTags.contains(tag)) {
            updatedTags.add(tag)
            _tags.value = updatedTags
        }
    }

    fun removeTag(tag: String) {
        val updatedTags = _tags.value ?: mutableListOf()
        updatedTags.remove(tag)
        _tags.value = updatedTags
    }


    fun clearTags() {
        _tags.value = mutableListOf()
    }
}
