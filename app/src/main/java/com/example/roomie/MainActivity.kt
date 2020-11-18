package com.example.roomie

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.roomie.model.RoomOffer
import com.example.roomie.model.RoomOfferViewModel
import com.example.roomie.ui.RoomOfferList
import com.example.roomie.ui.RoomieTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var roomOfferViewModel: RoomOfferViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomOfferViewModel = ViewModelProvider(this).get(RoomOfferViewModel::class.java)

        // TODO: enqueue periodic work request here (so we can also send push messages if something has changed)
        roomOfferViewModel.enqueuePeriodicalRefresh()

        val roomOfferFilter = fun(roomOffer: RoomOffer): Boolean {
            return (roomOffer.closingDate!! > Instant.now() // TODO: this will crash if closingDate not set
                    && roomOffer.isStudio
                    && roomOffer.municipality == "Delft"
                    && !roomOffer.address.contains("Arubastraat")
                    && !roomOffer.address.contains("Röntgenweg")
                    && !roomOffer.address.contains("Van Hasseltlaan")
                    && !roomOffer.address.contains("Zusterlaan"))
        }

        setContent {
            RoomieTheme {
                RoomAppScreen(applicationContext, roomOfferViewModel, roomOfferFilter)
            }
        }
    }

}


@Composable
private fun TopBarTitle(nRooms: Int) {
    Text("ROOMs in Delft (${nRooms})")
}

@Composable
private fun RoomAppScreen(context: Context, roomOfferViewModel: RoomOfferViewModel, filterFun: (RoomOffer) -> Boolean) {
    // Retrieve rooms from LiveData
    val roomOffers: List<RoomOffer> by roomOfferViewModel.rooms.observeAsState(emptyList())

    // Make selection
    val selection = roomOffers.filter(filterFun).sortedBy { it.timeRemaining() }

    // Build app screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopBarTitle(selection.size)
                },
                actions = {
                    IconButton(onClick = {
                        roomOfferViewModel.forceRefreshRooms()
                    }) {
                        Icon(Icons.Default.Refresh)
                    }
                }
            )
        },
        bodyContent = {
            Surface {
                RoomOfferList(
                    items = selection
                )
            }
        },
        bottomBar = {
            InfoBottomBar(roomOfferViewModel)
        }
    )
}

@Composable
fun InfoBottomBar(roomOfferViewModel: RoomOfferViewModel) {
    val dateFormat = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())

    val lastUpdated: Instant? by roomOfferViewModel.lastUpdated.observeAsState()
    val lastUpdatedStr = if (lastUpdated != null) dateFormat.format(lastUpdated) else "Unknown"

    Row(
        modifier = Modifier.background(MaterialTheme.colors.primaryVariant).fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Last updated: $lastUpdatedStr".toUpperCase(Locale.getDefault()),
            color = MaterialTheme.colors.onPrimary,
            fontSize = 12.sp
        )
    }
}