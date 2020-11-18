package com.example.roomie.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomie.model.RoomOffer
import java.text.NumberFormat
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun durationToString(duration: Duration): String {

    if (duration.toDays() < 1) {
        val hours = duration.toHours()

        return if (hours > 0) {
            "${hours}h left"
        } else {
            val minutes = duration.toMinutes() - (60 * hours)
            "${hours}h ${minutes}m left"
        }
    }

    return ""

}

@Composable
fun RoomOfferRow(roomOffer: RoomOffer, onUserClick: (RoomOffer) -> Unit) {

    val dateFormat = DateTimeFormatter.ofPattern("MMM d")
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault());

    // Calculate time remaining
    val timeRemaining = roomOffer.timeRemaining()

    // Create info line
    val surface = "${roomOffer.surface}m\u00B2"
    val available =
        if (roomOffer.availableFrom != null) "Available ${dateFormat.format(roomOffer.availableFrom)}" else ""
    val floor = roomOffer.floor

    Row(modifier = Modifier.clickable(onClick = { onUserClick(roomOffer) }).fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically).weight(0.8F)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = roomOffer.address, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                Text(
                    text = durationToString(timeRemaining),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1
                )
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
            RoomPrice(roomOffer.totalRent)
        }
    }
}

@Composable
private fun RoomPrice(totalRent: Float) {
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
