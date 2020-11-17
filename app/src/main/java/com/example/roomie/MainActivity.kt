package com.example.roomie

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.platform.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.roomie.ui.RoomList
import com.example.roomie.ui.RoomieTheme
import com.example.roomie.worker.getRooms

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "Called ViewModelProvider.get")
        viewModel = ViewModelProvider(this).get(RoomViewModel::class.java)

        // TODO: ViewModel for Rooms
        // TODO: fetch data from API (periodically) and update ViewModel, which in turn updates interface
        // TODO: time left as live data (update with app open)

        val rooms = getRooms(applicationContext, "test.json")
            .filter {
                it.isStudio
                        && it.municipality == "Delft"
                        && !it.address.contains("Arubastraat")
            }
            .sortedBy { it.timeRemaining() }

        setContent {
            RoomieTheme {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("ROOMs in Delft (${rooms.size})") },
                            actions = {
                                IconButton(onClick = {
                                    Toast.makeText(applicationContext, "TODO", Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Default.Info) }
                            }
                        )
                    },
                    bodyContent = { RoomList(rooms) }
                )

            }
        }
    }

}