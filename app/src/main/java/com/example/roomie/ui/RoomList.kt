package com.example.roomie.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomie.model.Room
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
private fun RoomRow(room: Room, onUserClick: (Room) -> Unit) {

    val dateFormat = DateTimeFormatter.ofPattern("MMM d")
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault());

    // Calculate time remaining
    val interval = room.timeRemaining()
    val intervalStr = if (interval.toDays() < 1) "${interval.toHours()}h left" else ""

    // Create info line
    val surface = "${room.surface}m\u00B2"
    val available =
        if (room.availableFrom != null) "Available ${dateFormat.format(room.availableFrom)}" else ""
    val floor = room.floor

    Row(modifier = Modifier.clickable(onClick = { onUserClick(room) }).fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically).weight(0.8F)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = room.address, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                Text(text = intervalStr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(text = available, style = MaterialTheme.typography.body2)
                Text(" \u00B7 ")
                Text(text = surface, style = MaterialTheme.typography.body2)
                Text(" \u00B7 ")
                Text(text = floor, style = MaterialTheme.typography.body2)
            }
        }
        Box(alignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterVertically).weight(0.2F)) {
            PriceBox(room.totalRent)
        }
    }
}

@Composable
fun PriceBox(totalRent: Float) {
    val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
    moneyFormat.currency = Currency.getInstance("EUR")
    moneyFormat.maximumFractionDigits = 0

    Text(
        text = moneyFormat.format(totalRent),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.subtitle1,
        textAlign = TextAlign.Center,
        fontSize = 20.sp
    )
}

@Composable
fun RoomList(roomList: List<Room>) {
    val context = ContextAmbient.current
    LazyColumnFor(items = roomList) { room ->
        RoomRow(room = room, onUserClick = {
            // Open listing browser
            val url = "https://www.room.nl/aanbod/studentenwoningen/details/${room.urlKey}"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
        Divider()
    }
}