package com.example.proyectorecetas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class SharedViewModel : ViewModel() {
    val toggleDrawerEvent = MutableLiveData<Unit>()

    fun toggleDrawer() {
        toggleDrawerEvent.value = Unit
    }
}