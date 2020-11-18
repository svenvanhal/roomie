package com.example.roomie.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.roomie.model.RoomOffer
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface RoomOfferDao {
    @Query("SELECT * FROM RoomOffer")
    fun getAll(): Flow<List<RoomOffer>>

    @Query("SELECT * FROM RoomOffer WHERE id IN (:roomIds)")
    fun loadAllByIds(roomIds: IntArray): Flow<List<RoomOffer>>

    @Insert
    fun insertAll(roomOffers: List<RoomOffer>)

    @Delete
    fun delete(roomOffer: RoomOffer)

    @Query("DELETE FROM RoomOffer")
    fun deleteAll()

    @Query("SELECT MAX(created_at) FROM RoomOffer")
    fun getLastTimestamp(): Flow<Instant?>
}
