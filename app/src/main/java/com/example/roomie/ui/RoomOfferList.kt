package com.example.roomie.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ContextAmbient
import com.example.roomie.model.RoomOffer

@Composable
fun RoomOfferList(items: List<RoomOffer>) {
    val context = ContextAmbient.current
    LazyColumnFor(items = items) { room ->
        RoomOfferRow(roomOffer = room, onUserClick = {
            // Open listing browser
            val url = "https://www.room.nl/aanbod/studentenwoningen/details/${room.urlKey}"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        })
        Divider()
    }
}
