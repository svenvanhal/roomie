package com.example.roomie

import android.util.Log
import androidx.lifecycle.ViewModel

class RoomViewModel : ViewModel() {
    init {
        Log.i("RoomViewModel", "GameViewModel created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "GameViewModel destroyed!")
    }
}
