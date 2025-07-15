package com.example.proyectorecetas

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _drawerState = MutableStateFlow(false)
    val drawerState = _drawerState.asStateFlow()

    fun toggleDrawer() {
        _drawerState.value = !_drawerState.value
    }

    fun closeDrawer() {
        _drawerState.value = false
    }
}